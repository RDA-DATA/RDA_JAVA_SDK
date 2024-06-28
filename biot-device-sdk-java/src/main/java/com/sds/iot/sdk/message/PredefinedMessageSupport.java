
package com.sds.iot.sdk.message;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sds.iot.sdk.util.JsonUtil;

/**
 * 이 클래스는 사전 정의된 메시지를 관리(생성/파싱)하는 함수를 제공합니다.
 * 
 * @author SDS
 */
public class PredefinedMessageSupport {

	private static String THING_IP;
	static {
		try {
			THING_IP = Inet4Address.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			THING_IP = null;
			e.printStackTrace();
		}
	}

	/**
	 * MSGBA0030001 : 지속 연결 요청 메시지 생성
	 * Keep-Alive(= Ping-pong = Heart Beat)
	 * 
	 * @return
	 */
	public static IotMessage createKeepAliveMessage() {
		IotMessage msg = new IotMessage();
		msg.setMsgCode(IotMessageCodeEnum.CONN_CTN_REQ.getMsgCode());
		//	msg.setFuncType("003"); // 구 서버 호환
		return msg;
	}

	/**
	 * MSGBA0110001 : 사물 활성화 요청 메시지 생성
	 * 
	 * @param modelName
	 * @param uniqueNum
	 * @param thingNickName
	 *        : null 설정 시 thingNickName 설정 안함
	 * @return
	 */
	public static IotMessage createActivateThingMessage(String modelName, String uniqueNum, String thingNickName) {
		JsonObject obj = new JsonObject();
		// Tree 형태로 등록도 가능.
		obj.addProperty("modelName", modelName);
		obj.addProperty("uniqueNum", uniqueNum);
		if (thingNickName != null && !thingNickName.isEmpty()) {
			obj.addProperty("thingNickName", thingNickName);
		}
		if (THING_IP != null) {
			obj.addProperty("thingIp", THING_IP);
		}
		String dataStr = JsonUtil.toJson(obj);

		IotMessage msg = new IotMessage();
		msg.setMsgCode(IotMessageCodeEnum.THING_ACTIVATION_REQ.getMsgCode());
		//	msg.setFuncType("011"); // 구 서버 호환
		msg.setDataString(dataStr);
		return msg;
	}

	/**
	 * MSGBA0110003 : 리프사물(end-node) 등록 요청 메시지 생성
	 * 
	 * @param parentThingName
	 * @param modelName
	 * @param uniqueNum
	 * @param thingNickName
	 *        : null 설정 시 thingNickName 설정 안함
	 * @return
	 */
	public static IotMessage createRegisterLeafThingMessage(String parentThingName, String modelName, String uniqueNum,
			String thingNickName) {
		// Tree 형태로 등록도 가능.
		JsonObject obj = new JsonObject();
		obj.addProperty("parentThingId", parentThingName); // thingId 명칭이지만 실제로는 thingName 설정
		obj.addProperty("modelName", modelName);
		obj.addProperty("uniqueNum", uniqueNum);
		if (thingNickName != null && !thingNickName.isEmpty()) {
			obj.addProperty("thingNickName", thingNickName);
		}
		String dataStr = JsonUtil.toJson(obj);

		IotMessage msg = new IotMessage();
		// endnode 명은 endNodeModelName + . + endNodeUniqueNum 로 고정
		msg.setLeafThingName(modelName + "." + uniqueNum);
		msg.setMsgCode(IotMessageCodeEnum.END_NODE_REG_REQ.getMsgCode());
		msg.setDataString(dataStr);
		return msg;
	}

