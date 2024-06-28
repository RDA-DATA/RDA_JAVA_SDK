
package com.sds.iot.sdk.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.iot.sdk.message.compress.IDataCompressor;
import com.sds.iot.sdk.message.encrypt.IDataEncryptor;
import com.sds.iot.sdk.message.headerformat.IHeaderFormatConverter;

/**
 * 이 클래스는 메시지 변환 관련 함수를 제공합니다.
 * 
 * @author SDS
 *
 */
public class MessageConverter {
	private static final Logger		LOGGER	= LoggerFactory.getLogger(MessageConverter.class);

	private IHeaderFormatConverter	headerFormatConverter;
	private IDataCompressor			dataCompressor;
	private IDataEncryptor			dataEncryptor;

	public MessageConverter(IHeaderFormatConverter headerFormatConverter, IDataCompressor dataCompressor,
			IDataEncryptor dataEncryptor) {
		this.headerFormatConverter = headerFormatConverter;
		this.dataCompressor = dataCompressor;
		this.dataEncryptor = dataEncryptor;
	}

	/**
	 * 메시지를 byte[]로 변환
	 * @param msg
	 *        메시지
	 * @return 변환된 메시지(byte[])
	 */
	public byte[] convertToBytes(IotMessage msg) {

		// 데이터부 암호화
		if (dataEncryptor.needEncrypt(msg)) {
			LOGGER.info("****** ENCRYPT SENDING DATA : encType={}", dataEncryptor.getEncType());
			byte[] encData = dataEncryptor.encrypt(msg.getData());
			msg.setEncType(dataEncryptor.getEncType());
			msg.setData(encData);
		}

		// 데이터부 압축
		if (dataCompressor.needCompress(msg)) {
			LOGGER.info("****** COMPRESS SENDING DATA : type={}", dataCompressor.getCompressType());
			byte[] compData = dataCompressor.compress(msg.getData());
			// 압축이 수행된 경우 메시지의 encType 앞쪽에 Z를 붙인다.
			msg.setEncType(dataCompressor.getCompressType() + msg.getEncType());
			msg.setData(compData);
		}

		// 헤더포맷 변환
		return headerFormatConverter.convertToBytes(msg);
	}

	/**
	 * byte[]데이터를 메시지로 변환
	 * @param bytes
	 *        메시지
	 * @return 변환된 메시지(IotMessage)
	 * @throws IllegalStateException
	 */
	public IotMessage convertFromBytes(byte[] bytes) throws IllegalStateException {

		// 헤더포맷 파싱 
		IotMessage msg = headerFormatConverter.convertFromBytes(bytes);

		// 데이터부 압축해제
		if (dataCompressor.needDecompress(msg)) {
			LOGGER.info("****** DECOMPRESS RECEIVED DATA : type={}", dataCompressor.getCompressType());
			byte[] decData = dataCompressor.decompress(msg.getData());
			// 압축을 해제한 경우 메시지의 encType 앞쪽에 Z를 제거한다.
			msg.setEncType(msg.getEncType().substring(1));
			msg.setData(decData);
		}

		// 데이터부 복호화
		if (dataEncryptor.needDecrypt(msg)) {
			LOGGER.info("****** DECRYPT RECEIVED DATA : encType={}", dataEncryptor.getEncType());
			byte[] plainData = dataEncryptor.decrypt(msg.getData());
			msg.setEncType("0");
			msg.setData(plainData);
			LOGGER.info("****** DECRYPT RESULT : {}", new String(plainData));
		}

		return msg;
	}

}
