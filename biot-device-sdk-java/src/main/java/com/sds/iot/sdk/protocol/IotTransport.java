
package com.sds.iot.sdk.protocol;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.iot.sdk.ICustomMessageListener;
import com.sds.iot.sdk.IotClientConfig;
import com.sds.iot.sdk.auth.IAuth;
import com.sds.iot.sdk.callback.AbstractCallback;
import com.sds.iot.sdk.callback.IResponseCallback;
import com.sds.iot.sdk.exception.IotException;
import com.sds.iot.sdk.message.IotMessage;
import com.sds.iot.sdk.message.MessageConverter;

/**
 * 이 클래스는 전송계층(Transport layer)에서 연결 관련 함수를 제공합니다.
 * 
 * @author SDS
 */
public final class IotTransport implements ITransportListener {
	private static final Logger						LOGGER							= LoggerFactory.getLogger(IotTransport.class);

	private final IProtocol							protocol;
	private final MessageConverter					converter;
	private final ICustomMessageListener			customListener;

	private IAuth									auth;
	private IotClientConfig							cfg;

	/** key:msgId, value:callback */
	private final Map<String, IResponseCallback>	callbackRepo					= new ConcurrentHashMap<>();

	/** 서버에서 인증 성공 여부 */
	private boolean									authorized						= false;

	/** 마지막에 서버로부터 메시지를 받은 시각 */
	private long									lastMessageReceived				= 0L;

	/** 이 값은 서버와의 연결이 한번 성공하면 true 가 되며, 명시적으로 disconnect() 를 수행하는 경우에만 false 가 된다. */
	private boolean									autoConnectRetry				= false;

	private Runnable								responseCallbackCheckRunnable	= null;
	private Runnable								keepAliveMessageSendRunable		= null;

	/**
	 * @param protocol
	 * @param converter
	 */
	public IotTransport(IProtocol protocol, MessageConverter converter, ICustomMessageListener customListener) {
		this.protocol = protocol;
		this.converter = converter;
		this.customListener = customListener;
	}

	/* ======================================================================================= */

	/**
	 * 서버 연결 시도. 연결 불가 시 연결 재시도 기능 포함.
	 * (단, 해당 프로토콜로 메시지 송수신이 필요한 인증 프로세스는 포함되지 않음)
	 * 
	 * @param auth
	 * @param cfg
	 */
	public void connect(IAuth auth, IotClientConfig cfg) {
		this.auth = auth;
		this.cfg = cfg;

		tryToGetCredential(cfg.getRetryCfg(), auth);

		this.autoConnectRetry = true;

		connectInternal();

	}

	private void tryToGetCredential(RetryConfig retryCfg, IAuth auth) {
		for (int i = 0; i <= retryCfg.getConnectRetryMaxCount(); i++) {

			try {
				auth.requestCredential();
				break;

			} catch (IotException e) {

				// 클라이언트 오류이면 재시도 하지 않음 
				if (e.isClientError()) {
					LOGGER.error("FAIL TO CONNECT (CLIENT CREDENTIAL PROBLEM) : {}", e.toString());
					throw e;
				}

				if (IotTransportSupport.waitAndRetryOrNot(retryCfg, i, "FAIL TO CONNECT (CREDENTIAL) : " + e.getMessage(), e)) {
					continue;
				} else {
					break;
				}
			}
		}
	}

	/**
	 * 서버 연결 수행 (재연결 시에는 이 함수가 호출됨)
	 */
	private void connectInternal() {
		if (!autoConnectRetry) {
			return;
		}
		LOGGER.info("TRY TO CONNECT... <{}>", cfg.toStringShort());

		protocol.init(cfg, converter, this, auth);

		tryToConnect(cfg.getRetryCfg());

		LOGGER.info("CONNECTED! <{}>", cfg.toStringShort());
	}

	private void tryToConnect(RetryConfig retryCfg) {
		for (int i = 0; i <= retryCfg.getConnectRetryMaxCount(); i++) {
			try {

				protocol.connect();
				break;

			} catch (IotException e) {

				// 클라이언트 오류이면 재시도 하지 않음 
				if (e.isClientError()) {
					LOGGER.error("FAIL TO CONNECT (CLIENT PROBLEM) : {}", e.toString());
					throw e;
				}

				if (IotTransportSupport.waitAndRetryOrNot(retryCfg, i, "FAIL TO CONNECT : " + e.getMessage(), e)) {
					continue;
				} else {
					break;
				}
			}
		}
	}

