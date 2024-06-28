package com.sample.app;
 
import com.google.gson.JsonObject;
import com.sds.iot.sdk.IotClient;
import com.sds.iot.sdk.IotClientConfig;
import com.sds.iot.sdk.IotStatusCode;
import com.sds.iot.sdk.auth.ita.ItaUserLoginAuth;

/**
 * Sample App(Attribute)
 * 사물의 속성 데이터를 B.IoT 에 업데이트하는 예제
 *
 */
public class AppAttribute
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
            	// 1. JsonObject 사용
                // Root thing attributes updating
            	JsonObject data = new JsonObject();
            	data.addProperty("temperature", 15);
                data.addProperty("humidity", 25);

                // Root Thing 속성 데이터 등록
                // 사물 활성화 상관 없이 속성 데이터 등록 됨.
                client.sendAttributes("Basic-AttrGroup", data.toString());

                //Leaf thing attributes updating
                // Leaf Thing 속성 데이터 등록
                // Leaf Thing 활성화 여부 상관 없이 속성 데이터 등록 됨.
                // 또한 Root Thing 활성화 여부 상관 없음.
                JsonObject dataLeaf = new JsonObject();
                data.addProperty("x", 23);
                data.addProperty("y", 67);
                data.addProperty("room", "R575");
                client.sendAttributesForLeaf("CoordinateSensor.050", "Basic-AttrGroup", dataLeaf.toString());

                // 2. JsonString 사용
                client.sendAttributes("Basic-AttrGroup", "{\"temperature\":66,\"humidity\":44}");
                client.sendAttributesForLeaf("CoordinateSensor.050", "Basic-AttrGroup", "{\"x\":10,\"y\":34,\"room\":\"R503\"}");

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