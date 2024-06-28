
package com.sds.iot.sdk.message;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.UUID;

import com.sds.iot.sdk.IotClientConfig;

/**
 * 이 클래스는 클라이언트와 서버 사이에 통신 시 사용되는 공통 메시지 구조입니다.
 * 
 * @author SDS
 *
 */
public class IotMessage {

	private String	version		= "003";

	private String	msgType		= "N";

	private String	funcType	= "0"; // 서버2.9부터는 ""도 가능

	/** siteId : 미 설정시 자동으로 IotClient 설정 값으로 설정 */
	private String	sId			= null;

	/** thingName : 미 설정시 자동으로 IotClient 설정 값으로 설정 */
	private String	tpId		= null;

	private String	tId			= null;

	private String	msgCode		= IotMessageCodeEnum.ATTRGROUP_REQ.getMsgCode();

	/** thingName : 미 설정시 자동으로 random UUID 값으로 설정 */
	private String	msgId		= null;

	private long	msgDate		= 0;
	private String	resCode		= null;
	private String	resMsg		= null;
	private String	dataFormat	= "application/json";

	private String	severity	= null;
	private String	encType		= "0";
	private String	authToken	= null;

	private byte[]	data		= null;

	public IotMessage() {
		super();
	}

	public void setDefault(IotClientConfig cfg) {
		if (sId == null) {
			sId = cfg.getSiteId();
		}
		if (tpId == null) {
			tpId = cfg.getThingName();
		}
		if (tId == null) {
			tId = tpId; // 2.8 이하 호환을 위해 필요
		}
		if (msgId == null) {
			msgId = UUID.randomUUID().toString();
		}
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	public String getFuncType() {
		return funcType;
	}

	public void setFuncType(String funcType) {
		this.funcType = funcType;
	}

	public String getSiteId() {
		return sId;
	}

	public void setSiteId(String sId) {
		this.sId = sId;
	}

	public String getThingName() {
		return tpId;
	}

	public void setThingName(String tpId) {
		this.tpId = tpId;
	}

	public String getEndNode() {
		return tId;
	}

	public void setLeafThingName(String tId) {
		this.tId = tId;
	}

	public String getMsgCode() {
		return msgCode;
	}

	public void setMsgCode(String msgCode) {
		this.msgCode = msgCode;
	}

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public long getMsgDate() {
		return msgDate;
	}

	public void setMsgDate(long msgDate) {
		this.msgDate = msgDate;
	}

	public String getResCode() {
		return resCode;
	}

	public void setResCode(String resCode) {
		this.resCode = resCode;
	}

	public String getResMsg() {
		return resMsg;
	}

	public void setResMsg(String resMsg) {
		this.resMsg = resMsg;
	}

	public String getDataFormat() {
		return dataFormat;
	}

	public void setDataFormat(String dataFormat) {
		this.dataFormat = dataFormat;
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}

	public String getEncType() {
		return encType == null || encType.isEmpty() ? "0" : encType;
	}

	public void setEncType(String encType) {
		this.encType = encType;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public String getDataString() {
		if (data == null) {
			return null;
		}

		// IF STRING CONV NOT AVAILABLE DATA FORMAT (OR COMPORESS / ENCRYPTED DATA..), 
		// BASE64 ENCODE.
		if (isStringData()) {
			return new String(data, Charset.defaultCharset());
		} else {
			//return java.util.Base64.getEncoder().encodeToString(data);
			return org.apache.commons.codec.binary.Base64.encodeBase64String(data);
		}
	}

	public void setDataString(String dataStr) {
		if (dataStr == null) {
			this.data = null;
		}

		// IF STRING CONV NOT AVAILABLE DATA FORMAT (OR COMPORESS / ENCRYPTED DATA..), 
		// BASE64 DECODE.
		if (isStringData()) {
			this.data = dataStr.getBytes(Charset.defaultCharset());
		} else {
			//this.data = java.util.Base64.getDecoder().decode(dataStr);
			this.data = org.apache.commons.codec.binary.Base64.decodeBase64(dataStr);
		}
	}

	private boolean isStringData() {
		if ("0".equals(this.getEncType())) {
			if (this.getDataFormat() == null || this.getDataFormat().isEmpty()
					|| "application/json".equalsIgnoreCase(this.getDataFormat())
					|| "application/xml".equalsIgnoreCase(this.getDataFormat())
					|| "application/x-delimiter".equalsIgnoreCase(this.getDataFormat())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("IotMessage [");
		if (msgType != null)
			builder.append("msgType=").append(msgType).append(", ");
		if (msgCode != null)
			builder.append("msgCode=").append(msgCode).append(", ");
		if (msgId != null)
			builder.append("msgId=").append(msgId).append(", ");
		if (tId != null)
			builder.append("tId=").append(tId).append(", ");
		if (resCode != null)
			builder.append("resCode=").append(resCode).append(", ");
		if (resMsg != null)
			builder.append("resMsg=").append(resMsg).append(", ");
		if (encType != null)
			builder.append("encType=").append(encType).append(", ");

		builder.append("dataLength=").append(data == null ? 0 : data.length);
		builder.append("]");
		return builder.toString();
	}

	public String toStringFull() {
		StringBuilder builder = new StringBuilder();
		builder.append("IotMessage [");
		if (version != null)
			builder.append("version=").append(version).append(", ");
		if (msgType != null)
			builder.append("msgType=").append(msgType).append(", ");
		if (funcType != null)
			builder.append("funcType=").append(funcType).append(", ");
		if (sId != null)
			builder.append("sId=").append(sId).append(", ");
		if (tpId != null)
			builder.append("tpId=").append(tpId).append(", ");
		if (tId != null)
			builder.append("tId=").append(tId).append(", ");
		if (msgCode != null)
			builder.append("msgCode=").append(msgCode).append(", ");
		if (msgId != null)
			builder.append("msgId=").append(msgId).append(", ");
		builder.append("msgDate=").append(msgDate).append(", ");
		if (resCode != null)
			builder.append("resCode=").append(resCode).append(", ");
		if (resMsg != null)
			builder.append("resMsg=").append(resMsg).append(", ");
		if (dataFormat != null)
			builder.append("dataFormat=").append(dataFormat).append(", ");
		if (severity != null)
			builder.append("severity=").append(severity).append(", ");
		if (encType != null)
			builder.append("encType=").append(encType).append(", ");
		if (authToken != null)
			builder.append("authToken=").append(authToken).append(", ");
		if (data != null) {
			builder.append("dataLength=").append(data.length).append(", ");
			builder.append("data=");
			if (encType == null || encType.isEmpty() || "0".equals(encType)) {
				builder.append(new String(data, Charset.defaultCharset()));
			} else {
				builder.append(Arrays.toString(data));
			}
		}
		builder.append("]");
		return builder.toString();
	}

	/**
	 * @return
	 */
	public IotMessage createResponse() {
		if (!"Q".equals(this.getMsgType())) {
			throw new IllegalArgumentException("'createResponse()' method can be used for a message of msgType='Q'.");
		}
		IotMessage resMsg = new IotMessage();
		resMsg.setMsgId(this.getMsgId());
		resMsg.setMsgType("A");
		resMsg.setSiteId(this.getSiteId());
		//resMsg.setThingName(this.getThingName());
		resMsg.setLeafThingName(this.getEndNode());
		if (this.getMsgCode() != null) {
			if (this.getMsgCode().startsWith("MSGBA")) {
				String numStr = this.getMsgCode().substring(5);
				int nextNum = Integer.parseInt(numStr) + 1; 
				String nextNumStr = String.format("MSGBA%07d", nextNum);
				resMsg.setMsgCode(nextNumStr);
			} else {
				resMsg.setMsgCode(this.getMsgCode());
			}
		}
		return resMsg;
	}

}