	/**
	 * Basic-AttrGroup : 사물 속성 정보 전달 메시지 생성
	 * 
	 * @param leafThingName
	 *        (= end-node)
	 *        null 설정 시, root 사물에 대한 메시지가 됨
	 * @param msgCode
	 *        null 설정 시, 기본 메시지코드 "Basic-AttrGroup" 이 사용됨
	 * @param dataStr
	 * @param dataFormat
	 *        null 설정 시, "application/json" 이 사용됨
	 * @return
	 */
	public static IotMessage createSendAttributesMessage(String leafThingName, String msgCode, String dataStr, String dataFormat) {
		IotMessage msg = new IotMessage();
		if (leafThingName != null) {
			msg.setLeafThingName(leafThingName);
		}
		if (msgCode != null) {
			msg.setMsgCode(msgCode);
		}
		//msg.setFuncType("021"); // 구 서버 호환
		if (dataFormat != null) {
			msg.setDataFormat(dataFormat);
		}
		msg.setDataString(dataStr);
		return msg;
	}

	/**
	 * MSGBA0420001 : 능동형 펌웨어 최신 버전 요청 메시지 생성
	 * (다건이 가능한 구조이나, 이 함수에서는 단건에 대한 요청만 생성함)
	 * 
	 * @param modelName
	 * @param firmwareType
	 * @return
	 */
	public static IotMessage createRequestFirmwareVersionMessage(String modelName, String firmwareType) {
		JsonObject item = new JsonObject();
		item.addProperty("modelName", modelName);
		item.addProperty("type", firmwareType);
		JsonArray arr = new JsonArray();
		arr.add(item);

		JsonObject obj = new JsonObject();
		obj.add("requestList", arr);
		String dataStr = JsonUtil.toJson(obj);

		IotMessage msg = new IotMessage();
		msg.setMsgCode(IotMessageCodeEnum.FW_VERSION_REQ.getMsgCode());
		msg.setDataString(dataStr);
		return msg;
	}

	/**
	 * MSGBA0420002 : 능동형 펌웨어 최신 버전 응답 메시지 파싱
	 * 
	 * @param resMsg
	 * @return Latest firmware information. Entries are "version", "fileUri", "modelName" and "type".
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, String> parseFirmwareVersionResponseMessage(IotMessage resMsg) {
		//	{
		//	    "resultList": [
		//	        {
		//	            "fileUri": "http://local.insator.io:8081/contentsService?accessKey=88b4b666-016f-1000-0000-21a02d0b66a9&downloadKey=88b51e52-016f-1000-0000-21a02d0b66a9",
		//	            "modelName": "doorLock_001",
		//	            "type": "firmwareType01",
		//	            "version": "1.0"
		//	        }
		//	    ]
		//	}
		Map<String, Object> resultMap = JsonUtil.fromJsonToMap(resMsg.getDataString());
		List<?> resultList = (List<?>) resultMap.get("resultList");
		return (Map<String, String>) resultList.get(0);
	}

	/**
	 * MSGBA0420003 : 수동형 펌웨어 업그레이드 요청 메시지 파싱
	 * 
	 * @param msg
	 * @return Map Entries are "type, "version" and "fileUri".
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, String> parseFirmwareUpgradeRequestMessage(IotMessage msg) {
		//		{
		//		    "requestList":
		//		    [
		//		        {
		//		        "fileUri":"http://local.insator.io:8081/contentsService?accessKey=88b4b666-016f-1000-0000-21a02d0b66a9&downloadKey=88fa4155-016f-1000-0000-ec2145ad57bb",
		//		        "type":"FWT",
		//		        "version":"1.1"
		//		        }
		//		    ],
		//		    "upgradeId":"U000001",
		//		    "upgradeOption":"FOR"
		//		}
		Map<String, Object> resultMap = JsonUtil.fromJsonToMap(msg.getDataString());
		List<?> requestList = (List<?>) resultMap.get("requestList");
		return (Map<String, String>) requestList.get(0);
	}

	/**
	 * MSGBA0420004 : 수동형 펌웨어 업그레이드 응답 메시지 생성
	 * 
	 * @param msg
	 * @return
	 */
	public static IotMessage createFirmwareUpgradeResponseMessage(IotMessage msg) {
		Map<String, Object> resultMap = JsonUtil.fromJsonToMap(msg.getDataString());
		String upgradeId = (String) resultMap.get("upgradeId");

		JsonObject obj = new JsonObject();
		obj.addProperty("upgradeId", upgradeId);
		String dataStr = JsonUtil.toJson(obj);

		IotMessage resMsg = msg.createResponse();
		resMsg.setResCode(IotMessageCodeEnum.FW_UPGRADE_RES.getMsgCode());
		resMsg.setDataString(dataStr);
		return resMsg;
	}

