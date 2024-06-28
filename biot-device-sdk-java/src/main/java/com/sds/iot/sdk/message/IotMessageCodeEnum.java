
package com.sds.iot.sdk.message;

/**
 * 이 클래스는 클라이언트와 서버 사이에 통신 시 사용되는 메시지 코드입니다.
 * 
 * @author SDS
 *
 */
public enum IotMessageCodeEnum{

	/** 클라이언트→서버 : 인증 처리 요청 (1단계) */
	AUTH_PROCESS1_REQ("MSGAUTH00000"),
	
	/** 클라이언트→서버 : 인증 처리 요청 (2단계) */
	AUTH_PROCESS2_REQ("MSGAUTH00002"),

	/** 클라이언트→서버 : 지속 연결 요청 */
	CONN_CTN_REQ("MSGBA0030001"),

	/** 클라이언트↔서버 : 속성 정보 전달을 위한, 기본 사용자메시지 코드 */
	ATTRGROUP_REQ("Basic-AttrGroup"),

	/** 서버→클라이언트 : 최초 연결시 프로비저닝을 위한, 기본 사용자메시지 코드 */
	PROVISIONING_REQ("Basic-Provisioning"),

	/** 클라이언트→서버 : 사물 활성화 요청 */
	THING_ACTIVATION_REQ("MSGBA0110001"),

	/** 클라이언트→서버 : 리프사물(end-node) 등록/활성화 요청 */
	END_NODE_REG_REQ("MSGBA0110003"),

	/** 클라이언트→서버 : 능동형 펌웨어 최신 버전 요청 */
	FW_VERSION_REQ("MSGBA0420001"),

	/** 서버→클라이언트 : 수동형 펌웨어 업그레이드 요청 */
	FW_UPGRADE_REQ("MSGBA0420003"),

	/** 클라이언트→서버 : 수동형 펌웨어 업그레이드 응답 */
	FW_UPGRADE_RES("MSGBA0420004"),

	/** 클라이언트→서버 : 펌웨어 업그레이드 완료 */
	FW_UPGRADE_COMPLETE_REQ("MSGBA0420005"),

	/** 클라이언트→서버 : 펌웨어 싸인 데이터 요청 */
	FW_SIGN_DATA_REQ("MSGBA0420007"),

	/** 클라이언트→서버 : 파일 업로드 URI 요청 */
	FILE_UPLOAD_URI_REQ("MSGBA0410001"),

	/** 클라이언트→서버 : 파일 업로드 완료 */
	FILE_UPLOAD_COMPLETE_REQ("MSGBA0410017"), 
	
	/** 서버→클라이언트 : 무변환 메시지 전송 (notiRequest) */
	NOTIREQUEST_REQ("MSGBA0300001"),
	
	/** 서버→클라이언트 : 무변환 메시지 전송 (notify) */
	NOTIFY("MSGBA0300003"),
	
	/** 서버→클라이언트 : 무변환 메시지 전송 (multiNotify) */
	MULTINOTIFY("MSGBA0300007"),
	
	/** 서버→클라이언트 : 무변환 메시지 전송 (notiRequest - old version) */
	OLD_NOTIREQUEST_REQ("MSGBA0300027"),
	
	/** 서버→클라이언트 : 무변환 메시지 전송 (notify - old version) */
	OLD_NOTIFY("MSGBA0300029"),

	;

	private final String msgCode;

	/**
	 * @param msgCode
	 */
	IotMessageCodeEnum(String msgCode) {
		this.msgCode = msgCode;
	}

	public String getMsgCode() {
		return msgCode;
	}

	public static IotMessageCodeEnum fromMsgCode(String msgCode) {
		for (IotMessageCodeEnum e : IotMessageCodeEnum.values()) {
			if (e.msgCode.equals(msgCode)) {
				return e;
			}
		}
		return null;
	}
}
