package com.sample.app;
 
import com.sds.iot.sdk.IotClient;
 
/**
 * Sample App(Registration)
 * Leaf Thing 등록 및 활성화하는 예제
 *
 */
public class AppRegistration
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
            
            // Send activation message
            try {
                // 부모 사물명 (Root thing), 자식 사물(Leaf Thing)의 모델명, 자식 사물의 고유 번호
                // 두번째 파라미터는 자식 사물명이 아니라 자식 사물의 모델명임을 주의
                client.registerLeafThing("TestModelITA.030", "CoordinateSensor", "050");
            } catch (Exception e) {
                // You can exception handling
                 e.printStackTrace();
            }
             
        } catch (Exception e) {
            // You can exception handling
             e.printStackTrace();
        }
    }
}