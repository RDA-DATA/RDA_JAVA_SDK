
package com.sds.iot.sdk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.iot.sdk.IFactory.DefaultFactory;
import com.sds.iot.sdk.auth.IAuth;
import com.sds.iot.sdk.callback.IResponseCallback;
import com.sds.iot.sdk.exception.IotException;
import com.sds.iot.sdk.message.IotMessage;
import com.sds.iot.sdk.message.MessageConverter;
import com.sds.iot.sdk.message.compress.IDataCompressor;
import com.sds.iot.sdk.message.encrypt.IDataEncryptor;
import com.sds.iot.sdk.message.headerformat.IHeaderFormatConverter;
import com.sds.iot.sdk.protocol.IProtocol;
import com.sds.iot.sdk.protocol.IProtocolConfig;
import com.sds.iot.sdk.protocol.IotTransport;
import com.sds.iot.sdk.protocol.RetryConfig;

/**
 * 이 클래스는 IoT 서버와 연결하여 아래와 같은 기능들을 제공하는 코어 클라이언트 입니다.
 * <code>connect()</code>,
 * <code>disconnect()</code>,
 * <code>send()</code>,
 * <code>call()</code>,
 * <code>syncCall()</code>
 * 
 * @author SDS
 */
public class IotCoreClient {
	private static final Logger		LOGGER			= LoggerFactory.getLogger(IotCoreClient.class);

	private final IotClientConfig	cfg;
	private final IAuth				auth;

	private IotTransport			transport		= null;
	private ICustomMessageListener	customListener	= null;

	/* ======================================================================================= */

	/**
	 * @param auth
     *        인증정보
	 * @param cfg
     *        클라이언트 설정 
	 */
	public IotCoreClient(IAuth auth, IotClientConfig cfg) {
		this.cfg = cfg;
		this.auth = auth;
		this.auth.setConfig(cfg);
	}

	/* ======================================================================================= */

	/**
	 * 확장 버전의 IotClient 사용 시,
	 * 하위 클래스에서 이 메서드를 override 하여 Factory 인스턴스를 교체하도록 합니다.
	 * 
	 * @return 디폴트 팩토리 인스턴스
	 */
	protected IFactory getFactory() {
		return DefaultFactory.INSTANCE;
	}

	/**
	 * 확장 버전의 IotClient 사용 시,
	 * 하위 클래스에서 cfg 멤버에 접근하기 위한 메서드 입니다.
	 * 
	 * @return 설정된 cfg
	 */
	protected final IotClientConfig getCfg() {
		return this.cfg;
	}

	/* ======================================================================================= */

	/**
	 * optional.
	 * <code>IotClientConfig</code>의 RetryConfig 설정
	 * 
	 * @param retryCfg 재연결 시도와 관련된 설정
	 */
	public void setRetryConfig(RetryConfig retryCfg) {
		this.cfg.setRetryCfg(retryCfg);

	}

	/**
	 * optional.
	 * <code>IotClientConfig</code>의 ProtocolConfig 설정
	 * 
	 * @param protocolCfg 프로토콜과 관련된 설정
	 */
	public void setProtocolConfig(IProtocolConfig protocolCfg) {
		this.cfg.setProtocolCfg(protocolCfg);
	}

	/**
	 * 서버로부터 요청/통보 메시지 수신 시 수행할 작업 정의를 위한 리스너를 설정한다.
	 * 만약, 서버로부터 요청/통보 메시지를 수신하여 로직을 처리해야 하는 경우가 없다면 설정하지 않아도 무관하다.
	 * 
	 * @param customListener 커스텀 리스너
	 */
	public void setCustomMessageListener(ICustomMessageListener customListener) {
		customListener.setClient(this);
		this.customListener = customListener;
	}
	/* ======================================================================================= */

	/**
	 * 주어진 정보를 이용하여 connect 시도하며, 사물 인증 프로세스을 포함한다.
	 * connect 실패 시 설정에 따라 재시도를 수행한다.
	 * 단, 클라이언트 설정에 오류가 있거나, 최대 재시도 횟수 초과 시 익셉션을 발생시킨다.
	 */
	public void connect() throws IotException {
		try {

			initStrategy();

			printStartLog();

			// 서버 연결
			transport.connect(auth, cfg);

			// 응답 콜백 타임아웃 체크 스레드 생성
			transport.startResponseCallbackTimeoutCheckThread();

			// 메시지 전송이 필요한 사물 인증 프로세스 진행
			transport.processDeviceAuth();

			// Keep Alive 메시지 송신 스레드 생성
			transport.startKeepAliveMessageSendThread();

		} catch (IotException e) {
			//LOGGER.error("FAIL TO CONNECT!! IT MAY BE '{}' PROBLEM!!", e.isClientError() ? "CLIENT" : "SERVER");

			transport.disconnect();

			throw e;
		}
	}

