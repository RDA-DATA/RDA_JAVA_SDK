
package com.sds.iot.sdk.callback;

import com.sds.iot.sdk.message.IotMessage;

/**
 * 이 클래스는 콜백 시 아무작업도 하지 않을 경우 호출되는 함수를 정의합니다.
 * 
 * @author SDS
 */
public class NoActionCallback extends AbstractCallback {

	@Override
	public void action(IotMessage resMsg) throws Exception {
		/* NO ACTION */

	}

	@Override
	public void timeoutAction() {
		/* NO ACTION */

	}

}
