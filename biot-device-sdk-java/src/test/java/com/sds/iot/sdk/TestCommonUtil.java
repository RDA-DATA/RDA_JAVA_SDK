
package com.sds.iot.sdk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.iot.sdk.message.IotMessage;

public class TestCommonUtil {
	private static final Logger	LOGGER				= LoggerFactory.getLogger(TestCommonUtil.class);

	static String				basicAttrDataJson1	= "{ \"aa\" : 111 }";
	static String				basicAttrDataJson2	= "{ \"aa\" : 222, \"bb\" : 222.222 }";
	static String				basicAttrDataJson3	= "{ \"aa\" : 333, \"bb\" : 333.333 }";

	static void testCommon(IotCoreClient client) {
		testCommon(client, 1, "");
	}

	static void testCommon(IotCoreClient client, int waitSeconds, String testName) {

		LOGGER.info("##############################################################");
		LOGGER.info("# START TEST !!! {}", testName);

		// CONNECT
		client.connect();

		// SEND 
		IotMessage msg1 = new IotMessage();
		msg1.setMsgCode("Basic-AttrGroup");
		msg1.setDataString(basicAttrDataJson1);
		client.send(msg1);

		// SYNC CALL 
		IotMessage msg2 = new IotMessage();
		msg2.setMsgCode("Basic-AttrGroup");
		msg2.setDataString(basicAttrDataJson2);
		IotMessage msg2res = client.syncCall(msg2, 3);
		if (msg2res == null) {
			LOGGER.error("no response...");
		} else {
			LOGGER.debug("response = {}", msg2res.toString());
		}

		// CALL 
		IotMessage msg3 = new IotMessage();
		msg3.setMsgCode("Basic-AttrGroup");
		msg3.setDataString(basicAttrDataJson3);
		client.call(msg3, new TestResponseCallback());

		// WAIT 
		try {
			Thread.sleep(1000L * waitSeconds);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// DISCONNECT
		client.disconnect();

		LOGGER.info("# END TEST !!! {}", testName);
		LOGGER.info("##############################################################");

	}

}
