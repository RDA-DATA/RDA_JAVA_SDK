
package com.sds.iot.sdk.auth.mutual;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.iot.sdk.IotClientConfig;
import com.sds.iot.sdk.auth.IAuth;
import com.sds.iot.sdk.exception.IotException;
import com.sds.iot.sdk.message.IotMessage;
import com.sds.iot.sdk.message.IotMessageCodeEnum;
import com.sds.iot.sdk.protocol.tls.TlsSupport;
import com.sds.iot.sdk.util.JsonUtil;

/**
 * 이 클래스는 SSL인증을 위한 관리(연결, 설정 등) 기능을 제공합니다.
 * 
 * @author SDS
 */
public class MutualTlsAuth implements IAuth {
	private static final Logger	LOGGER		= LoggerFactory.getLogger(MutualTlsAuth.class);

	private String				authToken	= null;

	@Override
	public String getAuthTypeName() {
		return "MutualTLS";
	}

	@Override
	public void requestCredential() throws IotException {
		/* nothing */
	}

	@Override
	public String getCredential() {
		/* nothing */
		return null;
	}

	@Override
	public void setConfig(IotClientConfig cfg) {
		// 현재는 IotClient 객체 생성 단계에서 익셉션 발생시킴. 
		// 만약 connect 단계에서 익셉션 발생하게 바꾸려면 requestCredential() 로 validation 로직 이동.
		validateConfig(cfg);
	}

	@Override
	public IotMessage makeDeviceAuthMessage(int seq) {
		IotMessage msg = new IotMessage();
		msg.setMsgType("Q");
		msg.setFuncType("002");
		msg.setMsgCode(IotMessageCodeEnum.AUTH_PROCESS2_REQ.getMsgCode());

		return msg;
	}

	@Override
	public void onAuthResponseMessageReceived(IotMessage resMsg) {
		LOGGER.trace("Auth response message received... data=[{}]", resMsg.getDataString());
		// MutualTLS는 serverAddress 가 바뀌지도 않고 연결유지 방식이므로 authToken 불필요
		if (resMsg.getResCode().startsWith("2")) {
			this.authToken = parseAuthToken(resMsg.getDataString());
			LOGGER.trace("authToken={}", authToken);
		}
	}

	@Override
	public String getAuthToken() {
		// MutualTLS는 serverAddress 가 바뀌지도 않고 연결유지 방식이므로 authToken 불필요
		//return null;
		return this.authToken;
	}

	private void validateConfig(IotClientConfig cfg) {
		if (!cfg.isServerAddrSsl()) {
			LOGGER.error("Cannot use 'Mutual TLS'. Configured server address is not 'ssl' protocol. serverAddr={}",
					cfg.getServerAddr());
			throw new IllegalArgumentException(
					"Cannot use 'Mutual TLS'. Configured server address is not 'ssl' protocol. serverAddr=" + cfg.getServerAddr());
		}

		if (!TlsSupport.isMutualTlsAvailable(cfg.getSslProperties())) {
			LOGGER.error("Cannot use 'Mutual TLS'. Check your ssl property 'MUTUAL_TLS_KEY' and 'MUTUAL_TLS_CRT' value.");
			throw new IllegalArgumentException(
					"Cannot use 'Mutual TLS'. Check your ssl property 'MUTUAL_TLS_KEY' and 'MUTUAL_TLS_CRT' value.");
		}
	}

	private static String parseAuthToken(String jsonStr) {
		Map<String, Object> resultMap = JsonUtil.fromJsonToMap(jsonStr);
		return (String) resultMap.get("authToken");
	}
}
