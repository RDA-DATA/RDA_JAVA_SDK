
package com.sds.iot.sdk;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.iot.sdk.auth.IAuth;
import com.sds.iot.sdk.auth.ita.ItaBasicAuthAuth;
import com.sds.iot.sdk.auth.ita.ItaUserLoginAuth;
import com.sds.iot.sdk.auth.mutual.MutualTlsAuth;
import com.sds.iot.sdk.exception.IotException;
import com.sds.iot.sdk.message.IotMessage;
import com.sds.iot.sdk.message.PredefinedMessageSupport;

/**
 * 이 클래스는 <code>IotCoreClient</code> 클래스에서 사전 정의된 메시지 전송 기능을 메서드 형태로 추가한 클래스입니다.<p>
 * 다음과 같은 기능을 메서드 형태로 제공합니다.
 * <ul>
 * <li>사물 활성화 요청</li>
 * <li>Leaf 사물 등록/활성화 요청</li>
 * <li>사물 속성 데이터 등록</li>
 * <li>능동형 펌웨어 최신 버전 요청</li>
 * <li>펌웨어 업그레이드 완료 전송</li>
 * <li>펌웨어 싸인 데이터 요청</li>
 * <li>파일 업로드 URI 요청</li>
 * <li>파일 업로드 완료 전송</li>
 * </ul>
 * 
 * @author SDS
 */