	/**
	 * 프로토콜, 메시지 컨버터 등 B.IoT와의 연결등에 필요한 설정을 준비하고 관련 인스턴스들을 생성한다.
	 */
	protected void initStrategy() {
		if (this.transport != null) {
			LOGGER.debug("strateges are already inited.");
			return;
		}
		IProtocol protocol = getFactory().createProtocol(cfg.getProtocolType());

		getFactory().initProtocolCfg(cfg);

		IHeaderFormatConverter headerFormatConverter = getFactory().createHeaderFormatConverter(cfg.getHeaderType());

		IDataCompressor dataCompressor = getFactory().createDataCompressor(cfg);

		IDataEncryptor dataEncryptor = getFactory().createDataEncryptor(cfg.getEncType());
		dataEncryptor.setAuth(auth);

		MessageConverter converter = new MessageConverter(headerFormatConverter, dataCompressor, dataEncryptor);

		this.transport = new IotTransport(protocol, converter, customListener);
	}

	protected void printStartLog() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(
					"\n==============================================================================" + "\n{}"
							+ "\nAuthImplementation={}"
							+ "\n==============================================================================",
					cfg.toString(), auth.getClass().getSimpleName());
		}
	}

	/**
	 * B.IoT에 인증 관련 메시지를 주고 받으면서 디바이스 인증을 수행합니다.
	 * 디바이스 인증 프로세스는 IotTransport 클래스로 이동하였음. retry connection 문제로..
	 */
	@Deprecated
	protected void processDeviceAuth() {
		// 메시지 전송이 필요한 디바이이스 인증 프로세스
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

			transport.setAuthorized(true);
		}
	}

	/**
	 * 서버와의 연결을 종료합니다.
	 * 이 메서드를 통하여 명시적으로 disconnect 하는 경우, 자동 연결 재시도 기능이 종료되며,
	 * 응답 콜백 타임아웃 체크 스레드, Keep Alive 메시지 송신 스레드가 종료됩니다.
	 */
	public void disconnect() {
		transport.disconnect();
	}

	/* ======================================================================================= */

	/**
	 * 메시지를 서버로 전송합니다.
	 * 단, 요청(Q) 타입의 메시지는 전송할 수 없습니다.
	 * 
	 * @param msg B.IoT메시지
	 * @return 메시지 전송 성공하면 true, 실패하면 false를 반환합니다.
	 */
	public boolean send(IotMessage msg) {
		return transport.send(msg);
	}

	/**
	 * 송신한 요청 메시지에 대한 응답 메시지 수신 후,
	 * 특정 작업을 수행해야 하는 경우 호출합니다.
	 * 단, 해당 메서드는 blocking 되지 않으므로 멀티스레드에 유의해야 합니다.
	 * 
	 * @param msg B.IoT메시지
	 * @param responseCallback 응답 콜백
	 *        응답 메시지 수신 이후의 액션
	 * @return
	 */
	public boolean call(IotMessage msg, IResponseCallback responseCallback) {
		return transport.call(msg, responseCallback);
	}

	/**
	 * 송신한 요청 메시지에 대한 응답 메시지를 수신하여 리턴 값으로 받고자 할 때 사용합니다.
	 * 서버에서 응답을 늦게 줄 수 있거나, 성능에 유의해야 하는 경우,
	 * syncCall() 이 아닌 call() 메서드의 사용을 권장합니다.
	 * 
	 * @param msg B.IoT메시지
	 * @param timeoutSeconds 타임 아웃 시간 (초 단위)
	 * @return return null if response timed out.
	 */
	public IotMessage syncCall(IotMessage msg, int timeoutSeconds) {
		return transport.syncCall(msg, timeoutSeconds);
	}

	/**
	 * 송신한 요청 메시지에 대한 응답 메시지를 수신하여 리턴 값으로 받고자 할 때 사용합니다.
	 * 서버에서 응답을 늦게 줄 수 있거나, 성능에 유의해야 하는 경우,
	 * syncCall() 이 아닌 call() 메서드의 사용을 권장합니다.
	 * 
	 * @param msg B.IoT메시지
	 * @return return null if response timed out.
	 */
	public IotMessage syncCall(IotMessage msg) {
		return syncCall(msg, cfg.getDefaultSyncResponseTimeout());
	}

}
