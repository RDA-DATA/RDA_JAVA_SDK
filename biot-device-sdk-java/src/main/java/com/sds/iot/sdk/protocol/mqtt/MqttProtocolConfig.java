
package com.sds.iot.sdk.protocol.mqtt;

import java.util.Properties;

import com.sds.iot.sdk.protocol.IProtocolConfig;
import com.sds.iot.sdk.util.ConfigFileUtil;

/**
 * 이 클래스는 Paho MQTT 라이브러리에서 클라이언트 관련 연결 설정에 대한 기본값을 제공합니다.
 * 
 * @author SDS
 */
public class MqttProtocolConfig implements IProtocolConfig {

	/* PAHO CONFIG ============================================================= */

	// for MqttConnectOptions
	private int						connectionTimeout	= 5;
	/** mqtt broker 내에서 연결을 끊을 수 있으므로 mqtt 자체 keep-alive 는 보내지 않도록 함 */
	private int						keepAliveInterval	= 0;
	private int						maxInflight			= 10;

	// for MqttConnectOptions (현재는 고정값 - 설정 불필요)
	private static final boolean	automaticReconnect	= false; // 별도 reconnect 로직을 사용하므로 automaticReconnect false 설정
	private static final boolean	cleanSession		= false;

	// for MqttClient
	private long					timeToWaitInMillis	= 30_000;

	// for MqttClient subscribe and publish (현재는 고정값 - 설정 불필요)
	private static final int		subscribeQos		= 0;
	private static final int		publishQos			= 1;

	/* ============================================================= */

	@Override
	public void initFromProperties(Properties props) {

		ConfigFileUtil.invokeSetterIfExists(props, "mqtt.", this, "connectionTimeout", int.class);
		ConfigFileUtil.invokeSetterIfExists(props, "mqtt.", this, "keepAliveInterval", int.class);
		ConfigFileUtil.invokeSetterIfExists(props, "mqtt.", this, "maxInflight", int.class);
		ConfigFileUtil.invokeSetterIfExists(props, "mqtt.", this, "timeToWaitInMillis", long.class);

	}

	/* ============================================================= */

	/**
	 * @return Connection Timeout (default 5sec)
	 */
	int getConnectionTimeout() {
		return connectionTimeout;
	}

	/**
	 * <b>seconds. default 5sec.</b>
	 * Sets the connection timeout value.
	 * This value, measured in seconds, defines the maximum time interval the client will wait for
	 * the network connection to the MQTT server to be established.
	 * The default timeout is 30 seconds.
	 * A value of 0 disables timeout processing meaning the client will wait
	 * until the network connection is made successfully or fails.
	 * 
	 * @param connectionTimeout
	 *        the timeout value, measured in seconds. {@literal It must be > 0.}
	 */
	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	/**
	 * @return Keep Alive Interval
	 */
	int getKeepAliveInterval() {
		return keepAliveInterval;
	}

	/**
	 * <b>seconds.</b>
	 * This value, measured in seconds,
	 * defines the maximum time interval between messages sent or received.
	 * It enables the client to detect if the server is no longer available,
	 * without having to wait for the TCP/IP timeout.
	 * The client will ensure that at least one message travels across the network within each keep alive period.
	 * In the absence of a data-related message during the time period,
	 * the client sends a very small "ping" message, which the server will acknowledge.
	 * A value of 0 disables keep alive processing in the client.
	 * 
	 * @param keepAliveInterval
	 *        the interval, measured in seconds, {@literal must be >= 0.}
	 */
	public void setKeepAliveInterval(int keepAliveInterval) {
		this.keepAliveInterval = keepAliveInterval;
	}

	/**
	 * @return Max Inflight
	 */
	int getMaxInflight() {
		return maxInflight;
	}

	/**
	 * Sets the "max inflight". please increase this value in a high traffic environment. The default value is 10
	 * 
	 * @param maxInflight
	 *        the number of maxInfligt messages
	 */
	public void setMaxInflight(int maxInflight) {
		this.maxInflight = maxInflight;
	}

	/**
	 * @return 자동 재연결 여부 (true/false)
	 */
	public boolean isAutomaticReconnect() {
		return automaticReconnect;
	}

	/**
	 * @return 클린 세션 여부 (true/false)
	 */
	public boolean isCleanSession() {
		return cleanSession;
	}

	/**
	 * @return timeout까지 남은 시간
	 */
	long getTimeToWaitInMillis() {
		return timeToWaitInMillis;
	}

	/**
	 * Set the maximum time to wait for an action to complete before returning control to the invoking application.
	 * Control is returned when:
	 * •the action completes
	 * •or when the timeout if exceeded
	 * •or when the client is disconnect/shutdown
	 * The default value is -1 which means the action will not timeout.
	 * In the event of a timeout the action carries on running in the background until it completes.
	 * The timeout is used on methods that block while the action is in progress.
	 * 
	 * @param timeToWaitInMillis
	 *        before the action times out. A value or 0 or -1 will wait until the action finishes and not timeout.
	 */
	public void setTimeToWaitInMillis(long timeToWaitInMillis) {
		this.timeToWaitInMillis = timeToWaitInMillis;
	}

	/**
	 * @return subscribeQos
	 */
	int getSubscribeQos() {
		return subscribeQos;
	}

	/**
	 * @return publishQos
	 */
	int getPublishQos() {
		return publishQos;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("  connectionTimeout=").append(connectionTimeout).append("\n  keepAliveInterval=").append(keepAliveInterval)
				.append("\n  maxInflight=").append(maxInflight).append("\n  timeToWaitInMillis=").append(timeToWaitInMillis);
		return builder.toString();
	}

}
