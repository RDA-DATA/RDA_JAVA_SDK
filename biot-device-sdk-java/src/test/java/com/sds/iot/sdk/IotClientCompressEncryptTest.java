
package com.sds.iot.sdk;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.iot.sdk.auth.ita.ItaBasicAuthAuth;
import com.sds.iot.sdk.auth.mutual.MutualTlsAuth;

/**
 * 압축/암호화 기능 테스트
 */
public class IotClientCompressEncryptTest {
	private static final Logger	LOGGER			= LoggerFactory.getLogger(IotClientCompressEncryptTest.class);

	/* --- basic config */
	private String				siteId			= "CB00000000";
	private String				thingName		= "MD1.123";
	private String				apiServerAddr	= "http://local.insator.io:8088"; //"https://test-brighticsiot.samsungsds.com:8088"

	private String				thingNameTls	= "MD1.MUTUAL_TLS_123";
	private String				serverAddrTls	= "ssl://local.insator.io:8011";
	/* --- basic config */
	
	private static final String BASIC_AUTH_CREDENTIAL = "Q0IwMDAwMDAwMCthcGlfdGVzdDphcGk=";

	@Test
	public void testPlain() {
		LOGGER.info("========== testPlain (NONE)");

		ItaBasicAuthAuth auth = new ItaBasicAuthAuth(BASIC_AUTH_CREDENTIAL);

		IotClientConfig cfg = new IotClientConfig(siteId, thingName);
		cfg.setApiServerAddr(apiServerAddr);
		cfg.setHeaderType("D");
		cfg.setCompressUseBytes(0);
		cfg.setEncType("0");

		IotClient client = new IotClient(auth, cfg);

		TestCommonUtil.testCommon(client);
	}

	@Test(expected = Exception.class)
	public void testError_config() {
		LOGGER.info("========== testError_config");

		// mutual tls 인증과 ita_aes128 암복호화 설정 ==> 오류 발생
		MutualTlsAuth auth = new MutualTlsAuth();

		IotClientConfig cfg = new IotClientConfig(siteId, thingNameTls);
		cfg.setSslPropsFilePath("ssl_twoway_private.properties");
		cfg.setServerAddr(serverAddrTls);
		cfg.setHeaderType("D");
		cfg.setCompressUseBytes(0);
		cfg.setEncType("3");

		IotClient client = new IotClient(auth, cfg);

		TestCommonUtil.testCommon(client);
	}

	@Test
	public void testEncrypt_3() {
		LOGGER.info("========== testEncrypt_3 (ITA_AES128)");

		ItaBasicAuthAuth auth = new ItaBasicAuthAuth(BASIC_AUTH_CREDENTIAL);

		IotClientConfig cfg = new IotClientConfig(siteId, thingName);
		cfg.setApiServerAddr(apiServerAddr);
		cfg.setHeaderType("D");
		cfg.setCompressUseBytes(0);
		cfg.setEncType("3");

		IotClient client = new IotClient(auth, cfg);

		TestCommonUtil.testCommon(client);
	}

	@Test
	public void testCompress_Z0() {
		LOGGER.info("========== testCompress_Z0");

		ItaBasicAuthAuth auth = new ItaBasicAuthAuth(BASIC_AUTH_CREDENTIAL);

		IotClientConfig cfg = new IotClientConfig(siteId, thingName);
		cfg.setApiServerAddr(apiServerAddr);
		cfg.setHeaderType("D");
		cfg.setCompressUseBytes(1);
		cfg.setEncType("0");

		IotClient client = new IotClient(auth, cfg);

		TestCommonUtil.testCommon(client);
	}

	@Test
	public void testCompressAndEncrypt_Z3() {
		LOGGER.info("========== testCompressAndEncrypt_Z3");

		ItaBasicAuthAuth auth = new ItaBasicAuthAuth(BASIC_AUTH_CREDENTIAL);

		IotClientConfig cfg = new IotClientConfig(siteId, thingName);
		cfg.setApiServerAddr(apiServerAddr);
		cfg.setHeaderType("D");
		cfg.setCompressUseBytes(10);
		cfg.setEncType("3");

		IotClient client = new IotClient(auth, cfg);

		TestCommonUtil.testCommon(client);
	}

}