	/**
	 * MSGBA0420005 : 펌웨어 업그레이드 완료 메시지 생성
	 * 
	 * @param firmwareType
	 * @param version
	 * @return
	 */
	public static IotMessage createFirmwareUpgradeCompleteMessage(String firmwareType, String version) {
		JsonObject obj = new JsonObject();
		obj.addProperty("type", firmwareType);
		obj.addProperty("version", version);
		String dataStr = JsonUtil.toJson(obj);

		IotMessage msg = new IotMessage();
		msg.setMsgCode(IotMessageCodeEnum.FW_UPGRADE_COMPLETE_REQ.getMsgCode());
		msg.setDataString(dataStr);
		return msg;
	}

	/**
	 * MSGBA0420007 : 펌웨어 싸인 데이터 요청 메시지 생성
	 * 
	 * @param fileUri
	 * @return
	 */
	public static IotMessage createRequestFirmwareSignMessage(String fileUri) {
		JsonObject obj = new JsonObject();
		obj.addProperty("fileUri", fileUri);
		String dataStr = JsonUtil.toJson(obj);

		IotMessage msg = new IotMessage();
		msg.setMsgCode(IotMessageCodeEnum.FW_SIGN_DATA_REQ.getMsgCode());
		msg.setDataString(dataStr);
		return msg;
	}

	/**
	 * MSGBA0420008 : 펌웨어 싸인 데이터 응답 메시지 파싱
	 * 
	 * @param resMsg
	 * @return
	 */
	public static String parseFirmwareSignResponseMessage(IotMessage resMsg) {
		//		{
		//			   "signedData": "----signed data----"
		//		}
		Map<String, Object> resultMap = JsonUtil.fromJsonToMap(resMsg.getDataString());
		return (String) resultMap.get("signedData");
	}

	/**
	 * MSGBA0410001 : 파일 업로드 URI 요청 메시지 생성
	 * 
	 * @return
	 */
	public static IotMessage createRequestFileUploadUriMessage() {
		IotMessage msg = new IotMessage();
		msg.setMsgCode(IotMessageCodeEnum.FILE_UPLOAD_URI_REQ.getMsgCode());
		return msg;
	}

	/**
	 * MSGBA0410002 : 파일 업로드 URI 응답 메시지 파싱
	 * 
	 * @param resMsg
	 * @return "uploadUri".
	 */
	public static String parseFileUploadUriResponseMessage(IotMessage resMsg) {
		//		{
		//		    "uploadUri": "http://local.insator.io:8081/contentsService?accessKey=8d46dc47-016f-1000-0001-77414c041fe8",
		//			"downloadUri" : "http://local.insator.io:8081/contentsService?accessKey=8d46dc47-016f-1000-0001-77414c041fe8"
		//		}
		Map<String, Object> resultMap = JsonUtil.fromJsonToMap(resMsg.getDataString());
		return (String) resultMap.get("uploadUri");
	}

	/**
	 * MSGBA0410017 : 파일 업로드 완료 메시지 생성
	 * 
	 * @param fileName
	 * @param fileUri
	 * @return
	 */
	public static IotMessage createFileUploadCompleteMessage(String fileName, String fileUri) {
		JsonObject obj = new JsonObject();
		obj.addProperty("fileName", fileUri);
		obj.addProperty("fileUri", fileUri);
		String dataStr = JsonUtil.toJson(obj);

		IotMessage msg = new IotMessage();
		msg.setMsgCode(IotMessageCodeEnum.FILE_UPLOAD_COMPLETE_REQ.getMsgCode());
		msg.setDataString(dataStr);
		return msg;
	}
}
