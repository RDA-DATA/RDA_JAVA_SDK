
package com.sds.iot.sdk;

import com.sds.iot.sdk.message.compress.IDataCompressor;
import com.sds.iot.sdk.message.compress.ZstdDataCompressor;
import com.sds.iot.sdk.message.encrypt.IDataEncryptor;
import com.sds.iot.sdk.message.encrypt.IDataEncryptor.PlainDataEncryptor;
import com.sds.iot.sdk.message.encrypt.ItaAesDataEncryptor;
import com.sds.iot.sdk.message.headerformat.DelimiterHeaderFormatConverter;
import com.sds.iot.sdk.message.headerformat.IHeaderFormatConverter;
import com.sds.iot.sdk.protocol.IProtocol;
import com.sds.iot.sdk.protocol.mqtt.MqttProtocol;
import com.sds.iot.sdk.protocol.mqtt.MqttProtocolConfig;

/**
 * 이 인터페이스 클래스는 프로토콜 및 헤더 타입 등의 확장이 필요한 경우 호출되는 함수를 정의합니다.
 * 
 * 확장 버전의 프로토콜, 헤더타입 등의 구현체 확장이 필요한 경우,
 * 이 인터페이스를 구현하고 필요한 메서드를 Override 하도록 합니다.
 * 
 * @author SDS
 */
interface IFactory {

	class DefaultFactory implements IFactory {
		static final DefaultFactory INSTANCE = new DefaultFactory();
	}

	/**
	 * 헤더 포맷 생성
	 * @param headerType
	 * 	      Delimiter 헤더 타입만 지원
	 *        : e.g. "D" (Delimiter)
	 * @return 변환된 헤더 포맷 
	 */
	default IHeaderFormatConverter createHeaderFormatConverter(String headerType) {
		if ("D".equals(headerType)) {
			return new DelimiterHeaderFormatConverter();
		} else {
			throw new IllegalArgumentException("Unsupported headerType");
		}
	}

	/**
	 * @param cfg
	 * @return
	 */
	default IDataCompressor createDataCompressor(IotClientConfig cfg) {
		return new ZstdDataCompressor(cfg.getCompressUseBytes());
	}

	/**
	 * 암호화된 데이터 생성
	 * @param encType
	 *        : e.g. "0" (Plain), "3" (ITA_AES128)
	 * @return 암호화된 데이터
	 */
	default IDataEncryptor createDataEncryptor(String encType) {
		if ("0".equals(encType)) {
			return new PlainDataEncryptor();
		} else if ("3".equals(encType)) {
			return new ItaAesDataEncryptor("3", 128);
		} else {
			throw new IllegalArgumentException("Unsupported encType : " + encType);
		}
	}

	/**
	 * IoT Client Protocol 생성
	 * 
	 * @param protocolType
	 * 	      MQTT 프로토콜 지원
	 *        : e.g. "MQTT" (MQTT Client)
	 * @return 생성된 프로토콜
	 */
	default IProtocol createProtocol(String protocolType) {
		if ("MQTT".equals(protocolType)) {
			return new MqttProtocol();
		} else {
			throw new IllegalArgumentException("Unsupported protocolType : " + protocolType);
		}
	}

	/**
	 * 프로토콜 설정값 초기화
	 * 
	 * @param cfg
	 * 
	 * @see IotClientConfig
	 */
	default void initProtocolCfg(IotClientConfig cfg) {
		if ("MQTT".equals(cfg.getProtocolType())) {
			cfg.initProtocolCfgFromProperties(new MqttProtocolConfig());
			return;
		} else {
			throw new IllegalArgumentException("Unsupported protocolType : " + cfg.getProtocolType());
		}
	}

}
