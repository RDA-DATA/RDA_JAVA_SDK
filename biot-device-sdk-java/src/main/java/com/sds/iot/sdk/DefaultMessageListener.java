
package com.sds.iot.sdk;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.iot.sdk.callback.NoActionCallback;
import com.sds.iot.sdk.message.IotMessage;
import com.sds.iot.sdk.message.IotMessageCodeEnum;
import com.sds.iot.sdk.message.PredefinedMessageSupport;

/**
 * 이 클래스는 메시지 수신 시, 단순히 로깅만 수행하는 ICustomMessageListener 인터페이스의 구현체입니다.<br>
 * 이 클래스를 확장하거나 참고하여 필요한 동작을 구현하는 클래스를 만들어서 사용합니다.
 * 
 * <pre>
 * <code>
 * ...
 * IotClient client = new IotClient(auth, cfg);
 * client.setCustomMessageListener(new DefaultMessageListener());
 * ...
 * </code>
 * </pre>
 * 
 * @author SDS
 */
public class DefaultMessageListener implements ICustomMessageListener {
	private static final Logger	LOGGER	= LoggerFactory.getLogger(DefaultMessageListener.class);

	private IotCoreClient			client;

	/**
	 * B.IoT에 연결하기 위한 클라이언트를 설정합니다.
	 * @param client 연결을 위한 클라이언트
	 */
	@Override
	public void setClient(IotCoreClient client) {
		this.client = client;
	}

	/**
	 * B.IoT에 연결된 클라이언트를 반환합니다.
	 * @return  B.IoT 연결 클라이언트
	 */
	protected final IotCoreClient getClient() {
		return this.client;
	}

	@Override
	/**
	 * B.IoT로 부터 노티 메시지를 받을 때, 호출됩니다.
	 * @param msg B.IoT메시지
	 */
	public void onNotificationReceived(IotMessage msg) {
		LOGGER.debug("[DefaultMessageListener] onNotificationReceived");
		IotMessageCodeEnum e = IotMessageCodeEnum.fromMsgCode(msg.getMsgCode());
		if (e != null) {
			// pre-defined message code
			switch (e) {
				case ATTRGROUP_REQ:
				case PROVISIONING_REQ:
					processProvisionMessage(msg);
					break;
				case NOTIREQUEST_REQ:
				case NOTIFY:
				case MULTINOTIFY:
				case OLD_NOTIREQUEST_REQ:
				case OLD_NOTIFY:
					processNotifyMessage(msg);
					break;
				default:
					LOGGER.warn("[DefaultMessageListener] UNIMPLEMENTED SERVICE FOR MESSAGE CODE : {}", msg.getMsgCode());
					return;
			}
		} else {
			// custom message code
			processCustomNotificationMessage(msg);
			return;
		}
	}

	/**
	 * B.IoT로 부터 요청 메시지를 받을 때, 호출됩니다.
	 * @param msg B.IoT메시지
	 */
	@Override
	public void onRequestReceived(IotMessage msg) {
		LOGGER.debug("[DefaultMessageListener] onRequestReceived");
		IotMessageCodeEnum e = IotMessageCodeEnum.fromMsgCode(msg.getMsgCode());
		if (e != null) {
			// pre-defined message code
			switch (e) {
				case ATTRGROUP_REQ:
				case PROVISIONING_REQ:
					processProvisionMessage(msg);
					break;
				case NOTIREQUEST_REQ:
				case NOTIFY:
				case MULTINOTIFY:
				case OLD_NOTIREQUEST_REQ:
				case OLD_NOTIFY:
					processNotifyMessage(msg);
					break;
				case FW_UPGRADE_REQ:
					processFirmwareUpgadeRequest(msg);
					break;
				default:
					LOGGER.warn("[DefaultMessageListener] UNIMPLEMENTED SERVICE FOR MESSAGE CODE : {}", msg.getMsgCode());
					return;
			}
		} else {
			// custom message code
			processCustomRequestMessage(msg);
			return;
		}
	}

	/**
	 * B.IoT로 부터 속성 정보 전달 혹은 프로비저닝 메시지가 왔을 때, 이 메소드를 오버라이드하여 비지니스 로직을 수행할 수 있습니다.<br>
	 * 요청 메시지인 경우, B.IoT로 응답 메시지를 보내 줍니다.
	 * 
	 * @param msg B.IoT메시지
	 */
	protected void processProvisionMessage(IotMessage msg) {
		LOGGER.info("[DefaultMessageListener] processProvisionMessage <YOU MAY OVERRIDE THIS METHOD>");

		// DO SOMETHING.

		if ("Q".equals(msg.getMsgType())) {
			// RESPONSE TO SERVER.
			IotMessage resMsg = msg.createResponse();
			getClient().send(resMsg);
		}
	}
	