	/**
	 * 응답 콜백 타임아웃 체크 스레드 생성하고 시작.
	 * 한번 생성된 스레드는 명시적으로 disconnect() 를 호출하거나, 인터럽트 발생 전까지는 종료되지 않는다.
	 */
	public void startResponseCallbackTimeoutCheckThread() {
		if (this.responseCallbackCheckRunnable != null) {
			LOGGER.debug("Response callback timeout check thread is already started.");
			return;
		}
		// 체크 주기는 최소 200ms. ~ 최대 2000ms. 사이 값 권장  
		this.responseCallbackCheckRunnable = new ResponseCallbackCheckRunnable(this, callbackRepo, 500L);
		Thread t = new Thread(responseCallbackCheckRunnable, "biot-resCallbackTimeoutChecker");
		t.start();
	}

	/**
	 * Keep Alive 메시지 송신 스레드 생성하고 시작.
	 * 한번 생성된 스레드는 명시적으로 disconnect() 를 호출하거나, 인터럽트 발생 전까지는 종료되지 않는다.
	 */
	public void startKeepAliveMessageSendThread() {
		if (this.keepAliveMessageSendRunable != null) {
			LOGGER.debug("Keep alive message send thread is already started.");
			return;
		}
		if (cfg.getKeepAliveInterval() <= 0) {
			LOGGER.info("Keep-alive message will not be sent. (keepAliveInterval={})", cfg.getKeepAliveInterval());
			return;
		}
		this.keepAliveMessageSendRunable = new KeepAliveMessageSendRunnable(this, cfg.getKeepAliveInterval());
		Thread t = new Thread(keepAliveMessageSendRunable, "biot-keepAliveMsgSender");
		t.start();
	}

	/**
	 * 서버와의 연결 해제.
	 * 자동 연결 재시도 기능이 off 되며,
	 * response callback check 스레드와 keep alive message send 스레드가 종료된다.
	 */
	public void disconnect() {
		this.responseCallbackCheckRunnable = null;
		this.keepAliveMessageSendRunable = null;
		this.autoConnectRetry = false;
		// 참고: autoConnectRetry 값이 false 인 경우, response callback check 스레드와 keep alive message send 스레드가 종료됨.

		LOGGER.info("DISCONNECT!");

		try {
			protocol.disconnect();
		} catch (Exception e) {
			LOGGER.debug("EXCEPTION WHILE DISCONNECTING : {}", e.toString());
		}

	}

	/* ======================================================================================= */

	/**
	 * 이 값은 인증이 완료되지 않아도 connected 상태는 true 일 수 있다.
	 * 
	 * @return 서버와 연결 여부
	 */
	boolean isConnected() {
		boolean ret = protocol.isConnected();
		if (!ret && LOGGER.isDebugEnabled()) {
			LOGGER.trace("isConnected={}", ret);
		}
		return ret;
	}

	/**
	 * @return 인증 여부 (true/false)
	 */
	boolean isAuthorized() {
		if (isConnected()) {
			return this.authorized;
		}
		return false;
	}

	/**
	 * @param authorized
	 *        인증 여부
	 */
	public void setAuthorized(boolean authorized) {
		this.authorized = authorized;
	}

	/**
	 * @return 마지막에 서버로부터 메시지를 받은 시각
	 */
	long getLastMessageReceived() {
		return this.lastMessageReceived;
	}

	/**
	 * 이 값은 서버와의 연결이 한번 성공하면 true 가 되며,
	 * 명시적으로 disconnect() 를 수행하는 경우에만 false 가 된다.
	 * 
	 * @return 자동 재연결 설정 여부.
	 */
	boolean isAutoConnectRetry() {
		return this.autoConnectRetry;
	}

	/* ======================================================================================= */

	/**
	 * 메시지를 서버로 전송합니다.
	 * 단, 요청(Q) 타입의 메시지는 전송할 수 없습니다.
	 * 
	 * @param msg
	 *        메시지
	 * @return 메시지 전송 여부 (true/false)
	 */
	public boolean send(IotMessage msg) {
		if ("Q".equals(msg.getMsgType())) {
			throw new IllegalArgumentException(
					"'send()' method can be used for a message of msgType='N' or 'A'. A message of msgType='Q' can be used 'call()' or 'syncCall()' method!");
		}
		msg.setDefault(cfg);
		msg.setAuthToken(auth.getAuthToken());

		if (!isConnected()) {
			IotTransportSupport.printSendFailLog(msg, "NOT CONNECTED", null);
			return false;
		}

		try {
			IotTransportSupport.printSendLog(msg);
			protocol.send(msg);
			return true;
		} catch (Exception e) {
			IotTransportSupport.printSendFailLog(msg, "FAIL TO SEND : " + e.toString(), e);
			return false;
		}
	}

