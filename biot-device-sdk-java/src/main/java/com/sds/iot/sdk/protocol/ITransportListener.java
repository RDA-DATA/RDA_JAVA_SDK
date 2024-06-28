
package com.sds.iot.sdk.protocol;

import com.sds.iot.sdk.message.IotMessage;

/**
 * 이 클래스는 각 프로토콜 클라이언트(MQTT)에서 연결 및 메시지 상태 업데이트를 통신하기위한 콜백 인터페이스입니다.
 * 
 * @author SDS
 *
 */
public interface ITransportListener {

	/**
	 * Callback to be fired when a transport message has been received.
	 * 
	 * @param msg
	 *        The message that was received
	 */
	void onMessageReceived(IotMessage msg);

	/**
	 * Callback to be fired when a message that the transport client sent has been acknowledged by Iot Hub
	 * 
	 * @param e
	 *        Null if the message was successfully acknowledged. Otherwise, this exception communicates if the message
	 *        should be resent at all
	 */
	void onMessageSent(Throwable e);

	/**
	 * Callback to be fired when connection has been lost
	 * 
	 * @param cause
	 *        the cause of the connection loss
	 */
	void onConnectionLost(String cause);

	//    /**
	//     * Callback to be fired when the connection has been successfully established
	//     * @param connectionId the id of the connection this update is relevant to
	//     */
	//    void onConnectionEstablished(String connectionId);
}
