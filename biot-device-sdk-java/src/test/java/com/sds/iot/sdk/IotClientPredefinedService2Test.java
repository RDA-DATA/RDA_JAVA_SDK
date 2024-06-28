
package com.sds.iot.sdk;

import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.iot.sdk.auth.ita.ItaBasicAuthAuth;
import com.sds.iot.sdk.protocol.http.SimpleHttpClient;
import com.sds.iot.sdk.util.JsonUtil;

/**
 * <li>능동형 펌웨어 최신 버전 요청 & 펌웨어 싸이닝 데이터 요청</li>
 * <li>펌웨어 업그레이드 요청 수신 & 펌웨어 업그레이드 완료 전송</li>
 * <li>파일 업로드 URI 요청 & 파일 업로드 완료 전송</li>
 * 기능 테스트
 */
public class IotClientPredefinedService2Test {
	private static final Logger	LOGGER			= LoggerFactory.getLogger(IotClientPredefinedService2Test.class);

	/* --- basic config */
	private String				siteId			= "CB00000000";
	private String				thingName		= "MD1.123";
	private String				configFilePath	= "biot_client.properties";
	/* --- basic config */

	private static final String BASIC_AUTH_CREDENTIAL = "Q0IwMDAwMDAwMCthcGlfdGVzdDphcGk=";
	
	/**
	 * 능동형 펌웨어 최신 버전 요청 & 펌웨어 싸이닝 데이터 요청
	 */
	@Test
	public void test_requestFirmwareLatestVersion() {

		ItaBasicAuthAuth auth = new ItaBasicAuthAuth(BASIC_AUTH_CREDENTIAL);
		IotClient client = new IotClient(auth, siteId, thingName, configFilePath);
		client.setCustomMessageListener(new DefaultMessageListener());

		// ==========================================================
		// CONNECT
		client.connect();

		// 능동형 펌웨어 최신 버전 요청
		Map<String, String> firmwareInfo = client.requestFirmwareLatestVersion("MD1", "FWT");
		LOGGER.info("firmwareInfo : {}", firmwareInfo);
		Assert.assertNotNull(firmwareInfo);

		String fileUri = firmwareInfo.get("fileUri");

		// 파일 다운로드
		downloadFile(fileUri, "target/download-test-file1.txt");

		// 펌웨어 싸이닝 데이터 요청
		String signData = client.requestFirmwareSignData(fileUri);
		LOGGER.info("signData : {}", signData);
		// 펌웨어 싸이닝 데이터는 없을 수 있음....
		//Assert.assertNotNull(signData);

		// WAIT 
		try {
			Thread.sleep(1000L * 2);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// DISCONNECT
		client.disconnect();
	}

	boolean FW_UPGRADE_SUCCESS = false;

	/**
	 * 수동형 펌웨어 업그레이드 & 펌웨어 업그레이드 완료 전송
	 */
	@Test
	public void test_requestFirmwareVersion() {

		ItaBasicAuthAuth auth = new ItaBasicAuthAuth(BASIC_AUTH_CREDENTIAL);
		IotClient client = new IotClient(auth, siteId, thingName, configFilePath);
		client.setCustomMessageListener(new MyMessageListener2());

		// ==========================================================
		// CONNECT
		client.connect();

		// WAIT 
		try {
			Thread.sleep(1000L * 2);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// (필요시) 펌웨어 업그레이드 작업 등록
		registerFirmwareUpgradeJob();

		// WAIT 
		for (int i = 0; i < 30; i++) {
			if (FW_UPGRADE_SUCCESS) {
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

		Assert.assertTrue(FW_UPGRADE_SUCCESS);
	}

	class MyMessageListener2 extends DefaultMessageListener {

		@Override
		protected void upgradeFirmware(String downloadUri, String firmwareType, String version) {
			LOGGER.info("=====>>>>> [MyMessageListener2] upgradeFirmware");
			LOGGER.info("=====>>>>> [MyMessageListener2] TYPE={}, VERSION={} ", firmwareType, version);
			LOGGER.info("=====>>>>> [MyMessageListener2] DOWNLOAD : GET {} ", downloadUri);

			// 파일 다운로드
			downloadFile(downloadUri, "target/download-test-file2.txt");

			FW_UPGRADE_SUCCESS = true;
		}
	}

	private static void registerFirmwareUpgradeJob() {
		SimpleHttpClient httpClient = new SimpleHttpClient();
		Map<String, Object> header = new HashMap<>();
		header.put("Authorization", "Basic Q0IwMDAwMDAwMCthcGlfdGVzdDphcGk=");
		header.put("Content-Type", "application/json");

		// 펌웨어 업그레이드 조회
		String fwJobUrl1 = "http://local.insator.io:8088/v1.0/customers/CB00000000/firmwares/things?search=upgradeStateCode:STB,firmwareTypeKey:%22FWT%22,thingModelName:%22MD1%22&offset=0&limit=1";
		byte[] resBytes = httpClient.request("GET", fwJobUrl1, null, header);
		Map<String, Object> result = JsonUtil.fromJsonToMap(resBytes);
		@SuppressWarnings("unchecked")
		Map<String, Object> resultPage = (Map<String, Object>) result.get("pagination");
		double total = (Double) resultPage.get("total");
		if (total > 0) {
			return;
		}

		// 펌웨어 즉시 업그레이드 작업 등록
		String fwJobUrl2 = "http://local.insator.io:8088/v1.0/customers/CB00000000/firmwareTypes/FWT/1.1/firmwares/1409_bdc41b21-8bba-4b8c-a320-5f5a4465ac76/applies";
		String fwJobReqBody2 = "{\"firmwareApplies\":[{\"siteId\":\"CB00000000\",\"thingName\":\"MD1.123\",\"thingModelId\":\"M00005\",\"upgradeStartDatetime\":\"2020-01-09T00:00:00.000Z\",\"upgradeModeCode\":\"FOR\"}]}";
		httpClient.request("POST", fwJobUrl2, fwJobReqBody2.getBytes(Charset.defaultCharset()), header);

	}

	/**
	 * 파일 업로드 URI 요청 & 파일 업로드 완료 전송
	 */
	@Test
	public void test_requestFileUploadUri() {
		ItaBasicAuthAuth auth = new ItaBasicAuthAuth(BASIC_AUTH_CREDENTIAL);
		IotClient client = new IotClient(auth, siteId, thingName, configFilePath);
		client.setCustomMessageListener(new DefaultMessageListener());

		// ==========================================================
		// CONNECT
		client.connect();

		// 파일 업로드 URI 요청
		String uploadUri = client.requestFileUploadUri();
		LOGGER.info("=====>>>>>  : {}", uploadUri);
		Assert.assertNotNull(uploadUri);

		// 파일 업로드 완료 전송
		String fileName = "something.txt";
		int result = client.requestFileUploadComplete(fileName, uploadUri);
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
	 * @param url
	 * @param filePath
	 * @return
	 */
	private static boolean downloadFile(String url, String filePath) {
		FileOutputStream fos = null;
		ReadableByteChannel rbc = null;
		InputStream websiteIs = null;
		try {
			URL website = new URL(url);
			websiteIs = website.openStream();
			rbc = Channels.newChannel(websiteIs);
			fos = new FileOutputStream(filePath);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			closeIfNotNull(fos);
			closeIfNotNull(rbc);
			closeIfNotNull(websiteIs);
		}
		return true;
	}

	private static void closeIfNotNull(Closeable obj) {
		if (obj != null) {
			try {
				obj.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
