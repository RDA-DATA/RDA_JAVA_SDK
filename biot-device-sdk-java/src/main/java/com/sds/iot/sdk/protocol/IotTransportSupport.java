
package com.sds.iot.sdk.protocol;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.iot.sdk.callback.IResponseCallback;
import com.sds.iot.sdk.exception.IotException;
import com.sds.iot.sdk.message.IotMessage;

/**
 * 이 클래스는 각 프로토콜(MQTT)에서 연결 및 메시지 송/수신에 대한 로깅, 체크 함수를 제공합니다.
 * 
 * @author SDS
 */
public class IotTransportSupport {
	private static final Logger			LOGGER	= LoggerFactory.getLogger(IotTransportSupport.class);
	private static final SecureRandom	RAND	= new SecureRandom();

	private static String getEndNode(IotMessage msg) {
		if (msg.getEndNode() == null || msg.getEndNode().isEmpty() || msg.getThingName().equals(msg.getEndNode())) {
			return "";
		}
		return "<" + msg.getEndNode() + ">";
	}

	private static String getResCode(IotMessage msg) {
		if (!"A".equals(msg.getMsgType())) {
			return "";
		}
		return msg.getResCode() != null && !"".equals(msg.getResCode()) ? ", resCode=" + msg.getResCode() : "";
	}

	private static int getDataLength(IotMessage msg) {
		return msg.getData() == null ? 0 : msg.getData().length;
	}

    /**
	 * 클라이언트 메시지 전송 로깅
	 * @param msg
	 *        메시지
	 *        
	 * @see IotMessage
	 */
	static void printSendLog(IotMessage msg) {
		LOGGER.info("<- SEND {}: msgId={}, msgType={}, msgCode={}{}, dataLength={}", getEndNode(msg), msg.getMsgId(),
				msg.getMsgType(), msg.getMsgCode(), getResCode(msg), getDataLength(msg));
	}

    /**
	 * 클라이언트 메시지 전송 실패 로깅
	 * @param msg
	 *        메시지
	 * @param errMsg
	 *        오류 메시지
	 * @param e
	 *        오류 객체
	 */
	static void printSendFailLog(IotMessage msg, String errMsg, Throwable e) {
		if (e != null && LOGGER.isDebugEnabled()) {
			// 스택 트레이스 포함 로그 (debug 모드 시)
			LOGGER.error("<X SEND {}: msgId={}, msgType={}, msgCode={}{}, dataLength={} : {}", getEndNode(msg), msg.getMsgId(),
					msg.getMsgType(), msg.getMsgCode(), getResCode(msg), getDataLength(msg), errMsg, e);
		} else {
			// 스택 트레이스 미포함 로그 
			LOGGER.error("<X SEND {}: msgId={}, msgType={}, msgCode={}{}, dataLength={} : {}", getEndNode(msg), msg.getMsgId(),
					msg.getMsgType(), msg.getMsgCode(), getResCode(msg), getDataLength(msg), errMsg);

		}
	}

	/**
	 * 서버 응답 로깅
	 * @param msg
	 *        메시지
	 *        
	 * @see IotMessage
	 */
	static void printRecvLog(IotMessage msg) {
		if ("A".equals(msg.getMsgType()) && !msg.getResCode().startsWith("2")) {
			LOGGER.warn("-> RECV {}: msgId={}, msgType={}, msgCode={}{}, dataLength={}", getEndNode(msg), msg.getMsgId(),
					msg.getMsgType(), msg.getMsgCode(), getResCode(msg), getDataLength(msg));
		} else {
			LOGGER.info("-> RECV {}: msgId={}, msgType={}, msgCode={}{}, dataLength={}", getEndNode(msg), msg.getMsgId(),
					msg.getMsgType(), msg.getMsgCode(), getResCode(msg), getDataLength(msg));
		}
	}

	/**
	 * 서버 응답 실패 로깅.
	 * @param msgId
	 *        메시지 ID
	 * @param errMsg
	 *        오류 메시지
	 */
	static void printRecvFailLog(String msgId, String errMsg) {
		LOGGER.error("X> RECV : msgId={} : {}", msgId, errMsg);
	}