	/**
	 * 송신한 요청 메시지에 대한 응답 메시지 수신 후,
	 * 특정 작업을 수행해야 하는 경우 호출합니다.
	 * 단, 해당 메서드는 blocking 되지 않으므로 멀티스레드에 유의해야 합니다.
	 * 
	 * @param msg
	 *        메시지
	 * @param responseCallback
	 *        응답 메시지 수신 이후의 액션
	 * @return 작업 호출 여부 (응답 이후, true/false)
	 */
	public boolean call(IotMessage msg, IResponseCallback responseCallback) {
		msg.setMsgType("Q");
		msg.setDefault(cfg);
		msg.setAuthToken(auth.getAuthToken());

		if (!isConnected()) {
			IotTransportSupport.printSendFailLog(msg, "NOT CONNECTED", null);
			return false;
		}

		if (callbackRepo.containsKey(msg.getMsgId())) {
			LOGGER.warn("store callback. (overwrite previous callback. msgId={})", msg.getMsgId());
		}
		if (callbackRepo.size() >= 10 && LOGGER.isDebugEnabled()) {
			LOGGER.debug("store callbak. (waiting callbacks={})", callbackRepo.size());
		}

		callbackRepo.put(msg.getMsgId(), responseCallback);

		try {
			IotTransportSupport.printSendLog(msg);
			protocol.send(msg);
			return true;
		} catch (Exception e) {
			IotTransportSupport.printSendFailLog(msg, "FAIL TO CALL : " + e.toString(), e);
			return false;
		}

	}

	/**
	 * 송신한 요청 메시지에 대한 응답 메시지를 수신하여 리턴 값으로 받고자 할 때 사용합니다.
	 * 서버에서 응답을 늦게 줄 수 있거나, 성능에 유의해야 하는 경우,
	 * syncCall() 이 아닌 call() 메서드의 사용을 권장합니다.
	 * 
	 * @param msg
	 *        메시지
	 * @param timeoutSeconds
	 *        time out 시간 (초)
	 * @return return null if response timed out.
	 */
	public IotMessage syncCall(IotMessage msg, int timeoutSeconds) {
		msg.setMsgType("Q");
		msg.setDefault(cfg);
		msg.setAuthToken(auth.getAuthToken());

		if (!isConnected()) {
			IotTransportSupport.printSendFailLog(msg, "NOT CONNECTED", null);
			return null;
		}

		BlockingQueue<IotMessage> queue = new ArrayBlockingQueue<>(1);
		SyncCallback cb = new SyncCallback(queue);
		long timeoutMillis = timeoutSeconds * 1000L;
		cb.setTimeout(timeoutMillis);

		// 송신
		boolean result = call(msg, cb);
		if (!result) {
			return null;
		}

		// 수신대기
		IotMessage resMsg = null;
		try {
			resMsg = (IotMessage) queue.poll(timeoutSeconds, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			LOGGER.warn("InterruptedException : {}", e.toString());
		}

		// resMsg == null 인 경우, 지정한 시간 내에 응답을 받지 못한, 응답 타임아웃 케이스
		return resMsg;
	}

	/**
	 * 이 클래스는 동기화 통신 시 콜백에 대해 정의합니다.
	 * 
	 * @author SDS
	 */
	static class SyncCallback extends AbstractCallback {
		private final BlockingQueue<IotMessage> queue;

		public SyncCallback(BlockingQueue<IotMessage> queue) {
			this.queue = queue;
		}

		@Override
		public void action(IotMessage resMomMessage) throws Exception {
			LOGGER.trace("SyncCallback action");
			queue.put(resMomMessage);
		}

		@Override
		public void timeoutAction() {
			// CorrelationInfoRepo 에서 checkTimeout() 타임아웃 시 호출됨
			LOGGER.trace("SyncCallback timeoutAction");
		}
	}

	/* ======================================================================================= */

	@Override
	public void onMessageReceived(IotMessage msg) {

		this.lastMessageReceived = System.currentTimeMillis();

		IotTransportSupport.printRecvLog(msg);

		// A 타입 메시지 수신
		if ("A".equals(msg.getMsgType())) {
			IResponseCallback cb = callbackRepo.remove(msg.getMsgId());
			if (cb == null) {
				LOGGER.warn("Answer type message received. but no callback action found. It can be timed out and removed.");
				// return;
				throw new IllegalStateException("Cannot found a callback object for msgId=" + msg.getMsgId() + " ("
						+ IotTransportSupport.formatDefaultLocale("yyyy-MM-dd HH:mm:ss", msg.getMsgDate()) + ")");
			}

			// 콜백이 수행되기 전까지 소요된 시간 출력
			IotTransportSupport.printElapsed(cb, msg.getMsgId());

			// 콜백 수행
			LOGGER.trace("callback action start");
			try {
				cb.action(msg);
				LOGGER.trace("callback action end");
				return;
			} catch (Exception e) {
				LOGGER.error("FAIL TO EXECUTE CALLBACK : {}", e.toString(), e);
			}
		}

		// N/Q 타입 메시지 수신
		if (customListener == null) {
			LOGGER.warn("NO CUSTOM MESSAGE LISTENER!!");
			LOGGER.warn("YOU NEED TO SET 'customomMessageListener' IN 'IotClient' Object.");
			LOGGER.warn("REFER TO 'ICustomMessageListener' INTERFACE AND 'DefaultMessageListener' CLASS.");
		} else if ("N".equals(msg.getMsgType())) {
			try {
				customListener.onNotificationReceived(msg);
			} catch (Exception e) {
				LOGGER.error("EXCEPTION OCCURRED 'onNotificationReceived' : {}", e.toString(), e);
			}
		} else if ("Q".equals(msg.getMsgType())) {
			try {
				customListener.onRequestReceived(msg);
			} catch (Exception e) {
				LOGGER.error("EXCEPTION OCCURRED 'onRequestReceived' : {}", e.toString(), e);
			}
		} else {
			// 그럴리는 없지만 서버가 N,Q,A 외의 타입 메시지를 보낸 경우
			LOGGER.error("INVALID MESSAGE TYPE RECEIVED. msgType={}", msg.getMsgType());
		}

	}

	@Override
	public void onMessageSent(Throwable e) {
		LOGGER.trace("onMessageSent");

	}

	@Override
	public void onConnectionLost(String cause) {
		LOGGER.info("onConnectionLost : {}", cause);

		this.authorized = false;
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			LOGGER.trace("{}", e1.getMessage());
		}

		// RECONNECT
		// 단, 중간에 연결이 끊기고 reconnect 시에는 프로토콜에 따라 별도 스레드에서 동작할 수 있음.
		connectInternal();
		// IoT Core 재기동 될 수 있기 때문에 인증도 다시 받는 구조로 수정.
		processDeviceAuth();
	}

