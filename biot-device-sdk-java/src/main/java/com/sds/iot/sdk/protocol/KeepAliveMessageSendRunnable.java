
package com.sds.iot.sdk.protocol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.iot.sdk.message.IotMessage;
import com.sds.iot.sdk.message.PredefinedMessageSupport;

/**
 * 이 클래스는 클라이언트에서 메시지 전송을 위한 서버와의 연결 유지를 관리하는 함수를 제공합니다.
 * 
 * @author SDS
 */
class KeepAliveMessageSendRunnable implements Runnable {
	private static final Logger	LOGGER	= LoggerFactory.getLogger(KeepAliveMessageSendRunnable.class);

	private final IotTransport	transport;
	private final long			intevalMillis;

	KeepAliveMessageSendRunnable(IotTransport transport, int intevalSec) {
		this.transport = transport;
		this.intevalMillis = intevalSec * 1000L;
	}

	@Override
	public void run() {
		LOGGER.debug("start keep-alive message send thread!");

		while (!Thread.interrupted() && transport.isAutoConnectRetry()) {

			if (checkAndWait()) {
				sendMessage();
			}

		}

		LOGGER.debug("stop keep-alive message send thread");
	}

	/**
	 * @return 바로 메시지 보내고자 하면 true 리턴, sleep 후에는 false 리턴.
	 */
	private boolean checkAndWait() {

		long sleepMillis;

		if (transport.isAuthorized()) {
			// 서버와 연결되어 있고 인증 완료된 상태이면,
			// 마지막 메시지 수신시각 기준으로 sleep 할 시간 계산
			// = lastMessageReceived + intervalMillis - now
			sleepMillis = transport.getLastMessageReceived() + intevalMillis - System.currentTimeMillis();
			if (sleepMillis <= 10) {
				// send message right now!!!!!!!!!!!!
				return true;
			}
			LOGGER.trace("client is going to check 'last message received time' after {} ms.", sleepMillis);
		} else {
			// 서버와 연결이 끊긴 상태이면, 기본 sleep 수행
			sleepMillis = intevalMillis;
			LOGGER.debug("client is not yet connected. client is going to check 'connection status' after {} ms.", sleepMillis);
		}

		// 대기 후 다음에 다시 연결 여부 체크
		try {
			Thread.sleep(sleepMillis);
		} catch (InterruptedException e) {
			LOGGER.debug("interruped : {}", e.toString());
		}
		return false;
	}

	private void sendMessage() {
		LOGGER.trace("send keep-alive message");

		IotMessage msg = PredefinedMessageSupport.createKeepAliveMessage();

		IotMessage resMsg = transport.syncCall(msg, 5);
		if (resMsg == null) {
			// 5초 이내 keep alive 응답 수신 실패.
			LOGGER.warn("Cannot receive keep-alive response message.");

			//TODO (SDK) Keep-alive 응답 수신 실패 시 연결을 끊을지? client.disconnect();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				LOGGER.debug("interruped : {}", e.toString());
			}
		}
		else {
			if(!resMsg.getResCode().startsWith("2"))
			{
				// TODO (SDK) 응답값이 success가 아닐 때, 어떻게 할 지 고민할 필요 있음, 이리로 오는 케이스는 현재까지는 보지 못했음. by Gideon
				LOGGER.warn("FAIL TO SEND Keep-alive message! ({})", resMsg.getResMsg());
			}

		}

	}
}
