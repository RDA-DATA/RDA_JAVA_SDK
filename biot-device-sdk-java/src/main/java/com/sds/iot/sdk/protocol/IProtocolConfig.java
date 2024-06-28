
package com.sds.iot.sdk.protocol;

import java.util.Properties;

/**
 * 이 인터페이스 클래스는 프로토콜의 기본값을 제공합니다.
 * 
 * @author SDS
 */
public interface IProtocolConfig {

	/**
	 * 설정 파일(*.properties)로 프로토콜 정보를 초기화합니다.
	 * @param props
	 *        설정 파일 객체
	 * @see Properties
	 */
	void initFromProperties(Properties props);
	
}
