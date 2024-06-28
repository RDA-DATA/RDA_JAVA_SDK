package com.sample.app;

import com.sds.iot.sdk.IotClientConfig;
import com.sds.iot.sdk.auth.ita.ItaUserLoginAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.iot.sdk.DefaultMessageListener;
import com.sds.iot.sdk.IotClient;
import com.sds.iot.sdk.message.IotMessage;

/**
 * Sample App (Notification)
 * IoT 서버에서 오는 Request 혹은 Notify 메시지를 처리하는 예제
 *
 */
public class AppNotification {
	public static void main(String[] args) {
		try {
			// Setup the thing information
			String userId = "test@samsung.com";
			String userPw = "test1!";
			String siteId = "CB00000000";
			String thingName = "TestModelITA.030";

			IotClient client = IotClient.thing(siteId, thingName).itaUserLogin(userId, userPw)
					.configFile("biot_client.properties");

			client.setCustomMessageListener(new NotificationListener());

			client.connect();

		} catch (Exception e) {
			// You can exception handling
			 e.printStackTrace();
		}
	}
}

class NotificationListener extends DefaultMessageListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(AppNotification.class);

	@Override
	public void processNotifyMessage(IotMessage msg) {
		LOGGER.info("[NotificationListener] processNotifyMessage");

		// 서비스 포탈의 Open API를 이용하여 데이터를 디바이스로 보낼 때 (Development - Open API 상세 - Open API Thing Control - NotiRequest)
		// 이 함수를 통하여 처리 될 수 있다.
		// DO SOMETHING
		// ...

		if ("Q".equals(msg.getMsgType())) {
			// RESPONSE TO SERVER.
			LOGGER.info("[NotificationListener] response to server");
			IotMessage resMsg = msg.createResponse();
			resMsg.setData("{\"test\":\"ok\"}".getBytes());
			getClient().send(resMsg);
		}
		else if ("N".equals(msg.getMsgType())){
			LOGGER.info("[NotificationListener] Notify message");

			// DO SOMETHING
			// ...
		}
	}

	@Override
	public void processProvisionMessage(IotMessage msg) {
		LOGGER.info("[NotificationListener] processProvisionMessage");

		// 서비스 포탈의 사물 정보에서 전송 버튼을 통하여 데이터를 디바이스로 보낼 때 (Thing - 사물 관리 - 사물 정보 - 메시지 코드- 전송)
		// 이 함수를 통하여 처리 될 수 있다.
		// 따라서, 어떤 특별한 상황이나 특정 데이터에 따라 커스텀 비지니스 로직을 추가하고 싶을 때
		// 이 함수를 오버라이딩하여 처리한다.
		// DO SOMETHING
		// ...

		if ("Q".equals(msg.getMsgType())) {
			// RESPONSE TO SERVER.
			LOGGER.info("[NotificationListener] response to server");
			IotMessage resMsg = msg.createResponse();
			getClient().send(resMsg);
		}
	}
}