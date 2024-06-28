
package com.sds.iot.sdk.message.headerformat;

import com.sds.iot.sdk.message.IotMessage;

/**
 * 이 인터페이스 클래스는  헤더 포맷 변환을 제공하기 위한 기본 구현체입니다.
 * 
 * @author SDS
 */
public interface IHeaderFormatConverter {

	/**
	 * convert IotMessage to byte[] (Specified header format).
	 * 헤더 포맷 변환만 수행.
	 * 
	 * @param msg
     *        메시지
	 * @return byte[]로 변환된 헤더 메시지 
	 */
	byte[] convertToBytes(IotMessage msg);

	/**
	 * convert byte[] (Specified header format) to IotMessage.
	 * 헤더 포맷 변환만 수행.
	 * 
	 * @param bytes
     *        메시지
	 * @return IotMessage로 변환된  헤더 메시지
	 * @throws IllegalStateException
	 */
	IotMessage convertFromBytes(byte[] bytes) throws IllegalStateException;

}
