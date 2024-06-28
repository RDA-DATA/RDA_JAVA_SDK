
package com.sds.iot.sdk;

import org.junit.Assert;
import org.junit.Test;

import com.sds.iot.sdk.auth.ita.ItaBasicAuthAuth;

/**
 * <li>사물 활성화</li>
 * <li>리프사물 등록</li>
 * <li>사물 데이터 전송</li>
 * <li>리프사물 데이터 전송</li>
 * 기능 테스트
 */
public class IotClientPredefinedService1Test {
	//private static final Logger	LOGGER			= LoggerFactory.getLogger(IotClientPredefinedService1Test.class);

	/* --- basic config */
	private String				siteId			= "CB00000000";
	private String				thingName		= "MD1.123";
	private String				configFilePath	= "biot_client.properties";
	/* --- basic config */

	private static final String BASIC_AUTH_CREDENTIAL = "Q0IwMDAwMDAwMCthcGlfdGVzdDphcGk=";
	
	/**
	 * 사물 활성화
	 */
	@Test
	public void test_activateThing() {

		ItaBasicAuthAuth auth = new ItaBasicAuthAuth(BASIC_AUTH_CREDENTIAL);
		IotClient client = new IotClient(auth, siteId, thingName, configFilePath);
		client.setCustomMessageListener(new DefaultMessageListener());

		// ==========================================================
		// CONNECT
		client.connect();

		// SERVICE
		int result = client.activateThing("MD1", "123", "NEW_NICK2");
		Assert.assertTrue(result >= 200 && result < 300);

		// WAIT 
		try {
			Thread.sleep(1000L * 2);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// DISCONNECT
		client.disconnect();
	}

	/**
	 * 리프사물 등록
	 */
	@Test
	public void test_registerLeaf() {

		ItaBasicAuthAuth auth = new ItaBasicAuthAuth(BASIC_AUTH_CREDENTIAL);
		IotClient client = new IotClient(auth, siteId, thingName, configFilePath);
		client.setCustomMessageListener(new DefaultMessageListener());

		// ==========================================================
		// CONNECT
		client.connect();

		// SERVICE
		int result = client.registerLeafThing(thingName, "MD1", "LEAF_12345", "nicknick");
		Assert.assertTrue(result >= 200 && result < 300);

		// WAIT 
		try {
			Thread.sleep(1000L * 2);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// DISCONNECT
		client.disconnect();
	}

	/**
	 * 사물 데이터 전송
	 */
	@Test
	public void test_sendAttributes() {

		ItaBasicAuthAuth auth = new ItaBasicAuthAuth(BASIC_AUTH_CREDENTIAL);
		IotClient client = new IotClient(auth, siteId, thingName, configFilePath);
		client.setCustomMessageListener(new DefaultMessageListener());

		// ==========================================================
		// CONNECT
		client.connect();

		// SERVICE
		client.sendAttributes(null, "{ \"aa\" : 555 }");

		// WAIT 
		try {
			Thread.sleep(1000L * 2);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// DISCONNECT
		client.disconnect();
	}

	/**
	 * 리프사물 데이터 전송
	 */
	@Test
	public void test_sendAttributesLeaf() {

		ItaBasicAuthAuth auth = new ItaBasicAuthAuth(BASIC_AUTH_CREDENTIAL);
		IotClient client = new IotClient(auth, siteId, thingName, configFilePath);
		client.setCustomMessageListener(new DefaultMessageListener());

		// ==========================================================
		// CONNECT
		client.connect();

		// SERVICE
		client.sendAttributesForLeaf("MD1.LEAF_12345", null, "{ \"aa\" : 555 }");

		// WAIT 
		try {
			Thread.sleep(1000L * 2);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// DISCONNECT
		client.disconnect();
	}
}
