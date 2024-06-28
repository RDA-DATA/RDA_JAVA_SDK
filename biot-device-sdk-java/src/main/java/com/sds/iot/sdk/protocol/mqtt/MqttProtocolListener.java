
package com.sds.iot.sdk.protocol.mqtt;
/*
 * Copyright (c) 2012-2014 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License v2.0 is available at
 * http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.iot.sdk.message.IotMessage;
import com.sds.iot.sdk.message.MessageConverter;
import com.sds.iot.sdk.protocol.ITransportListener;

/**
 * 이 클래스는 Paho MQTT 라이브러리에서 클라이언트 연결 및 메시지 처리에 대한 리스너 함수를 구현합니다.
 * 
 * @author SDS
 */
public class MqttProtocolListener implements MqttCallback {
	private static final Logger			LOGGER	= LoggerFactory.getLogger(MqttProtocolListener.class);

	private final ITransportListener	transport;
	private final MessageConverter		converter;

	public MqttProtocolListener(ITransportListener transport, MessageConverter converter) {
		this.transport = transport;
		this.converter = converter;
	}

	@Override
	public void connectionLost(Throwable cause) {
		LOGGER.warn("[MQTT] connectionLost : {}", cause.toString(), cause);

		// TODO (SDK) 스레드 분리를 할지? - connection retry 가 이 스레드로 작업됨
		transport.onConnectionLost(cause.toString());
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[MQTT] message arrived {} bytes", message.getPayload().length);
		}

		IotMessage msg = converter.convertFromBytes(message.getPayload());

		// TODO (SDK) 스레드 분리를 할지? - 메시지 수신 후, 긴 작업을 하는 경우 MQTT Protocol 로 메시지 수신이 불가능함. 
		transport.onMessageReceived(msg);
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.trace("[MQTT] publish complete (token={})", token.getMessageId());
		}
		transport.onMessageSent(null);
	}

}
