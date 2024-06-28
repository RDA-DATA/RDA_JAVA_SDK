
package com.sds.iot.sdk.message.encrypt;

import com.sds.iot.sdk.auth.IAuth;
import com.sds.iot.sdk.message.IotMessage;
import com.sds.iot.sdk.message.IotMessageCodeEnum;

/**
 * 이 인터페이스 클래스는 데이터 암/복호화를 제공하기 위한 기본 구현체입니다.
 * 
 * @author SDS
 */
public interface IDataEncryptor {

	public class PlainDataEncryptor implements IDataEncryptor {

		@Override
		public String getEncType() {
			return "0";
		}

		@Override
		public void setAuth(IAuth auth) {
			/* nothing */
		}

		@Override
		public byte[] encrypt(byte[] plainData) {
			return plainData; // PLAIN : no encrypt
		}

		@Override
		public byte[] decrypt(byte[] encryptedData) {
			return encryptedData; // PLAIN : no encrypt
		}

		@Override
		public boolean needEncrypt(IotMessage msg) {
			return false; // PLAIN : no encrypt
		}
	}

	String getEncType();

	void setAuth(IAuth auth);

	byte[] encrypt(byte[] plainData);

	byte[] decrypt(byte[] encryptedData);

	/**
	 * 메시지 암호화 필요 여부 확인
	 * @param msg
	 *        메시지
	 * @return 메시지 암호화 필요 여부 (true/false)
	 */
	default boolean needEncrypt(IotMessage msg) {
		if (isNoEncryptMessage(msg)) {
			return false;
		}
		return msg.getData() != null;
	}

	/**
	 * 메시지 암호화 여부 확인
	 * @param msg
	 *        메시지
	 * @return 메시지 암호화 여부(true/false)
	 */
	default boolean isNoEncryptMessage(IotMessage msg) {
		// 인증, keep alive 메시지는 true
		IotMessageCodeEnum msgCodeEnum = IotMessageCodeEnum.fromMsgCode(msg.getMsgCode());
		if(msgCodeEnum == null)
			return false;
		else{
			switch (msgCodeEnum) {
				case AUTH_PROCESS1_REQ:
				case AUTH_PROCESS2_REQ:
				case CONN_CTN_REQ:
					return true;
				default:
					return false;
			}
		}
	}

	/**
	 * 메시지 복호화 필요 여부 확인
	 * @param msg
	 *        메시지
	 * @return 메시지 복호화 필요 여부 (true/false)
	 */
	default boolean needDecrypt(IotMessage msg) {
		if (msg.getData() == null) {
			return false;
		}
		return !"0".equals(msg.getEncType());
	}

}