public class IotClient extends IotCoreClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(IotClient.class);

	/* ======================================================================================= */

	/**
	 * <pre>
	 * IotClient 객체 생성을 위한 인증 빌더를 리턴합니다. 다음과 같은 형태로 사용 가능합니다.
	 * IotClient client1 = IotClient.thing(siteId, thingName).mutualTls().configFile("biot.properties");
	 * IotClient client2 = IotClient.thing(siteId, thingName).itaUserLogin(username, password).configFile("biot.properties");
	 * IotClient client3 = IotClient.thing(siteId, thingName).itaBasicAuth(basicAuthCredential).configFile("biot.properties");
	 * </pre>
	 * 
	 * @param siteId IoT 서버에 등록된 사이트 ID
	 * @param thingName IoT 서버에 등록된 사물명
	 * @return IoTClient 객체 생성을 위한 인증 빌더
	 */
	public static AuthBuilder thing(String siteId, String thingName) {
		return new AuthBuilder(siteId, thingName);
	}

	/**
	 * IotClient 객체 생성을 위한 인증 빌더 클래스입니다.
	 *
	 */
	public static class AuthBuilder {
		private final String	siteId;
		private final String	thingName;

		private AuthBuilder(String siteId, String thingName) {
			this.siteId = siteId;
			this.thingName = thingName;
		}

		public ConfBuilder mutualTls() {
			return new ConfBuilder(siteId, thingName, new MutualTlsAuth());
		}

		public ConfBuilder itaUserLogin(String username, String password) {
			return new ConfBuilder(siteId, thingName, new ItaUserLoginAuth(username, password));
		}

		public ConfBuilder itaBasicAuth(String basicAuthCredential) {
			return new ConfBuilder(siteId, thingName, new ItaBasicAuthAuth(basicAuthCredential));
		}

	}

	/**
	 * IotClient 객체 생성을 위한 config 파일 처리 빌더 클래스입니다.
	 *
	 */
	public static class ConfBuilder {
		private final String	siteId;
		private final String	thingName;
		private final IAuth		auth;

		/**
		 * @param siteId IoT 서버에 등록된 사이트 ID
		 * @param thingName IoT 서버에 등록된 사물명
		 * @param auth 사물에 적용되어 있는 인증 방식
		 */
		private ConfBuilder(String siteId, String thingName, IAuth auth) {
			this.siteId = siteId;
			this.thingName = thingName;
			this.auth = auth;
		}

		/**
		 * Config 파일을 참고하여 IotClient 객체를 만들고 리턴합니다.
		 * <pre>
		 * <code>
		IotClient client1 = IotClient.thing(siteId, thingName).mutualTls().configFile("biot.properties");
		IotClient client2 = IotClient.thing(siteId, thingName).itaUserLogin(username, password).configFile("biot.properties");
		IotClient client3 = IotClient.thing(siteId, thingName).itaBasicAuth(basicAuthCredential).configFile("biot.properties");
		 * </code>
		 * </pre>
		 *
		 * @param configFilePath Config 파일 path
		 * @return IotClient
		 */
		public IotClient configFile(String configFilePath) {
			return new IotClient(auth, new IotClientConfig(siteId, thingName, configFilePath));
		}

		/**
		 * API 서버 주소를 참고하여 IotClient 객체를 만들고 리턴합니다.
		 * <pre>
		 * <code>
		 IotClient client4 = IotClient.thing(siteId, thingName).itaUserLogin("username", "password").apiServerAddr(apiServerAddr);
		 IotClient client5 = IotClient.thing(siteId, thingName).itaBasicAuth("basicAuthCredential").apiServerAddr(apiServerAddr);
		 *</code>
		 * </pre>
		 *
		 * @param apiServerAddr
		 * @return IotClient
		 */
		public IotClient apiServerAddr(String apiServerAddr) {
			return new IotClient(auth, new IotClientConfig(siteId, thingName, apiServerAddr, null));
		}
	}

	/* ======================================================================================= */

	/**
	 * @param auth 클라이언트 인증 정보를 나타내는 IAuth 형 파라미터
	 * @param cfg B.IoT 기본 설정값을 가지고 있는 IotClientConfig 형 파라미터
	 */
	public IotClient(IAuth auth, IotClientConfig cfg) {
		super(auth, cfg);
	}

	/**
	 * @param auth 클라이언트 인증 정보를 나타내는 IAuth 형 파라미터
	 * @param siteId
	 *        IoT 서버에 등록된 사이트 ID
	 *        : e.g. "CB00000000"
	 * @param thingName
	 *        IoT 서버에 등록된 사물명
	 * @param configFilePath
	 *        : configuration properties file path. (relative or abstract)
	 *        e.g. "biot_client.properties"
	 */
	public IotClient(IAuth auth, String siteId, String thingName, String configFilePath) {
		super(auth, new IotClientConfig(siteId, thingName, configFilePath));
	}

	/**
	 * @param auth 클라이언트 인증 정보를 나타내는 IAuth 형 파라미터
	 * @param siteId
	 * 	IoT 서버에 등록된 사이트 ID
	 *        : e.g. "CB00000000"
	 * @param thingName
	 * 	IoT 서버에 등록된 사물명
	 * @param apiServerAddr
	 *        : e.g. "https://test-brighticsiot.samsungsds.com:8088"
	 * @param serverAddr
	 *        : if it uses 'ITA' auth, set null.
	 *        e.g. "ssl://test-brighticsiot.samsungsds.com:8001"
	 */
	public IotClient(IAuth auth, String siteId, String thingName, String apiServerAddr, String serverAddr) {
		super(auth, new IotClientConfig(siteId, thingName, apiServerAddr, serverAddr));
	}

	/* ======================================================================================= */

	/**
	 * B.IoT 서버와 약속된 프로토콜을 통하여 연결합니다.
	 *
	 * @throws IotException
	 */
	@Override
	public void connect() throws IotException {
		super.connect();
	}

	/**
	 * B.IoT 서버와 연결된 프로토콜의 연결을 끊습니다.
	 */
	@Override
	public void disconnect() {
		super.disconnect();
	}

	@Override
	public void setCustomMessageListener(ICustomMessageListener customListener) {
		super.setCustomMessageListener(customListener);
	}

	/* ======================================================================================= */

	/**
	 * Root 사물의 속성 데이터를 서버로 전송.
	 * 
	 * @param msgCode
	 *        null 설정 시, 기본 메시지코드 "Basic-AttrGroup" 이 사용됨
	 * @param dataStr
	 * 		  약속된 포맷을 가진 스트링형 데이터
	 */
	public void sendAttributes(String msgCode, String dataStr) {
		IotMessage msg = PredefinedMessageSupport.createSendAttributesMessage(null, msgCode, dataStr, null);
		this.send(msg);
	}

	/**
	 * Root 사물의 속성 데이터를 서버로 전송. (결과 확인)
	 * 
	 * @param msgCode
	 *        null 설정 시, 기본 메시지코드 "Basic-AttrGroup" 이 사용됨
	 * @param dataStr
	 * 		  약속된 포맷을 가진 스트링형 데이터
	 * @return
	 * 		   int형 응답메시지의 응답 상태 코드값
	 */
	public int sendAttributesSync(String msgCode, String dataStr) {
		IotMessage msg = PredefinedMessageSupport.createSendAttributesMessage(null, msgCode, dataStr, null);
		IotMessage resMsg = this.syncCall(msg, getCfg().getDefaultSyncResponseTimeout());
		int retStatus = IotStatusCode.NO_RESPONSE.getCode();
		if(resMsg != null)
		{
			retStatus = Integer.parseInt(resMsg.getResCode());
			if(!resMsg.getResCode().startsWith("2"))
			{
				LOGGER.warn("FAIL TO SEND ATTRIBUTES! ({})", resMsg.getResMsg());
			}
		}
		else {
			LOGGER.warn("FAIL TO SEND ATTRIBUTES! ({})", "Response Timeout");
		}

		return retStatus;
	}

	/**
	 * Root 사물의 속성 데이터를 서버로 전송.
	 * 
	 * @param msgCode
	 *        null 설정 시, 기본 메시지코드 "Basic-AttrGroup" 이 사용됨
	 * @param dataStr
	 * 		  약속된 포맷을 가진 스트링형 데이터
	 * @param dataFormat 메시지 데이터의 포맷
	 */
	public void sendAttributes(String msgCode, String dataStr, String dataFormat) {
		IotMessage msg = PredefinedMessageSupport.createSendAttributesMessage(null, msgCode, dataStr, dataFormat);
		LOGGER.warn(msg.toStringFull());
		this.send(msg);
	}

	/**
	 * Root 사물의 속성 데이터를 서버로 전송. (결과 확인)
	 * 
	 * @param msgCode
	 *        null 설정 시, 기본 메시지코드 "Basic-AttrGroup" 이 사용됨
	 * @param dataStr
	 * 약속된 포맷을 가진 스트링형 데이터
	 * @param dataFormat
	 * 메시지 데이터의 포맷
	 * @return
	 * 		   int형 응답메시지의 응답 상태 코드값
	 */
	public int sendAttributesSync(String msgCode, String dataStr, String dataFormat) {
		IotMessage msg = PredefinedMessageSupport.createSendAttributesMessage(null, msgCode, dataStr, dataFormat);
		IotMessage resMsg = this.syncCall(msg, getCfg().getDefaultSyncResponseTimeout());
		int retStatus = IotStatusCode.NO_RESPONSE.getCode();
		if(resMsg != null)
		{
			retStatus = Integer.parseInt(resMsg.getResCode());
			if(!resMsg.getResCode().startsWith("2"))
			{
				LOGGER.warn("FAIL TO SEND ATTRIBUTES! ({})", resMsg.getResMsg());
			}
		}
		else {
			LOGGER.warn("FAIL TO SEND ATTRIBUTES! ({})", "Response Timeout");
		}

		return retStatus;
	}

	/**
	 * Leaf 사물의 속성 데이터를 서버로 전송.
	 * 
	 * @param leafThingName
	 *        End-node name(= Leaf thing name).
	 * @param msgCode
	 *        null 설정 시, 기본 메시지코드 "Basic-AttrGroup" 이 사용됨
	 * @param dataStr
	 * 약속된 포맷을 가진 스트링형 데이터
	 */
	public void sendAttributesForLeaf(String leafThingName, String msgCode, String dataStr) {
		IotMessage msg = PredefinedMessageSupport.createSendAttributesMessage(leafThingName, msgCode, dataStr, null);
		this.send(msg);
	}

	/**
	 * Leaf 사물의 속성 데이터를 서버로 전송.
	 * 
	 * @param leafThingName
	 *        End-node name(= Leaf thing name).
	 * @param msgCode
	 *        null 설정 시, 기본 메시지코드 "Basic-AttrGroup" 이 사용됨
	 * @param dataStr
	 * 약속된 포맷을 가진 스트링형 데이터
	 * @return
	 * 		   int형 응답메시지의 응답 상태 코드값
	 */
	public int sendAttributesSyncForLeaf(String leafThingName, String msgCode, String dataStr) {
		IotMessage msg = PredefinedMessageSupport.createSendAttributesMessage(leafThingName, msgCode, dataStr, null);
		IotMessage resMsg = this.syncCall(msg, getCfg().getDefaultSyncResponseTimeout());
		int retStatus = IotStatusCode.NO_RESPONSE.getCode();
		if(resMsg != null)
		{
			retStatus = Integer.parseInt(resMsg.getResCode());
			if(!resMsg.getResCode().startsWith("2"))
			{
				LOGGER.warn("FAIL TO SEND ATTRIBUTES! ({})", resMsg.getResMsg());
			}
		}
		else {
			LOGGER.warn("FAIL TO SEND ATTRIBUTES! ({})", "Response Timeout");
		}

		return retStatus;
	}

	/**
	 * Leaf 사물의 속성 데이터를 서버로 전송.
	 * 
	 * @param leafThingName
	 * @param msgCode
	 *        null 설정 시, 기본 메시지코드 "Basic-AttrGroup" 이 사용됨
	 * @param dataStr
	 * 약속된 포맷을 가진 스트링형 데이터
	 * @param dataFormat
	 * 메시지 데이터의 포맷
	 */
	public void sendAttributesForLeaf(String leafThingName, String msgCode, String dataStr, String dataFormat) {
		IotMessage msg = PredefinedMessageSupport.createSendAttributesMessage(leafThingName, msgCode, dataStr, dataFormat);
		this.send(msg);
	}

	/**
	 * Leaf 사물의 속성 데이터를 서버로 전송. (결과 확인)
	 * 
	 * @param leafThingName
	 * @param msgCode
	 *        null 설정 시, 기본 메시지코드 "Basic-AttrGroup" 이 사용됨
	 * @param dataStr
	 * 약속된 포맷을 가진 스트링형 데이터
	 * @param dataFormat
	 * 메시지 데이터의 포맷
	 * @return
	 * 		   int형 응답메시지의 응답 상태 코드값
	 */
	public int sendAttributesSyncForLeaf(String leafThingName, String msgCode, String dataStr, String dataFormat) {
		IotMessage msg = PredefinedMessageSupport.createSendAttributesMessage(leafThingName, msgCode, dataStr, dataFormat);
		IotMessage resMsg = this.syncCall(msg, getCfg().getDefaultSyncResponseTimeout());
		int retStatus = IotStatusCode.NO_RESPONSE.getCode();
		if(resMsg != null)
		{
			retStatus = Integer.parseInt(resMsg.getResCode());
			if(!resMsg.getResCode().startsWith("2"))
			{
				LOGGER.warn("FAIL TO SEND ATTRIBUTES! ({})", resMsg.getResMsg());
			}
		}
		else {
			LOGGER.warn("FAIL TO SEND ATTRIBUTES! ({})", "Response Timeout");
		}

		return retStatus;
	}

	/* ======================================================================================= */

	/**
	 * 사물 활성화 요청
	 * 
	 * @param modelName
	 * @param uniqueNum
	 * @return
	 * 		   int형 응답메시지의 응답 상태 코드값
	 */
	public int activateThing(String modelName, String uniqueNum) {
		IotMessage msg = PredefinedMessageSupport.createActivateThingMessage(modelName, uniqueNum, null);
		IotMessage resMsg = this.syncCall(msg, getCfg().getDefaultSyncResponseTimeout());
		int retStatus = IotStatusCode.NO_RESPONSE.getCode();
		if(resMsg != null)
		{
			retStatus = Integer.parseInt(resMsg.getResCode());
			if(!resMsg.getResCode().startsWith("2"))
			{
				LOGGER.warn("CANNOT ACTIVATE THING!");
			}
		}
		else {
			LOGGER.warn("CANNOT ACTIVATE THING!");
		}

		return retStatus;
	}

	/**
	 * Leaf 사물 활성화 요청
	 *
	 * @param parentThingName
	 * 		  IoT에 등록된 Leaf 사물의 부모 사물명
	 * @param modelName
	 *        IoT에 등록된 Leaf 사물의 모델명
	 * @param uniqueNum
	 *        IoT에 등록된 Leaf 사물의 고유 번호
	 * @return
	 * 		   int형 응답메시지의 응답 상태 코드값
	 */
	public int activateThingForLeaf(String parentThingName, String modelName, String uniqueNum) {
		IotMessage msg = PredefinedMessageSupport.createRegisterLeafThingMessage(parentThingName, modelName, uniqueNum, null);
		IotMessage resMsg = this.syncCall(msg, getCfg().getDefaultSyncResponseTimeout());
		int retStatus = IotStatusCode.NO_RESPONSE.getCode();
		if(resMsg != null)
		{
			retStatus = Integer.parseInt(resMsg.getResCode());
			if(!resMsg.getResCode().startsWith("2"))
			{
				LOGGER.warn("CANNOT ACTIVATE LEAF THING!");
			}
		}
		else {
			LOGGER.warn("CANNOT ACTIVATE LEAF THING!");
		}

		return retStatus;
	}

	/**
	 * 사물 활성화 요청 (with Nickname)
	 * 
	 * @param modelName
	 *        IoT에 등록된 사물의 모델명
	 * @param uniqueNum
	 *        IoT에 등록된 사물의 고유 번호
	 * @param thingNickName
	 *        IoT에 등록된 사물의 별명
	 * @return
	 * 		   int형 응답메시지의 응답 상태 코드값
	 */
	public int activateThing(String modelName, String uniqueNum, String thingNickName) {
		IotMessage msg = PredefinedMessageSupport.createActivateThingMessage(modelName, uniqueNum, thingNickName);
		IotMessage resMsg = this.syncCall(msg, getCfg().getDefaultSyncResponseTimeout());
		int retStatus = IotStatusCode.NO_RESPONSE.getCode();
		if(resMsg != null)
		{
			retStatus = Integer.parseInt(resMsg.getResCode());
			if(!resMsg.getResCode().startsWith("2"))
			{
				LOGGER.warn("CANNOT ACTIVATE THING!");
			}
		}
		else {
			LOGGER.warn("CANNOT ACTIVATE THING!");
		}

		return retStatus;
	}

	/**
	 * Leaf 사물 등록/활성화 요청
	 * 
	 * @param parentThingName
	 * 		  IoT에 등록된 Leaf 사물의 부모 사물명
	 * @param modelName
	 *        IoT에 등록된 Leaf 사물의 모델명
	 * @param uniqueNum
	 *        IoT에 등록된 Leaf 사물의 고유 번호
	 * @return
	 * 		   int형 응답메시지의 응답 상태 코드값
	 */
	public int registerLeafThing(String parentThingName, String modelName, String uniqueNum) {
		IotMessage msg = PredefinedMessageSupport.createRegisterLeafThingMessage(parentThingName, modelName, uniqueNum, null);
		IotMessage resMsg = this.syncCall(msg, getCfg().getDefaultSyncResponseTimeout());
		int retStatus = IotStatusCode.NO_RESPONSE.getCode();
		if(resMsg != null)
		{
			retStatus = Integer.parseInt(resMsg.getResCode());
			if(!resMsg.getResCode().startsWith("2"))
			{
				LOGGER.warn("CANNOT REGISTER LEAF THING!");
			}
		}
		else {
			LOGGER.warn("CANNOT REGISTER LEAF THING!");
		}

		return retStatus;
	}

	/**
	 * Leaf 사물 등록/활성화 요청 (with Nickname)
	 * 
	 * @param parentThingName
	 * 		  IoT에 등록된 Leaf 사물의 부모 사물명
	 * @param modelName
	 *        IoT에 등록된 Leaf 사물의 모델명
	 * @param uniqueNum
	 *        IoT에 등록된 Leaf 사물의 고유 번호
	 * @param thingNickName
	 *        IoT에 등록된 Leaf 사물의 별명
	 * @return
	 * 		   int형 응답메시지의 응답 상태 코드값
	 */
	public int registerLeafThing(String parentThingName, String modelName, String uniqueNum, String thingNickName) {
		IotMessage msg = PredefinedMessageSupport.createRegisterLeafThingMessage(parentThingName, modelName, uniqueNum,
				thingNickName);
		IotMessage resMsg = this.syncCall(msg, getCfg().getDefaultSyncResponseTimeout());
		int retStatus = IotStatusCode.NO_RESPONSE.getCode();
		if(resMsg != null)
		{
			retStatus = Integer.parseInt(resMsg.getResCode());
			if(!resMsg.getResCode().startsWith("2"))
			{
				LOGGER.warn("CANNOT REGISTER LEAF THING!");
			}
		}
		else {
			LOGGER.warn("CANNOT REGISTER LEAF THING!");
		}

		return retStatus;
	}

	/* ======================================================================================= */

	/**
	 * 능동형 펌웨어 최신 버전 요청 (모델/펌웨어타입 단건)
	 * 
	 * @param modelName
	 *        IoT에 등록된 사물의 모델명
	 * @param firmwareType
	 *        IoT에 등록된 펌웨어 타입
	 * @return Latest firmware information. Entries are "version", "fileUri", "modelName" and "type".
	 */
	public Map<String, String> requestFirmwareLatestVersion(String modelName, String firmwareType) {
		// 단건 요청
		IotMessage msg = PredefinedMessageSupport.createRequestFirmwareVersionMessage(modelName, firmwareType);
		IotMessage resMsg = this.syncCall(msg, getCfg().getDefaultSyncResponseTimeout());
		if (resMsg != null && resMsg.getResCode().startsWith("2") && resMsg.getData() != null) {
			// 단건 응답 파싱
			return PredefinedMessageSupport.parseFirmwareVersionResponseMessage(resMsg);
		}
		LOGGER.warn("CANNOT GET LATEST FIRMWARE VERSION!");
		return null;
	}

	// 다건 능동형 펌웨어 최신 버전 요청 기능 (모델 하나, 펌웨어타입 다건)
	//public Map<String, Object> requestFirmwaresLatestVersionOneModel(String modelName, String ... firmwareTypes) {
	//	return nul;
	//}

	// 다건 능동형 펌웨어 최신 버전 요청 기능 (펌웨어타입 하나, 모델 다건)
	//public Map<String, Object> requestFirmwaresLatestVersionOneType(String firmwareType, String ... modelNames ) {
	//	 return null;
	//}

	/**
	 * 펌웨어 업그레이드 완료 전송
	 * 
	 * @return
	 * 		   int형 응답메시지의 응답 상태 코드값
	 */
	public int requestFirmwareUpgradeComplete(String firmwareType, String version) {
		IotMessage msg = PredefinedMessageSupport.createFirmwareUpgradeCompleteMessage(firmwareType, version);
		IotMessage resMsg = this.syncCall(msg, getCfg().getDefaultSyncResponseTimeout());
		int retStatus = IotStatusCode.NO_RESPONSE.getCode();
		if(resMsg != null)
		{
			retStatus = Integer.parseInt(resMsg.getResCode());
			if(!resMsg.getResCode().startsWith("2"))
			{
				LOGGER.warn("CANNOT SEND FIRMWARE UPGRADE COMPLETE MESSAGE!");
			}
		}
		else {
			LOGGER.warn("CANNOT SEND FIRMWARE UPGRADE COMPLETE MESSAGE!");
		}

		return retStatus;
	}

	/**
	 * 펌웨어 싸인 데이터 요청
	 * 
	 * @param fileUri 싸인 데이터 요청 URI
	 * @return
	 * 스트링형 싸인 데이터
	 * <p>
	 * </p>
	 * <p>
	 * </p>
	 */
	public String requestFirmwareSignData(String fileUri) {
		IotMessage msg = PredefinedMessageSupport.createRequestFirmwareSignMessage(fileUri);
		IotMessage resMsg = this.syncCall(msg, getCfg().getDefaultSyncResponseTimeout());
		if (resMsg != null && resMsg.getResCode().startsWith("2") && resMsg.getData() != null) {
			// 응답 파싱
			return PredefinedMessageSupport.parseFirmwareSignResponseMessage(resMsg);
		}
		LOGGER.warn("CANNOT GET FIRMWARE SIGN!");
		return null;
	}

	/**
	 * 파일 업로드 URI 요청
	 * 
	 * @return 업로드 URI
	 */
	public String requestFileUploadUri() {
		IotMessage msg = PredefinedMessageSupport.createRequestFileUploadUriMessage();
		IotMessage resMsg = this.syncCall(msg, getCfg().getDefaultSyncResponseTimeout());
		if (resMsg != null && resMsg.getResCode().startsWith("2") && resMsg.getData() != null) {
			// 응답 파싱
			return PredefinedMessageSupport.parseFileUploadUriResponseMessage(resMsg);
		}
		LOGGER.warn("CANNOT GET FILE UPLOAD URI!");
		return null;
	}

	/**
	 * 파일 업로드 완료 전송
	 * 
	 * @param fileName 업로드한 파일 네임
	 * @param fileUri 파일 업로드에 사용된 URI
	 * @return
	 */
	public int requestFileUploadComplete(String fileName, String fileUri) {
		IotMessage msg = PredefinedMessageSupport.createFileUploadCompleteMessage(fileName, fileUri);
		IotMessage resMsg = this.syncCall(msg, getCfg().getDefaultSyncResponseTimeout());
		int retStatus = IotStatusCode.NO_RESPONSE.getCode();
		if(resMsg != null)
		{
			retStatus = Integer.parseInt(resMsg.getResCode());
			if(!resMsg.getResCode().startsWith("2"))
			{
				LOGGER.warn("CANNOT SEND FILE UPLOAD COMPLETE MESSAGE!");
			}
		}
		else {
			LOGGER.warn("CANNOT SEND FILE UPLOAD COMPLETE MESSAGE!");
		}

		return retStatus;
	}

}
