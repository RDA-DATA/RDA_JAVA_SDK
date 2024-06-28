
package com.sds.iot.sdk;

import com.sds.iot.sdk.message.IotMessage;

/**
 * 이 인터페이스를 구현하여 필요한 동작을 구현하도록 합니다.
 * 
 * @author SDS
 */
public interface ICustomMessageListener {

	/**
	 * @param client
	 *        IotCoreClient
	 */
	void setClient(IotCoreClient client);

	/**
	 * @param msg
	 *        메시지
	 */
	void onNotificationReceived(IotMessage msg);

	/**
	 * @param msg
	 *        메시지
	 */
	void onRequestReceived(IotMessage msg);

}
