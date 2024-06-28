
package com.sds.iot.sdk.protocol;

import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.iot.sdk.callback.IResponseCallback;

/**
 * 이 클래스는 서버로부터의 응답에 대한 콜백 실행에 대해 관리하는 함수를 제공합니다.
 * 
 * @author SDS
 */
class ResponseCallbackCheckRunnable implements Runnable {
	private static final Logger						LOGGER	= LoggerFactory.getLogger(ResponseCallbackCheckRunnable.class);

	private final IotTransport						transport;
	private final Map<String, IResponseCallback>	callbackRepo;
	private final long								checkIntervalMillis;

	ResponseCallbackCheckRunnable(IotTransport transport, Map<String, IResponseCallback> callbackRepo, long checkIntervalMillis) {
		this.transport = transport;
		this.callbackRepo = callbackRepo;
		this.checkIntervalMillis = checkIntervalMillis;
	}

	@Override
	public void run() {
		LOGGER.debug("start response callback timeout check thread!");

		while (!Thread.interrupted() && transport.isAutoConnectRetry()) {

			checkTimeout();

			try {
				Thread.sleep(checkIntervalMillis);
			} catch (InterruptedException e) {
				LOGGER.debug("interruped : {}", e.toString());
				break;
			}
		}

		LOGGER.debug("stop response callback timeout check thread");

		clearRepo();
	}

	/**
	 * 지정된 타임아웃 이내에 응답을 받지 못한 요청 정보를 삭제하고, timeoutAction 을 수행합니다.
	 */
	private void checkTimeout() {
		if (LOGGER.isInfoEnabled() && callbackRepo.size() >= 10) {
			LOGGER.info("check response callback timeout. (waiting callbacks={})", callbackRepo.size());
		} else if (LOGGER.isDebugEnabled() && !callbackRepo.isEmpty()) {
			LOGGER.debug("check response callback timeout. (waiting callbacks={})", callbackRepo.size());
		}

		for (Entry<String, IResponseCallback> entry : callbackRepo.entrySet()) {
			IResponseCallback callback = entry.getValue();

			if (callback.getCreateTime() + callback.getTimeout() < System.currentTimeMillis()) {
				callbackRepo.remove(entry.getKey());

				IotTransportSupport.printRecvFailLog(entry.getKey(),
						"Response timed out. " + (callback.getTimeout() / 1000) + " sec.");
				LOGGER.warn("Response timed out. [{}] : created={}, timeout={}, type={}", entry.getKey(),
						IotTransportSupport.formatDefaultLocale("HH:mm:ss", callback.getCreateTime()), callback.getTimeout(),
						callback.getClass().getSimpleName());
				try {
					callback.timeoutAction();
				} catch (Throwable e) {
					LOGGER.error("callback timeout action error. [" + entry.getKey() + "] ", e);
				}
			}
		}
	}

	/**
	 * 응답을 받지 못한 요청 정보가 남아 있는데, 명시적으로 연결을 끊은 경우의 처리를 수행합니다.
	 */
	private void clearRepo() {
		for (Entry<String, IResponseCallback> entry : callbackRepo.entrySet()) {
			IResponseCallback callback = entry.getValue();

			callbackRepo.remove(entry.getKey());

			IotTransportSupport.printRecvFailLog(entry.getKey(),
					"No more wait response. " + (callback.getTimeout() / 1000) + " sec.");
			LOGGER.warn("No more wait response. [{}] : created={}, timeout={}, type={}", entry.getKey(),
					IotTransportSupport.formatDefaultLocale("HH:mm:ss", callback.getCreateTime()), callback.getTimeout(),
					callback.getClass().getSimpleName());
			try {
				callback.timeoutAction();
			} catch (Throwable e) {
				LOGGER.error("callback timeout action error. [" + entry.getKey() + "] ", e);
			}
		}
	}
}
