
package com.sds.iot.sdk.message.headerformat;

import java.nio.charset.Charset;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.iot.sdk.message.IotMessage;

/**
 * 이 클래스는 메시지의 헤더 포맷(Delimiter인 경우) 변환 시 호출되는 함수를 정의합니다.
 * 
 * @author SDS
 */
public class DelimiterHeaderFormatConverter implements IHeaderFormatConverter {
	private static final Logger	LOGGER				= LoggerFactory.getLogger(DelimiterHeaderFormatConverter.class);

	private static final String	HEADER_DELIMITER_S	= "|";
	private static final byte	HEADER_DELIMITER	= '|';

	//version|msgType|funcType|sId|tpId|tId
	//|msgCode|msgId|msgDate|resCode|resMsg|dataFormat
	//|severity|encType|authToken
	//|data-length
	//|data

	private static void appendHeader(StringBuilder sb, Object obj) {
		if (obj != null) {
			sb.append(obj);	
		}
		sb.append(HEADER_DELIMITER_S);
	}
	
	@Override
	public byte[] convertToBytes(IotMessage msg) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[CONV] convertToBytes   - before [{}]", msg.toStringFull());
		}
		
		// create header bytes 
		StringBuilder sb = new StringBuilder();
		appendHeader(sb, msg.getVersion());
		appendHeader(sb, msg.getMsgType());
		appendHeader(sb, msg.getFuncType());
		appendHeader(sb, msg.getSiteId());
		appendHeader(sb, msg.getThingName());
		appendHeader(sb, msg.getEndNode());
		appendHeader(sb, msg.getMsgCode());
		appendHeader(sb, msg.getMsgId());
		appendHeader(sb, msg.getMsgDate());
		appendHeader(sb, msg.getResCode());
		appendHeader(sb, msg.getResMsg());
		appendHeader(sb, msg.getDataFormat());
		appendHeader(sb, msg.getSeverity());
		appendHeader(sb, msg.getEncType());
		appendHeader(sb, msg.getAuthToken());

		// delimiter format 의 경우 body length 를 함께 보냄 
		byte[] bodyBytes = msg.getData();
		appendHeader(sb, bodyBytes == null ? 0 : bodyBytes.length);

		byte[] headerBytes = sb.toString().getBytes(Charset.defaultCharset());

		// headerBytes + bodyBytes
		byte[] allBytes;
		if (bodyBytes == null) {
			allBytes = headerBytes;
		} else {
			allBytes = new byte[headerBytes.length + bodyBytes.length];
			System.arraycopy(headerBytes, 0, allBytes, 0, headerBytes.length);
			System.arraycopy(bodyBytes, 0, allBytes, headerBytes.length, bodyBytes.length);
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.trace("[CONV] convertToBytes   - after  [{}]", new String(allBytes, Charset.defaultCharset()));
		}
		return allBytes;
	}

	@Override
	public IotMessage convertFromBytes(byte[] bytes) throws IllegalStateException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.trace("[CONV] convertFromBytes - before [{}]", new String(bytes, Charset.defaultCharset()));
		}

		int startPos;
		int endPos;
		IotMessage msg = new IotMessage();

		startPos = 0;
		endPos = getNextHeaderItemEndPosition(bytes, startPos);
		msg.setVersion(getNextHeaderItem(bytes, startPos, endPos));

		startPos = endPos + 1;
		endPos = getNextHeaderItemEndPosition(bytes, startPos);
		msg.setMsgType(getNextHeaderItem(bytes, startPos, endPos));

		startPos = endPos + 1;
		endPos = getNextHeaderItemEndPosition(bytes, startPos);
		msg.setFuncType(getNextHeaderItem(bytes, startPos, endPos));

		startPos = endPos + 1;
		endPos = getNextHeaderItemEndPosition(bytes, startPos);
		msg.setSiteId(getNextHeaderItem(bytes, startPos, endPos));

		startPos = endPos + 1;
		endPos = getNextHeaderItemEndPosition(bytes, startPos);
		msg.setThingName(getNextHeaderItem(bytes, startPos, endPos));

		startPos = endPos + 1;
		endPos = getNextHeaderItemEndPosition(bytes, startPos);
		msg.setLeafThingName(getNextHeaderItem(bytes, startPos, endPos));

		startPos = endPos + 1;
		endPos = getNextHeaderItemEndPosition(bytes, startPos);
		msg.setMsgCode(getNextHeaderItem(bytes, startPos, endPos));

		startPos = endPos + 1;
		endPos = getNextHeaderItemEndPosition(bytes, startPos);
		msg.setMsgId(getNextHeaderItem(bytes, startPos, endPos));

		startPos = endPos + 1;
		endPos = getNextHeaderItemEndPosition(bytes, startPos);
		msg.setMsgDate(Long.parseLong(getNextHeaderItem(bytes, startPos, endPos)));

		startPos = endPos + 1;
		endPos = getNextHeaderItemEndPosition(bytes, startPos);
		msg.setResCode(getNextHeaderItem(bytes, startPos, endPos));

		startPos = endPos + 1;
		endPos = getNextHeaderItemEndPosition(bytes, startPos);
		msg.setResMsg(getNextHeaderItem(bytes, startPos, endPos));

		startPos = endPos + 1;
		endPos = getNextHeaderItemEndPosition(bytes, startPos);
		msg.setDataFormat(getNextHeaderItem(bytes, startPos, endPos));

		startPos = endPos + 1;
		endPos = getNextHeaderItemEndPosition(bytes, startPos);
		msg.setSeverity(getNextHeaderItem(bytes, startPos, endPos));

		startPos = endPos + 1;
		endPos = getNextHeaderItemEndPosition(bytes, startPos);
		msg.setEncType(getNextHeaderItem(bytes, startPos, endPos));

		startPos = endPos + 1;
		endPos = getNextHeaderItemEndPosition(bytes, startPos);
		msg.setAuthToken(getNextHeaderItem(bytes, startPos, endPos));

		startPos = endPos + 1;
		endPos = getNextHeaderItemEndPosition(bytes, startPos);
		int bodySize = Integer.parseInt(getNextHeaderItem(bytes, startPos, endPos));

		// body
		startPos = endPos + 1;
		if (bodySize != bytes.length - startPos) {
			LOGGER.error("bodySize={} / bytes.length={} / startPos={}", bodySize, bytes.length, startPos);
		}
		if (startPos < bytes.length) {

			byte[] bodyBytes = Arrays.copyOfRange(bytes, startPos, bytes.length);

			msg.setData(bodyBytes);

		} else {
			LOGGER.trace("[CONV] No body data");
		}

		// TODO (SDK) 메시지 수신 후 validate 필요할지?

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[CONV] convertFromBytes - after  [{}]", msg.toStringFull());
		}

		return msg;
	}

	private static int getNextHeaderItemEndPosition(byte[] bytes, int startPos) {
		int endPos = -1;
		for (int i = startPos; i < bytes.length; i++) {
			if (bytes[i] == HEADER_DELIMITER) {
				endPos = i;
				break;
			}
		}
		if (endPos == -1) {
			throw new IllegalStateException("Cannot parse IotMessage!");
		}
		return endPos;
	}

	private static String getNextHeaderItem(byte[] bytes, int startPos, int endPos) {
		return new String(Arrays.copyOfRange(bytes, startPos, endPos), Charset.defaultCharset());
	}

}
