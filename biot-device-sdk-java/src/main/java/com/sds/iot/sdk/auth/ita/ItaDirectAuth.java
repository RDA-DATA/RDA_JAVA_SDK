
package com.sds.iot.sdk.auth.ita;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.sds.iot.sdk.IotClientConfig;
import com.sds.iot.sdk.auth.IAuth;
import com.sds.iot.sdk.exception.IotException;
import com.sds.iot.sdk.message.IotMessage;
import com.sds.iot.sdk.message.IotMessageCodeEnum;

/**
 * 이 클래스는 사물 인증 코드를 이용한 방식의 인증 처리를 위한 함수를 제공합니다.
 * 
 * @author SDS
 */
public class ItaDirectAuth implements IAuth {
	private static final Logger	LOGGER		= LoggerFactory.getLogger(ItaDirectAuth.class);

	private String				authCode	= null;
	private String				authToken	= null;

	protected ItaDirectAuth() {
		super();
	}

	/**
	 * @param authCode
	 *        : set authCode directly.
	 */
	public ItaDirectAuth(String authCode) {
		super();
		setAuthCode(authCode);
	}

	/**
	 * @param authCode
	 *        : set authCode directly.
	 */
	protected void setAuthCode(String authCode) {
		this.authCode = authCode;
	}

	@Override
	public String getAuthTypeName() {
		return "ITA_Direct";
	}

	@Override
	public void requestCredential() throws IotException {
		/* nothing */
	}

	@Override
	public String getCredential() {
		return authCode;
	}

	@Override
	public void setConfig(IotClientConfig cfg) {
		/* nothing */
	}

	@Override
	public IotMessage makeDeviceAuthMessage(int seq) {
		if (seq != 1) {
			return null;
		}

		JsonObject data = new JsonObject();
		data.addProperty("authCode", this.getCredential());
		String dataStr = data.toString();
		//LOGGER.trace("[{}]", dataStr);

		IotMessage msg = new IotMessage();
		msg.setMsgType("Q");
		msg.setFuncType("002");
		msg.setMsgCode(IotMessageCodeEnum.AUTH_PROCESS2_REQ.getMsgCode());
		msg.setDataString(dataStr);

		return msg;
	}

	@Override
	public void onAuthResponseMessageReceived(IotMessage resMsg) {
		LOGGER.trace("Auth response message received... data=[{}]", resMsg.getDataString());
		if (resMsg.getResCode().startsWith("2")) {
			this.authToken = ItaAuthSupport.parseAuthToken(resMsg.getDataString());
			LOGGER.trace("authToken={}", authToken);
		}
	}

	@Override
	public String getAuthToken() {
		return this.authToken;
	}

}