	/**
	 * B.IoT에 인증 관련 메시지를 주고 받으면서 디바이스 인증을 수행합니다.
	 */
	public void processDeviceAuth() {
		// IoTCoreClient에 있었던 함수를 여기로 이동했음.
		// 메시지 전송이 필요한 디바이이스 인증 프로세스
		LOGGER.info("AUTHORIZATION... <{}> [{}]", cfg.toStringShort(), auth.getAuthTypeName());
		int seq;
		for (seq = 1; seq <= 2; seq++) {
			IotMessage msg = auth.makeDeviceAuthMessage(seq);
			if (msg == null) {
				break;
			} else {
				if (seq == 1) {
					LOGGER.info("START DEVICE AUTHORIZATION... <{}> [{}]", cfg.toStringShort(), auth.getAuthTypeName());
				}

				IotMessage resMsg = syncCall(msg, cfg.getDefaultSyncResponseTimeout());
				if (resMsg == null) {
					// 요청 메시지 전송 이후 서버와의 연결이 끊긴 경우도 포함됨
					throw new IotException("500", "Fail to process device authentication!");
				}
				if (LOGGER.isDebugEnabled()) {
					LOGGER.trace("Device Auth Response Message Received : resCode={}", resMsg.getResCode());
				}

				// 인증 응답 메시지에 따른 처리 수행
				auth.onAuthResponseMessageReceived(resMsg);

				if (!resMsg.getResCode().startsWith("2")) {
					throw new IotException(resMsg.getResCode(),
							"Fail to process device authentication! resCode=" + resMsg.getResCode());
				}

			}
		}
		if (seq != 1) {
			LOGGER.info("DEVICE IS AUTHORIZED SUCCESSFULLY! <{}> [{}]", cfg.toStringShort(), auth.getAuthTypeName());

			setAuthorized(true);
		}
	}
}