	/**
	 * B.IoT로 부터 통보 메시지가 왔을 때, 이 메소드를 오버라이드하여 비지니스 로직을 수행할 수 있습니다.<br>
	 * 요청 메시지인 경우, B.IoT로 응답 메시지를 보내 줍니다.
	 * 
	 * @param msg B.IoT메시지
	 */
	protected void processNotifyMessage(IotMessage msg) {
		LOGGER.info("[DefaultMessageListener] processNotifyMessage <YOU MAY OVERRIDE THIS METHOD>");

		// DO SOMETHING.

		if ("Q".equals(msg.getMsgType())) {
			// RESPONSE TO SERVER.
			IotMessage resMsg = msg.createResponse();
			getClient().send(resMsg);
		}
	}

	/**
	 * B.IoT로 부터 수동형 펌웨어 업그레이드 요청 메시지가 왔을 때, 펌웨어를 업데이트 해 주고 완료 메시지를 보내 줍니다.<br>
	 *
	 * @param msg B.IoT메시지
	 */
	protected void processFirmwareUpgadeRequest(IotMessage msg) {
		LOGGER.debug("[DefaultMessageListener] processFirmwareUpgadeRequest");

		// RESPONSE TO SERVER
		IotMessage resMsg = PredefinedMessageSupport.createFirmwareUpgradeResponseMessage(msg);
		getClient().send(resMsg);

		// PARSE FIRMWARE INFO
		Map<String, String> fwInfo = PredefinedMessageSupport.parseFirmwareUpgradeRequestMessage(msg);
		LOGGER.debug("firmware info : {}", fwInfo);
		String downloadUri = fwInfo.get("fileUri");
		String firmwareType = fwInfo.get("type");
		String version = fwInfo.get("version");

		// UPGRADE FIRMWARE
		upgradeFirmware(downloadUri, firmwareType, version);

		// SEND UPGRADE COMPLETE MESSAGE 
		IotMessage completeMsg = PredefinedMessageSupport.createFirmwareUpgradeCompleteMessage(firmwareType, version);
		getClient().call(completeMsg, new NoActionCallback());
	}

	/**
	 * B.IoT로 부터 수동형 펌웨어 업그레이드 요청 메시지가 왔을 때, 디바이스에서 펌웨어 업그레이드를 진행합니다.<br>
	 * 이 메소드를 오버라이드하여 디바이스의 펌웨어 업그레이드 로직을 구현해야 합니다.
	 * 
	 * @param downloadUri 펌웨어 다운로드 URI
	 * @param firmwareType 펌웨어 타입
	 * @param version 펌웨어 버전
	 */
	protected void upgradeFirmware(String downloadUri, String firmwareType, String version) {
		LOGGER.warn("[DefaultMessageListener] upgradeFirmware <YOU NEED TO OVERRIDE THIS METHOD>");
		LOGGER.info("[DefaultMessageListener] TYPE={}, VERSION={} ", firmwareType, version);
		LOGGER.info("[DefaultMessageListener] DOWNLOAD : GET {} ", downloadUri);
		// FILE DOWNLOAD AND DO SOMETHING!
	}

	/**
	 * B.IoT로 부터 커스텀 통보 메시지가 왔을 때, 이 메소드를 오버라이드하여 비지니스 로직을 수행할 수 있습니다.
	 *
	 * @param msg B.IoT메시지
	 */
	protected void processCustomNotificationMessage(IotMessage msg) {
		LOGGER.warn("[DefaultMessageListener] processCustomNotificationMessage <YOU NEED TO OVERRIDE THIS METHOD> : {}",
				msg.getMsgCode());

		// DO SOMETHING.

	}

	/**
	 * B.IoT로 부터 커스텀 요청 메시지가 왔을 때, 이 메소드를 오버라이드하여 비지니스 로직을 수행할 수 있습니다.<br>
	 * B.IoT로 응답 메시지를 보내 줍니다.
	 * 
	 * @param msg B.IoT메시지
	 */
	protected void processCustomRequestMessage(IotMessage msg) {
		LOGGER.warn("[DefaultMessageListener] processCustomRequestMessage <YOU NEED TO OVERRIDE THIS METHOD> : {}",
				msg.getMsgCode());

		// DO SOMETHING.

		// RESPONSE TO SERVER.
		IotMessage resMsg = msg.createResponse();
		//resMsg.setResCode(resCode);
		//resMsg.setDataString(dataStr);
		getClient().send(resMsg);
	}
}
