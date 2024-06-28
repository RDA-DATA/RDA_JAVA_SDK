
package com.sds.iot.sdk.protocol;

import com.sds.iot.sdk.IotClientConfig;
import com.sds.iot.sdk.auth.IAuth;
import com.sds.iot.sdk.message.IotMessage;
import com.sds.iot.sdk.message.MessageConverter;

/**
 * 이 인터페이스 클래스는 서버와의 통신 기능을 제공하기 위한 기본 구현체입니다.
 * 
 * @author SDS
 */
public interface IProtocol {

	/**
	 * 연결 시도 전에 프로토콜 정보 초기화 작업 수행.
	 * 단, 서버와의 연결이 끊긴 경우 연결 재시도 전에도 한 번 수행됨.
	 * 따라서 서버와의 연결이 끊긴 횟수만큼 여러 번 초기화 작업이 수행될 수 있음.
	 * 
	 * @param cfg
	 *        클라이언트 설정값
	 * @param converter
	 *        MessageConverter
	 * @param transport
	 *        TransportListener
	 * @param auth
	 *        Auth
	 * 
	 * @see IotClientConfig
	 * @see MessageConverter
	 * @see ITransportListener
	 * @see IAuth
	 */
	void init(IotClientConfig cfg, MessageConverter converter, ITransportListener transport, IAuth auth);

	/**
	 * 서버로 연결
	 */
	void connect();

	/**
	 * 서버와의 연결 종료
	 */
	void disconnect();

	/**
	 * 서버로 메시지 전송
	 * 
	 * @param msg
	 *        메시지
	 * @throws Exception
	 * 
	 * @see IotMessage
	 */
	void send(IotMessage msg) throws Exception;

	/**
	 * 서버와 연결 여부. 단, 인증이 완료되지 않아도 connected 상태는 true 일 수 있음.
	 * 
	 * @return 서버와의 연결 여부 (true/false)
	 */
	boolean isConnected();
}
