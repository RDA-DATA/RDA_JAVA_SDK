package com.sample.app;
 
import com.sds.iot.sdk.IotClient;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * Sample App
 * ini 파일을 이용하여 유저 정보를 획득하고 IoT 서버와 Connect 하는 예제
 *
 */
public class AppUsingFileINI
{
    public static void main( String[] args )
    {
        try {   
            // Setup the thing information
            // userId, userPw, siteId, thingName - ini 파일을 통하여 해당 정보를 가지고 온다.
            Properties p = new Properties();
            p.load(new FileInputStream("D:\\IoT_Dev\\test\\thingInfo.ini"));

            String userId	 	= p.getProperty("UserID");
			String userPw 		= p.getProperty("UserPW");
            String siteId 		= p.getProperty("SiteID");
            String thingName 	= p.getProperty("ThingName");
            
            IotClient client = IotClient.thing(siteId, thingName).itaUserLogin(userId, userPw).configFile("biot_client.properties");
 
    		client.connect();
        } catch (Exception e) {
            // You can exception handling
            e.printStackTrace();
        }
    }
}