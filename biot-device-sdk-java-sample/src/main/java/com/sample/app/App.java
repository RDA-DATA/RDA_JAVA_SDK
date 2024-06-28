package com.sample.app;
 
import com.google.gson.JsonObject;
import com.sds.iot.sdk.IotClient;
import com.sds.iot.sdk.auth.ita.ItaDirectAuth;

/**
 * Sample App
 * ITA 인증 방법으로 IoT 서버와 Connect 하는 예제
 *
 */
public class App
{
    public static void main( String[] args )
    {
        try {
            // authCode - ita 장비 인증정보
            String siteId 		= "C000000037";
            String thingName 	= "SDK_SAMPLE_MODEL.0625";
            String authCode     = "373f8d68055caf9e";

            // 사물인증방식 (authCode)
            ItaDirectAuth auth = new ItaDirectAuth(authCode);
            IotClient client = new IotClient(auth, siteId, thingName, "biot_client.properties");

            // connecting Thing
            client.connect();
            // activation Thing
            client.activateThing("SDK_SAMPLE_MODEL", "0625");

            // Send activation message
            try {
                JsonObject data = new JsonObject();
                data.addProperty("temperature", 1.1);
                data.addProperty("humidity", 2.2);

                client.sendAttributes("Basic-AttrGroup", data.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}