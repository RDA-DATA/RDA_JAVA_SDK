
package com.sds.iot.sdk.protocol.mqtt;

import javax.net.ssl.SSLContext;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.iot.sdk.IotClientConfig;
import com.sds.iot.sdk.auth.IAuth;
import com.sds.iot.sdk.exception.IotException;
import com.sds.iot.sdk.message.IotMessage;
import com.sds.iot.sdk.message.MessageConverter;
import com.sds.iot.sdk.protocol.IProtocol;
import com.sds.iot.sdk.protocol.ITransportListener;
import com.sds.iot.sdk.protocol.tls.TlsSupport;

/**
 * 이 클래스는 클라이언트에서 MQTT 프로토콜을 이용한 서버 연결 및 메시지 전송에 대한 함수를 제공합니다.
 * 
 * @author SDS
 */
public class MqttProtocol implements IProtocol {
	private static final Logger	LOGGER				= LoggerFactory.getLogger(MqttProtocol.class);
	private static final String	TOPIC_PREFIX		= "ocp/";
	private static final String	INTERNAL_CLIENT_ID	= "dataBus";

	private IotClientConfig		cfg;
	private MessageConverter	converter;

	/** paho client */
	private MqttClient			mqttClient			= null;
	private MqttConnectOptions	mqttConnOptions		= new MqttConnectOptions();

	/** "{siteId}/{thingName}" */
	private String				mqttClientId;

	/** "ocp/{siteId}/{thingName}" */
	private String				mqttSubscribeTopic;

	/** "ocp/dataBus" */
	private String				mqttPublishTopic	= TOPIC_PREFIX + INTERNAL_CLIENT_ID;

	private MqttProtocolConfig getProtocolCfg() {
		return (MqttProtocolConfig) cfg.getProtocolCfg();
	}

	/**
	 * MQTT 클라이언트 초기화
	 */
	@Override
	public void init(IotClientConfig cfg, MessageConverter converter, ITransportListener transport, IAuth auth) {
		this.cfg = cfg;
		this.converter = converter;

		if (cfg.getProtocolCfg() == null) {
			// protocol config 를 별도 설정하지 않으면 디폴트 값으로 생성
			cfg.setProtocolCfg(new MqttProtocolConfig());
		}

		this.mqttClientId = cfg.getSiteId() + "+" + cfg.getThingName();
		this.mqttSubscribeTopic = TOPIC_PREFIX + cfg.getSiteId() + "/" + cfg.getThingName();

		// set mqttConnOptions 
		mqttConnOptions.setUserName(mqttClientId);
		if (auth.getCredential() != null) {
			LOGGER.trace("[MQTT] connOptions {}", auth.getCredential());
			mqttConnOptions.setPassword(auth.getCredential().toCharArray());
		}

		mqttConnOptions.setKeepAliveInterval(getProtocolCfg().getKeepAliveInterval());
		mqttConnOptions.setConnectionTimeout(getProtocolCfg().getConnectionTimeout());
		mqttConnOptions.setMaxInflight(getProtocolCfg().getMaxInflight());
		mqttConnOptions.setAutomaticReconnect(getProtocolCfg().isAutomaticReconnect());
		mqttConnOptions.setCleanSession(getProtocolCfg().isCleanSession());

		// create MqttClient object
		// mqttClient 객체를 만들고 나면, serverAddr 변경 불가
		if (mqttClient == null) {
			if (cfg.isServerAddrSsl()) {
				try {
					SSLContext sslContext = TlsSupport.createTlsContext(cfg.getSslProperties());
					mqttConnOptions.setSocketFactory(sslContext.getSocketFactory());
				} catch (Exception e) {
					throw new IllegalStateException("Fail to make ssl context.", e);
				}
			}
			try {
				mqttClient = new MqttClient(cfg.getServerAddr(), mqttClientId, null);
			} catch (MqttException e) {
				throw new IllegalArgumentException("Fail to create MQTT Client", e);
			}
		} else if (mqttClient.isConnected()) {
			throw new IllegalStateException("MQTT client is already initialized and connected. Please disconnect and connect.");
		} else {
			LOGGER.trace("[MQTT] mqttClient is not null, but it is not connected. It may be disconnected and reconnecting...");
		}

		// set MqttClient options
		MqttCallback mqttProtocolListener = new MqttProtocolListener(transport, converter);
		mqttClient.setCallback(mqttProtocolListener);
		mqttClient.setTimeToWait(getProtocolCfg().getTimeToWaitInMillis());
	}

	/**
	 * MQTT 클라이언트 서버로 연결
	 */
	@Override
	public void connect() {
		try {
			LOGGER.info("[MQTT] Connect : {}", cfg.getServerAddr());

			mqttClient.connect(mqttConnOptions);

			if (!mqttClient.isConnected()) {
				throw new IllegalStateException("MQTT client is not connected!");
			}

			// MQTT SUBSCRIBE
			try {
				LOGGER.debug("[MQTT] Subscribe : {}", mqttSubscribeTopic);
				mqttClient.subscribe(mqttSubscribeTopic, getProtocolCfg().getSubscribeQos());
			} catch (MqttException e) {
				throw new IllegalStateException("Fail to MQTT subscribe!", e);
			}

		} catch (MqttSecurityException e) {
			// MQTT 인증 오류이면 재시도 하지 않음
			LOGGER.error("[MQTT] Fail to connect (MqttSecurityException) : {}", e.toString());
			throw new IotException("401", "Fail to connect to MQTT server!", e);

		} catch (MqttException e) {
			LOGGER.warn("[MQTT] Fail to connect (MqttException) : {}", e.toString());
			throw new IotException("500", "Fail to connect to MQTT server!", e);
		}
	}

	/**
	 * MQTT 클라이언트 연결 종료
	 */
	@Override
	public void disconnect() {
		if (mqttClient == null) {
			LOGGER.debug("[MQTT] Disconnection is not needed. mqttClient is null.");
			return;
		} else if (!isConnected()) {
			LOGGER.debug("[MQTT] Disconnection is not needed. mqttClient is already disconnected.");
			return;
		}

		LOGGER.debug("[MQTT] Disconnect");
		try {
			mqttClient.disconnect();
		} catch (MqttException e) {
			LOGGER.warn("[MQTT] Exception occurred while disconnecting : {}", e.toString());
		}

	}

	/**
	 * MQTT 클라이언트에서 서버로 메시지 전송.
	 */
	@Override
	public void send(IotMessage msg) throws Exception {

		byte[] payload = converter.convertToBytes(msg);

		LOGGER.debug("[MQTT] Publish {} bytes : {}", payload.length, mqttPublishTopic);

		MqttMessage mqttMsg = new MqttMessage(payload);
		mqttMsg.setQos(getProtocolCfg().getPublishQos());
		MqttDeliveryToken token = mqttClient.getTopic(mqttPublishTopic).publish(mqttMsg);

		LOGGER.trace("[MQTT] publish start (token={})", token.getMessageId());
	}

	/**
	 * MQTT 클라이언트 연결 여부
	 */
	@Override
	public boolean isConnected() {
		return mqttClient.isConnected();
	}

}
