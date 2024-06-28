
package com.sds.iot.sdk.message.compress;

import com.sds.iot.sdk.message.IotMessage;
import com.sds.iot.sdk.message.IotMessageCodeEnum;

/**
 * 이 인터페이스 클래스는 데이터 압축/해제를 제공하기 위한 기본 구현체입니다.
 * @author SDS
 */
public interface IDataCompressor {

	String getCompressType();

	byte[] compress(byte[] plainData);

	byte[] decompress(byte[] encryptedData);

    /**
	 * 데이터 압축 필요 여부 확인
	 * @param msg
	 *        메시지
	 * @return 데이터 압축 필요 여부 (true/false)
	 */
	default boolean needCompress(IotMessage msg) {
		if (isNoCompressMessage(msg)) {
			return false;
		}
		return msg.getData() != null;
	}

    /**
	 * 메시지 압축 여부 확인
	 * @param msg
	 *        메시지
	 * @return 메시지 압축 여부(true/false)
	 */
	default boolean isNoCompressMessage(IotMessage msg) {
		// 인증, keep alive 메시지는 true
		IotMessageCodeEnum msgCodeEnum = IotMessageCodeEnum.fromMsgCode(msg.getMsgCode());
		if (msgCodeEnum == null)
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
	 * 데이터 압축 해제 필요 여부 확인
	 * @param msg
	 *        메시지
	 * @return 데이터 압축 해제 필요 여부 (true/false)
	 */
	default boolean needDecompress(IotMessage msg) {
		if (msg.getData() == null) {
			return false;
		}
		return msg.getEncType().startsWith(this.getCompressType());
	}

}
