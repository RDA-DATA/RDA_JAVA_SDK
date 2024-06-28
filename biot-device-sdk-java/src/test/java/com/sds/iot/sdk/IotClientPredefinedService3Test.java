
package com.sds.iot.sdk;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.iot.sdk.auth.ita.ItaBasicAuthAuth;
import com.sds.iot.sdk.message.IotMessage;
import com.sds.iot.sdk.protocol.http.SimpleHttpClient;

/**
 * <li>프로비저닝 메시지 수신</li>
 * <li>각종 notify, notiRequest 메시지 수신</li>
 * 기능 테스트
 */
public class IotClientPredefinedService3Test {
	private static final Logger	LOGGER					= LoggerFactory.getLogger(IotClientPredefinedService3Test.class);

	/* --- basic config */
	private String				siteId					= "CB00000000";
	private String				thingName				= "MD1.123";
	private String				configFilePath			= "biot_client.properties";
	/* --- basic config */

	private static final String BASIC_AUTH_CREDENTIAL = "Q0IwMDAwMDAwMCthcGlfdGVzdDphcGk=";
	
	private static final String	API_SERVER_ADDR			= "http://local.insator.io:8088";

	private static final String	URI1_PROVISION			= "/v1.0/customers/{customerId}/messageCode/{messageCode}/notify?mep=queueing";
	private static final String	URI2_NOTIREQUEST		= "/v1.0/site/{siteId}/thingName/{thingName}/notiRequest";
	private static final String	URI3_NOTIFY				= "/v1.0/site/{siteId}/thingName/{thingName}/notify?mep=queueing";
	private static final String	URI4_MULTINOTIFY		= "/site/{siteId}/multiNotify";
	private static final String	URI5_OLD_NOTIREQUEST	= "/site/{siteId}/tp/{thingName}/notiRequest";
	private static final String	URI6_OLD_NOTIFY			= "/site/{siteId}/tp/{thingName}/notify?mep=queueing";

	private class SuccessFlag {
		private boolean flag = false;

		public void setSuccess() {
			flag = true;
		}

		public boolean isSuccess() {
			return flag;
		}
	}

	private class MyMessageListener extends DefaultMessageListener {
		private final SuccessFlag	provisionFlag;
		private final SuccessFlag	notificationFlag;

		MyMessageListener(SuccessFlag provisionFlag, SuccessFlag notificationFlag) {
			this.provisionFlag = provisionFlag;
			this.notificationFlag = notificationFlag;
		}

		@Override
		protected void processProvisionMessage(IotMessage msg) {
			LOGGER.info("=====>>>>> processProvisionMessage");
			provisionFlag.setSuccess();
			super.processProvisionMessage(msg);
		}

		@Override
		protected void processNotifyMessage(IotMessage msg) {
			LOGGER.info("=====>>>>> processNotifyMessage");
			notificationFlag.setSuccess();
			super.processNotifyMessage(msg);
		}
	}

