
package com.sds.iot.sdk;

import org.junit.Test;

import com.sds.iot.sdk.auth.mutual.MutualTlsAuth;
import com.sds.iot.sdk.protocol.RetryConfig;

/**
 * TLS 상호인증 테스트
 */
public class IotClientMutualTlsAuthTest {
	//	private static final Logger	LOGGER				= LoggerFactory.getLogger(IotClientTlsConfigTest.class);

	/* --- basic config */
	private String	siteId		= "CB00000000";
	private String	thingName	= "MD1.MUTUAL_TLS_123";
	private String	serverAddr	= "ssl://local.insator.io:8011"; //"tcp://test-brighticsiot.samsungsds.com:8001";
	/* --- basic config */

	@Test
	public void test_mutualTls() {

		MutualTlsAuth auth = new MutualTlsAuth();

		IotClientConfig cfg = new IotClientConfig(siteId, thingName);
		cfg.setServerAddr(serverAddr);
		cfg.setSslPropsFilePath("ssl_twoway_private.properties");

		IotClient client = new IotClient(auth, cfg);

		RetryConfig retryCfg = new RetryConfig();
		retryCfg.setConnectRetryMaxCount(2);
		retryCfg.setConnectRetryBaseDelay(2000);
		retryCfg.setConnectRetryDelayRandomRange(0);
		client.setRetryConfig(retryCfg);

		TestCommonUtil.testCommon(client);
	}

	@Test
	public void test_mutualTlsFileConfig() {

		MutualTlsAuth auth = new MutualTlsAuth();
		IotClientConfig cfg = new IotClientConfig(siteId, thingName, "biot_client.properties");
		IotClient client = new IotClient(auth, cfg);

		TestCommonUtil.testCommon(client);
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_mutualTlsError1() {

		MutualTlsAuth auth = new MutualTlsAuth();

		IotClientConfig cfg = new IotClientConfig(siteId, thingName);
		cfg.setServerAddr("tcp://localhost:8001"); // INVALID SERVER ADDRESS
		cfg.setSslPropsFilePath("");

		@SuppressWarnings("unused")
		IotClient client = new IotClient(auth, cfg);
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_mutualTlsError2() {

		MutualTlsAuth auth = new MutualTlsAuth();

		IotClientConfig cfg = new IotClientConfig(siteId, thingName);
		cfg.setServerAddr(serverAddr);
		cfg.setSslPropsFilePath("INVALID.properties"); // INVALID SSL PROPERTY

		@SuppressWarnings("unused")
		IotClient client = new IotClient(auth, cfg);
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_mutualTlsError3() {

		MutualTlsAuth auth = new MutualTlsAuth();

		IotClientConfig cfg = new IotClientConfig(siteId, thingName);
		cfg.setServerAddr(serverAddr);
		cfg.setSslPropsFilePath("ssl_oneway_private.properties"); // ONE WAY ONLY

		@SuppressWarnings("unused")
		IotClient client = new IotClient(auth, cfg);
	}
}
