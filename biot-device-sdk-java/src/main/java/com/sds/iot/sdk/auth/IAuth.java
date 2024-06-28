
package com.sds.iot.sdk.auth;

import com.sds.iot.sdk.IotClientConfig;
import com.sds.iot.sdk.exception.IotException;
import com.sds.iot.sdk.message.IotMessage;

/**
 * 이 인터페이스 클래스는 클라이언트에 대한 인증을 제공하기 위한 기본 구현체입니다.
 * 
 * @author SDS
 */
public interface IAuth {

	/**
	 * @return 인증 타입
	 */
	String getAuthTypeName();

	/**
	 * @throws IotException the exception thrown if the authentication operation fails
	 */
	void requestCredential() throws IotException;

	/**
	 * @return authCode
	 */
	String getCredential();

	/**
	 * @param cfg B.IoT 연결을 위한 기본 설정값을 가지고 있는 IotClientConfig 값
	 * 
	 * @see IotClientConfig
	 */
	void setConfig(IotClientConfig cfg);

	/**
	 * 서버로 송신할 디바이스 인증 요청 메시지를 생성하여 리턴
	 * 
	 * @param seq=1
	 *        부터 시작하며, 인증 프로세스에 더 이상 메시지 송신이 필요 없으면 null 리턴
	 * @return 디바이스 인증 메시지
	 */
	IotMessage makeDeviceAuthMessage(int seq);

	/**
	 * 서버에서 디바이스 인증 응답 메시지를 수신했을 때 수행할 액션
	 * 
	 * @param resMsg
	 *        응답 메시지
	 */
	void onAuthResponseMessageReceived(IotMessage resMsg);

	/**
	 * @return 인증 토큰
	 */
	String getAuthToken();

}
