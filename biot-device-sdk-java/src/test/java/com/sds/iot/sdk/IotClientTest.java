
package com.sds.iot.sdk;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.iot.sdk.auth.ita.ItaBasicAuthAuth;
import com.sds.iot.sdk.message.IotMessage;
import com.sds.iot.sdk.protocol.RetryConfig;
import com.sds.iot.sdk.protocol.mqtt.MqttProtocolConfig;

/**
 * 기본 테스트
 */
public class IotClientTest {
	private static final Logger	LOGGER			= LoggerFactory.getLogger(IotClientTest.class);

	/* --- basic config */
	private String				siteId			= "CB00000000";
	private String				thingName		= "MD1.123";
	private String				apiServerAddr	= "http://local.insator.io:8088"; //"https://test-brighticsiot.samsungsds.com:8088"
	/* --- basic config */
	
	private static final String BASIC_AUTH_CREDENTIAL = "Q0IwMDAwMDAwMCthcGlfdGVzdDphcGk=";

	@Test
	@Ignore
	@SuppressWarnings("unused")
	public void test_builder() throws Exception {

		// (샘플에 들어갈만한) 자주 쓸만한 빌더 형태 
		IotClient client1 = IotClient.thing(siteId, thingName).mutualTls().configFile("biot.properties");
		IotClient client2 = IotClient.thing(siteId, thingName).itaUserLogin("username", "password").configFile("biot.properties");
		IotClient client3 = IotClient.thing(siteId, thingName).itaBasicAuth("basicAuthCredential").configFile("biot.properties");

		// 기타 가능한 빌더 형태
		IotClient client4 = IotClient.thing(siteId, thingName).itaUserLogin("username", "password").apiServerAddr(apiServerAddr);
		IotClient client5 = IotClient.thing(siteId, thingName).itaBasicAuth("basicAuthCredential").apiServerAddr(apiServerAddr);

		// 오리지널 생성자 형태
		//IotClient c1 = new IotClient(auth, cfg);
		//IotClient c2 = new IotClient(auth, siteId, thingName, configFilePath);
		//IotClient c3 = new IotClient(auth, siteId, thingName, apiServerAddr, serverAddr);
	}
	
	@Test
	public void test() {
		LOGGER.info("========== test");

		ItaBasicAuthAuth auth = new ItaBasicAuthAuth(BASIC_AUTH_CREDENTIAL);
		IotClient client = new IotClient(auth, siteId, thingName, apiServerAddr, null);

		RetryConfig retryCfg = new RetryConfig();
		retryCfg.setConnectRetryMaxCount(2);
		retryCfg.setConnectRetryBaseDelay(2000);
		retryCfg.setConnectRetryDelayRandomRange(0);
		client.setRetryConfig(retryCfg);

		MqttProtocolConfig protocolCfg = new MqttProtocolConfig();
		client.setProtocolConfig(protocolCfg);

		TestCommonUtil.testCommon(client);
	}

	@Test
	public void test_fileConfig() {
		LOGGER.info("========== test_fileConfig");

		ItaBasicAuthAuth auth = new ItaBasicAuthAuth(BASIC_AUTH_CREDENTIAL);
		IotClientConfig cfg = new IotClientConfig(siteId, thingName, "biot_client.properties");
		IotClient client = new IotClient(auth, cfg);

		TestCommonUtil.testCommon(client);
	}

