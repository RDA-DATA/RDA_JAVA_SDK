package com.sample.app;
 
import com.sds.iot.sdk.IotClient;

/**
 * Sample App(ActivationLeafThing)
 * Leaf thing (자식 사물)을 활성화 시키는 예제
 */
public class AppActivationLeafThing
{   
    public static void main( String[] args )
    {
    	try {   
            // Setup the thing information
            String userId	 	= "test@samsung.com";
            String userPw 		= "test1!";
            String siteId 		= "CB00000000";
            String thingName 	= "TestModelITA.030";
            
            IotClient client = IotClient.thing(siteId, thingName).itaUserLogin(userId, userPw).configFile("biot_client.properties");

            client.connect();

            // Leaf thing activation
            // 반드시, 부모 사물 (Root Thing)이 활성화 되어 있어야 함.
    		// 자식 사물 (Leaf Thing)이 준비 또는 비활성일 때, 활성화 해 준다.
            client.activateThingForLeaf("TestModelITA.030", "CoordinateSensor", "050"); // 새로 만듬.
        } catch (Exception e) {
            // You can exception handling
            e.printStackTrace();
        }
    }
}