	/**
	 * 프로비저닝 메시지 수신
	 */
	@Test
	public void test1_provisioning() {
		ItaBasicAuthAuth auth = new ItaBasicAuthAuth(BASIC_AUTH_CREDENTIAL);
		IotClient client = new IotClient(auth, siteId, thingName, configFilePath);
		SuccessFlag flag = new SuccessFlag();
		client.setCustomMessageListener(new MyMessageListener(flag, null));

		// ==========================================================
		// CONNECT
		client.connect();

		// WAIT 
		try {
			Thread.sleep(1000L * 2);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// 서버에서 클라이언트로 메시지를 전송하도록 서버의 API 호출
		request_provisioning();

		// WAIT 
		for (int i = 0; i < 10; i++) {
			if (flag.isSuccess()) {
				break;
			}
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// DISCONNECT
		client.disconnect();

		Assert.assertTrue(flag.isSuccess());
	}

	@Test
	public void test2_notiRequest() {
		ItaBasicAuthAuth auth = new ItaBasicAuthAuth(BASIC_AUTH_CREDENTIAL);
		IotClient client = new IotClient(auth, siteId, thingName, configFilePath);
		SuccessFlag flag = new SuccessFlag();
		client.setCustomMessageListener(new MyMessageListener(null, flag));

		// ==========================================================
		// CONNECT
		client.connect();

		// WAIT 
		try {
			Thread.sleep(1000L * 2);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// 서버에서 클라이언트로 메시지를 전송하도록 서버의 API 호출
		request_notify(URI2_NOTIREQUEST);

		// WAIT 
		for (int i = 0; i < 10; i++) {
			if (flag.isSuccess()) {
				break;
			}
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// DISCONNECT
		client.disconnect();

		Assert.assertTrue(flag.isSuccess());
	}

	@Test
	public void test3_notify() {
		ItaBasicAuthAuth auth = new ItaBasicAuthAuth(BASIC_AUTH_CREDENTIAL);
		IotClient client = new IotClient(auth, siteId, thingName, configFilePath);
		SuccessFlag flag = new SuccessFlag();
		client.setCustomMessageListener(new MyMessageListener(null, flag));

		// ==========================================================
		// CONNECT
		client.connect();

		// WAIT 
		try {
			Thread.sleep(1000L * 2);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// 서버에서 클라이언트로 메시지를 전송하도록 서버의 API 호출
		request_notify(URI3_NOTIFY);

		// WAIT 
		for (int i = 0; i < 10; i++) {
			if (flag.isSuccess()) {
				break;
			}
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// DISCONNECT
		client.disconnect();

		Assert.assertTrue(flag.isSuccess());
	}

	@Test
	public void test4_multiNotify() {
		ItaBasicAuthAuth auth = new ItaBasicAuthAuth(BASIC_AUTH_CREDENTIAL);
		IotClient client = new IotClient(auth, siteId, thingName, configFilePath);
		SuccessFlag flag = new SuccessFlag();
		client.setCustomMessageListener(new MyMessageListener(null, flag));

		// ==========================================================
		// CONNECT
		client.connect();

		// WAIT 
		try {
			Thread.sleep(1000L * 2);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// 서버에서 프로비저닝 메시지 전송
		request_multiNotify(URI4_MULTINOTIFY);

		// WAIT 
		for (int i = 0; i < 10; i++) {
			if (flag.isSuccess()) {
				break;
			}
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// DISCONNECT
		client.disconnect();

		Assert.assertTrue(flag.isSuccess());
	}

	@Test
	public void test5_oldNotiRequest() {
		ItaBasicAuthAuth auth = new ItaBasicAuthAuth(BASIC_AUTH_CREDENTIAL);
		IotClient client = new IotClient(auth, siteId, thingName, configFilePath);
		SuccessFlag flag = new SuccessFlag();
		client.setCustomMessageListener(new MyMessageListener(null, flag));

		// ==========================================================
		// CONNECT
		client.connect();

		// WAIT 
		try {
			Thread.sleep(1000L * 2);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// 서버에서 프로비저닝 메시지 전송
		request_notify(URI5_OLD_NOTIREQUEST);

		// WAIT 
		for (int i = 0; i < 10; i++) {
			if (flag.isSuccess()) {
				break;
			}
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// DISCONNECT
		client.disconnect();

		Assert.assertTrue(flag.isSuccess());
	}

	@Test
	public void test6_oldNotify() {
		ItaBasicAuthAuth auth = new ItaBasicAuthAuth(BASIC_AUTH_CREDENTIAL);
		IotClient client = new IotClient(auth, siteId, thingName, configFilePath);
		SuccessFlag flag = new SuccessFlag();
		client.setCustomMessageListener(new MyMessageListener(null, flag));

		// ==========================================================
		// CONNECT
		client.connect();

		// WAIT 
		try {
			Thread.sleep(1000L * 2);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// 서버에서 프로비저닝 메시지 전송
		request_notify(URI6_OLD_NOTIFY);

		// WAIT 
		for (int i = 0; i < 10; i++) {
			if (flag.isSuccess()) {
				break;
			}
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// DISCONNECT
		client.disconnect();

		Assert.assertTrue(flag.isSuccess());
	}

	private void request_provisioning() {
		SimpleHttpClient httpClient = new SimpleHttpClient();
		Map<String, Object> header = new HashMap<>();
		header.put("Authorization", "Basic Q0IwMDAwMDAwMCthcGlfdGVzdDphcGk=");
		header.put("Content-Type", "application/json");

		// API 호출
		String uri = URI1_PROVISION;
		uri = uri.replace("{customerId}", "CB00000000");
		uri = uri.replace("{messageCode}", "TMS000000006");
		
		String ReqBody = "{\n"
				+ "\"attrs\":[{\"attrKey\":\"aa\",\"attrValue\":\"1\",\"attrType\":\"Int\"},{\"attrKey\":\"bb\",\"attrValue\":\"2\",\"attrType\":\"Double\"}],\n"
				+ "\"things\":[{\"topThingName\":\"MD1.123\",\"thingName\":\"MD1.123\",\"siteId\":\"CB00000000\"}],\n"
				+ "\"messageCode\":\"TMS000000006\",\n" + "\"userMessageCode\":\"Basic-Provisioning\"\n" + "}";

		httpClient.request("POST", API_SERVER_ADDR + uri, ReqBody.getBytes(Charset.defaultCharset()), header);
	}

	private void request_notify(String uri) {
		SimpleHttpClient httpClient = new SimpleHttpClient();
		Map<String, Object> header = new HashMap<>();
		header.put("Authorization", "Basic Q0IwMDAwMDAwMCthcGlfdGVzdDphcGk=");
		header.put("Content-Type", "application/json");

		// API 호출
		uri = uri.replace("{siteId}", siteId);
		uri = uri.replace("{thingName}", thingName);
		
		String ReqBody = "{ \"contents\" : \"GOOD!\" }";

		httpClient.request("POST", API_SERVER_ADDR + uri, ReqBody.getBytes(Charset.defaultCharset()), header);
	}
	
	private void request_multiNotify(String uri) {
		SimpleHttpClient httpClient = new SimpleHttpClient();
		Map<String, Object> header = new HashMap<>();
		header.put("Authorization", "Basic Q0IwMDAwMDAwMCthcGlfdGVzdDphcGk=");
		header.put("Content-Type", "application/json");

		// API 호출
		uri = uri.replace("{siteId}", siteId);
		uri = uri.replace("{thingName}", thingName);
		
		String ReqBody = "{"
				+ "\"thingList\" : [ \""+ thingName +"\" ], "
				+ "\"messageBody\" : { \"content\" : \"MULTI!!!!!\" } "
				+ "}";

		httpClient.request("POST", API_SERVER_ADDR + uri, ReqBody.getBytes(Charset.defaultCharset()), header);
		
		// 참고) 2.8 에서는 json object 형태({})의 messageBody 만 송신 가능하며 다른 json string 등의 형태는 송신 불가함. 
//		String ReqBody2 = "{"
//				+ "\"thingList\" : [ \""+ thingName +"\" ], "
//				+ "\"messageBody\" :  \"TEXT_DATA\" "
//				+ "}";
//		httpClient.request("POST", API_SERVER_ADDR + uri, ReqBody2.getBytes(Charset.defaultCharset()), header);

	}
}
