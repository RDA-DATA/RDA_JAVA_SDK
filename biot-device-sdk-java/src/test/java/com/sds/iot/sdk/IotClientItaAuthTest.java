
package com.sds.iot.sdk;

import org.junit.Test;

import com.sds.iot.sdk.auth.ita.ItaBasicAuthAuth;
import com.sds.iot.sdk.auth.ita.ItaDirectAuth;
import com.sds.iot.sdk.auth.ita.ItaUserLoginAuth;
import com.sds.iot.sdk.exception.IotException;
import com.sds.iot.sdk.protocol.RetryConfig;

/**
 * ITA 인증 방식 별 테스트
 */
public class IotClientItaAuthTest {
	//	private static final Logger	LOGGER			= LoggerFactory.getLogger(IotClientConnectTest.class);

	/* --- basic config */
	private String	siteId			= "CB00000000";
	private String	thingName		= "MD1.123";
	private String	apiServerAddr	= "http://local.insator.io:8088"; //"https://test-brighticsiot.samsungsds.com:8088"
	private String	serverAddr		= "tcp://local.insator.io:8001"; // Direct 외의 ITA는 serverAddress 를 변경해주므로 오류 발생하지 않음
	/* --- basic config */

	private static final String BASIC_AUTH_CREDENTIAL = "Q0IwMDAwMDAwMCthcGlfdGVzdDphcGk=";
	
	@Test
	public void test1_ItaDirect() {

		ItaDirectAuth auth = new ItaDirectAuth("ec6aa3224f0fe11e");
		IotClient client = new IotClient(auth, siteId, thingName, apiServerAddr, serverAddr);

		TestCommonUtil.testCommon(client);
	}

	@Test(expected = IotException.class)
	public void test1_ItaDirectError1() {

		// MQTT 프로토콜 내, AUTH_CODE 오류 시에는 바로 익셉션 발생 후 종료
		ItaDirectAuth auth = new ItaDirectAuth("INVALID_AUTHCODE");
		IotClient client = new IotClient(auth, siteId, thingName, apiServerAddr, serverAddr);

		TestCommonUtil.testCommon(client);
	}

	@Test(expected = IotException.class)
	public void test1_ItaDirectError2() {

		// MQTT CONNECT 자체를 실패하는 경우에는 CONNECT RETRY 
		ItaDirectAuth auth = new ItaDirectAuth("ec6aa3224f0fe11e");
		IotClient client = new IotClient(auth, siteId, thingName, apiServerAddr, "tcp://INVALIDHOST");

		RetryConfig retryCfg = new RetryConfig();
		retryCfg.setConnectRetryMaxCount(2);
		retryCfg.setConnectRetryBaseDelay(2000);
		retryCfg.setConnectRetryDelayRandomRange(0);
		client.setRetryConfig(retryCfg);

		TestCommonUtil.testCommon(client);
	}

	@Test
	public void test2_ItaBasicAuth() {

		ItaBasicAuthAuth auth = new ItaBasicAuthAuth(BASIC_AUTH_CREDENTIAL);
		IotClient client = new IotClient(auth, siteId, thingName, apiServerAddr, null);

		TestCommonUtil.testCommon(client);
	}

	@Test(expected = IotException.class)
	public void test2_ItaBasicAuthError1() {

		// BASIC AUTH 오류 시에는 바로 익셉션 발생 후 종료
		ItaBasicAuthAuth auth = new ItaBasicAuthAuth("SU5WQUxJRDpCQVNJQ0FVVEhfQ1JFREVOVElBTA=="); //invalid basic auth credential
		IotClient client = new IotClient(auth, siteId, thingName, apiServerAddr, null);

		TestCommonUtil.testCommon(client);
	}

	@Test(expected = IotException.class)
	public void test2_ItaBasicAuthError2() {

		// API SERVER CONNECT 자체를 실패하는 경우에는 CONNECT RETRY
		ItaBasicAuthAuth auth = new ItaBasicAuthAuth(BASIC_AUTH_CREDENTIAL);
		IotClient client = new IotClient(auth, siteId, thingName, "http://INVALIDHOST:8088", null);

		RetryConfig retryCfg = new RetryConfig();
		retryCfg.setConnectRetryMaxCount(2);
		retryCfg.setConnectRetryBaseDelay(2000);
		retryCfg.setConnectRetryDelayRandomRange(0);
		client.setRetryConfig(retryCfg);

		TestCommonUtil.testCommon(client);
	}

	@Test
	public void test3_UserLogin() throws Exception {

		ItaUserLoginAuth auth = new ItaUserLoginAuth("insator", "ocplab1!");
		IotClient client = new IotClient(auth, siteId, thingName, apiServerAddr, null);

		TestCommonUtil.testCommon(client);
	}

	@Test(expected = IotException.class)
	public void test3_UserLoginError1() throws Exception {

		// USER LOGIN 오류 시에는 바로 익셉션 발생 후 종료
		ItaUserLoginAuth auth = new ItaUserLoginAuth("INVALID_USERID", "INVALID_PASSWD");
		IotClient client = new IotClient(auth, siteId, thingName, apiServerAddr, null);

		TestCommonUtil.testCommon(client);
	}

	@Test(expected = IotException.class)
	public void test3_UserLoginError2() throws Exception {

		// API SERVER CONNECT 자체를 실패하는 경우에는 CONNECT RETRY
		ItaUserLoginAuth auth = new ItaUserLoginAuth("insator", "ocplab1!");
		IotClient client = new IotClient(auth, siteId, thingName, "http://INVALIDHOST:8088", null);

		RetryConfig retryCfg = new RetryConfig();
		retryCfg.setConnectRetryMaxCount(2);
		retryCfg.setConnectRetryBaseDelay(2000);
		retryCfg.setConnectRetryDelayRandomRange(0);
		client.setRetryConfig(retryCfg);

		TestCommonUtil.testCommon(client);
	}

}
