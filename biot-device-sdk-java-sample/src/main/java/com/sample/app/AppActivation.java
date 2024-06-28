package com.sample.app;
 
import com.sds.iot.sdk.IotClient;
 
/**
 * Sample App(Activation)
 * Root thing (부모 사물)을 활성화 시키는 예제
 *
 */
public class AppActivation
{   
    public static void main( String[] args )
    {
    	try {   
            // Setup the thing information
            String userId	 	= "test@samsung.com";
            String userPw 		= "test1!";
            String siteId 		= "CB00000000";
            String thingName 	= "TestModelITA.030";

            // Root thing activation
            IotClient client = IotClient.thing(siteId, thingName).itaUserLogin(userId, userPw).configFile("biot_client.properties");
 
    		client.connect();

    		// 사물이 준비 또는 비활성일 때, 활성화 해 준다.
            // 준비 및 비활성화는 상태는 서비스 포탈에서 설정할 수 있다. (Thing - 사물 관리)
    		client.activateThing("TestModel", "030");
        } catch (Exception e) {
            // You can exception handling
            e.printStackTrace();
        }
    }
}