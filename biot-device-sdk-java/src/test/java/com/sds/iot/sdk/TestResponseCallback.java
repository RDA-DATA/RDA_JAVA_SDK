
package com.sds.iot.sdk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.iot.sdk.callback.AbstractCallback;
import com.sds.iot.sdk.message.IotMessage;

public class TestResponseCallback extends AbstractCallback {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestResponseCallback.class);

	@Override
	public void action(IotMessage resMsg) throws Exception {
		LOGGER.info("TestResponseCallback action!!");

	}

	@Override
	public void timeoutAction() {
		LOGGER.warn("TestResponseCallback timeoutAction!!");

	}

}
