package com.sample.app;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.iot.sdk.DefaultMessageListener;
import com.sds.iot.sdk.IotClient;

/**
 * Sample App (Firmware)
 * 능동형 펌웨어 최신 버전을 요청하거나 수동형 펌웨어 요청을 처리하는 예제
 *
 */
public class AppFirmware {
	private static final Logger	LOGGER	= LoggerFactory.getLogger(AppFirmware.class);
	
	public static void main(String[] args) {
		try {
			// Setup the thing information
			String userId = "test@samsung.com";
			String userPw = "test1!";
			String siteId = "CB00000000";
			String thingName = "TestModel.020";

			IotClient client = IotClient.thing(siteId, thingName).itaUserLogin(userId, userPw).configFile("biot_client.properties");
			client.setCustomMessageListener(new FirmwareListener()); // 수동형 펌웨어 요청 수신을 위한 사용자 메시지 리스너 설정

			client.connect();
			
			// 능동형 펌웨어 최신 버전 요청
			Map<String, String> firmwareInfo = client.requestFirmwareLatestVersion("TestModel", "Patch");
			LOGGER.info("firmwareInfo : {}", firmwareInfo);

			String fileUri = firmwareInfo.get("fileUri");		// 해당 uri로 펌웨어 파일 다운로드 요청
			LOGGER.info("fileUri=["+fileUri+"]");

			// 해당 uri로 파일 다운로드 실행
			// 펌웨어 업데이트
			// ...

		} catch (Exception e) {
			// You can exception handling
			e.printStackTrace();
		}
	}
}

class FirmwareListener extends DefaultMessageListener {
	private static final Logger	LOGGER	= LoggerFactory.getLogger(AppFirmware.class);

	// 수동형 펌웨어 요청 수신부
	// ocp_node_firmware_upgrade.conf에서 firmwareScheduleOn 항목이 true 세팅되어야 한다.
	// Firmware - 펌웨어 작업 관리 를 통하여 Job을 등록해야 함.
	@Override
	protected void upgradeFirmware(String downloadUri, String firmwareType, String version) {
		LOGGER.info("=====>>>>> [FirmwareListener] upgradeFirmware");
		LOGGER.info("=====>>>>> [FirmwareListener] TYPE={}, VERSION={} ", firmwareType, version);
		LOGGER.info("=====>>>>> [FirmwareListener] DOWNLOAD : GET {} ", downloadUri);

		// 해당 downloadUri로 펌웨어 파일 다운로드 요청
		// 펌웨어 업데이트
		// ...
	}
}