
package com.sds.iot.sdk.callback;

import com.sds.iot.sdk.message.IotMessage;

/**
 * 이 인터페이스 클래스는 응답 콜백에서 수행할 액션을 제공하기 위한 기본 구현체입니다.
 * 
 * @author SDS
 */
public interface IResponseCallback {

	/**
	 * 응답 메시지 수신 시 콜백 수행
	 * @param resMsg
	 * @throws Exception
	 */
	void action(IotMessage resMsg) throws Exception;

	/**
	 * 지정한 시간 이내에 응답 또는 ACK가 들어오지 않은 경우 수행
	 */
	void timeoutAction();

	/**
	 * 해당 콜백 인스턴스가 생성된 시각. 이 시각을 기준으로 타임아웃 처리가 된다.
	 * @return
	 */
	long getCreateTime();

	/**
	 * 해당 콜백 인스턴스가 유지되는 시간
	 * @return
	 */
	long getTimeout();

	/**
	 * 해당 콜백 인스턴스가 유지되는 시간
	 * @param timeoutMillis
	 *        (milliseconds)
	 */
	void setTimeout(long timeoutMillis);

}