	/**
	 * 경과시간에 따른 서버 응답 콜백 로깅.
	 * @param cb
	 *        응답 콜백
	 * @param msgId
	 *        메시지 ID
	 */
	static void printElapsed(IResponseCallback cb, String msgId) {
		long elapsed = extractCallbackElapsedTime(cb.getCreateTime());
		if (elapsed > 2000) {
			// 응답 수신에 2초 이상 소요되었으면 warn 로그로 출력.
			LOGGER.warn("callback action found. corrId=[{}], created=[{}], elapsed=[{}], class=[{}]", msgId,
					formatDefaultLocale("HH:mm:ss", cb.getCreateTime()), elapsed, cb.getClass().getSimpleName());
		} else if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("callback action found. corrId=[{}], created=[{}], elapsed=[{}], class=[{}]", msgId,
					formatDefaultLocale("HH:mm:ss", cb.getCreateTime()), elapsed, cb.getClass().getSimpleName());
		}
	}

	/** for scouter */
	static long extractCallbackElapsedTime(long callbackCreatedTime) {
		return System.currentTimeMillis() - callbackCreatedTime;
	}

	/**
	 * 지역(Locale)에 따른 날짜, 시간 포맷 변환
	 * @param pattern
	 *        변경할 포맷
	 * @param millis
	 *        시간 (long type)
	 * @return 포맷에 따른 날짜 또는 시간
	 */
	static String formatDefaultLocale(String pattern, long millis) {
		//return String.valueOf(millis);
		//org.joda.time.format.DateTimeFormatter formatter = org.joda.time.format.DateTimeFormat.forPattern(pattern).withLocale(Locale.getDefault());
		//return formatter.print(millis);
		org.apache.commons.lang3.time.FastDateFormat df = org.apache.commons.lang3.time.FastDateFormat.getInstance(pattern,
				TimeZone.getDefault(), Locale.getDefault());
		return df.format(millis);
	}

	/**
	 * 재연결 시도 여부 확인
     * 
	 * @param retryCfg
	 *        재연결 설정
	 * @param currentRetryTimes
	 *        현재 재시도 횟수
	 * @param errMsg
	 *        오류 메시지
	 * @param cause
	 *        오류 객체
	 * @return 재연결 시도 여부 (true/false)
	 */
	static boolean waitAndRetryOrNot(RetryConfig retryCfg, int currentRetryTimes, String errMsg, Exception cause) {
		long millis = calculateConnectRetryDelay(retryCfg, currentRetryTimes);
		if (millis == -1) {
			LOGGER.error("NO MORE RETRY. {}", errMsg, cause);
			throw new IotException("500", cause);
		} else {
			LOGGER.warn("CONNECTION RETRY {}/{} (after sleep {} ms.)", currentRetryTimes + 1, retryCfg.getConnectRetryMaxCount(),
					millis);
			try {
				Thread.sleep(millis);
			} catch (InterruptedException e1) {
				LOGGER.warn(e1.toString());
				return false;
			}
		}
		return true;
	}

	/**
	 * 재연결에 대한 지연 시간(초) 계산합니다.
	 * 
	 * @param retryCfg
	 *        재연결 설정
	 * @param currentRetryTimes
	 *        : for-loop index. 0 ~ connectRetryMaxCount
	 * @return 재연결 지연 시간(초)
	 */
	public static long calculateConnectRetryDelay(RetryConfig retryCfg, int currentRetryTimes) {
		if (currentRetryTimes >= retryCfg.getConnectRetryMaxCount()) {
			return -1;
		}

		double millis = Math.pow(2.0, currentRetryTimes) * retryCfg.getConnectRetryBaseDelay();

		if (retryCfg.getConnectRetryDelayRandomRange() > 0) {
			millis = millis + RAND.nextInt(retryCfg.getConnectRetryDelayRandomRange());
		}

		if (millis > retryCfg.getConnectRetryMaxDelay()) {
			millis = retryCfg.getConnectRetryMaxDelay();
		} else if (millis < retryCfg.getConnectRetryBaseDelay()) {
			millis = retryCfg.getConnectRetryBaseDelay();
		}
		return (long) millis;
	}
}