	@Test
	public void test_sendTooManyInvalidMsg() {
		LOGGER.info("========== test_sendTooManyInvalidMsg");

		ItaBasicAuthAuth auth = new ItaBasicAuthAuth(BASIC_AUTH_CREDENTIAL);
		IotClientConfig cfg = new IotClientConfig(siteId, thingName, "biot_client.properties");
		IotClient client = new IotClient(auth, cfg);

		// ==========================================================
		// CONNECT
		client.connect();
		// CALL 
		for (int i = 0; i < 20; i++) {
			IotMessage msg = new IotMessage();
			msg.setMsgCode("INVALID_MSG_" + i);

			client.send(msg);

			// WAIT 
			try {
				Thread.sleep(500L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		// WAIT 
		try {
			Thread.sleep(1000L * 10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// DISCONNECT
		client.disconnect();
	}

	@Test
	public void test_syncCallTimeout() {
		LOGGER.info("========== test_syncCallTimeout");

		ItaBasicAuthAuth auth = new ItaBasicAuthAuth(BASIC_AUTH_CREDENTIAL);
		IotClientConfig cfg = new IotClientConfig(siteId, thingName, "biot_client.properties");
		IotClient client = new IotClient(auth, cfg);

		// ==========================================================
		// CONNECT
		client.connect();
		// SYNC CALL 
		IotMessage msg = new IotMessage();
		msg.setMsgCode("INVALID_MSG");
		IotMessage msg2res = client.syncCall(msg, 1); // 1 sec. 
		if (msg2res == null) {
			LOGGER.debug("sync call no response...");
		} else {
			LOGGER.debug("response = {}", msg2res.toString());
		}
		// WAIT 
		try {
			Thread.sleep(1000L * 3);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// DISCONNECT
		client.disconnect();
	}

	@Test
	public void test_callTimeout() {
		LOGGER.info("========== test_callTimeout");

		ItaBasicAuthAuth auth = new ItaBasicAuthAuth(BASIC_AUTH_CREDENTIAL);
		IotClientConfig cfg = new IotClientConfig(siteId, thingName, "biot_client.properties");
		IotClient client = new IotClient(auth, cfg);

		// ==========================================================
		// CONNECT
		client.connect();

		// CALL
		IotMessage msg = new IotMessage();
		msg.setMsgCode("INVALID_MSG");

		TestResponseCallback cb = new TestResponseCallback();
		cb.setTimeout(1000); // 1 sec.
		client.call(msg, cb);

		// WAIT 
		try {
			Thread.sleep(1000L * 3);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// DISCONNECT
		client.disconnect();
	}

	@Test
	public void test_callTimeoutTooManyInvalidMsg() {
		LOGGER.info("========== test_callTimeoutTooManyInvalidMsg");

		ItaBasicAuthAuth auth = new ItaBasicAuthAuth(BASIC_AUTH_CREDENTIAL);
		IotClientConfig cfg = new IotClientConfig(siteId, thingName, "biot_client.properties");
		IotClient client = new IotClient(auth, cfg);

		// ==========================================================
		// CONNECT
		client.connect();

		// CALL 
		for (int i = 0; i < 20; i++) {
			IotMessage msg = new IotMessage();
			msg.setMsgCode("INVALID_MSG_" + i);

			TestResponseCallback cb = new TestResponseCallback();
			cb.setTimeout(1000); // 1 sec.

			client.call(msg, cb);
		}
		
		// WAIT 
		try {
			Thread.sleep(1000L * 10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// DISCONNECT
		client.disconnect();
	}

	@Test
	public void test_keepAlive() {
		LOGGER.info("========== test_keepAlive");

		ItaBasicAuthAuth auth = new ItaBasicAuthAuth(BASIC_AUTH_CREDENTIAL);
		IotClientConfig cfg = new IotClientConfig(siteId, thingName, "biot_client.properties");
		cfg.setDefaultSyncResponseTimeout(2);
		cfg.setKeepAliveInterval(3); // 3초마다 keep alive

		IotClient client = new IotClient(auth, cfg);

		// ==========================================================
		// CONNECT
		client.connect();

		// WAIT (1)  
		try {
			Thread.sleep(1000L * 2);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		IotMessage msg = new IotMessage();
		msg.setDataString("{ \"aa\" : 111 }");

		IotMessage resMsg = client.syncCall(msg, 3);
		if (resMsg == null) {
			LOGGER.debug("sync call no response...");
		} else {
			LOGGER.debug("response = {}", resMsg.toString());
		}

		// WAIT (2)
		try {
			Thread.sleep(1000L * 20);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// DISCONNECT
		client.disconnect();
	}

}
