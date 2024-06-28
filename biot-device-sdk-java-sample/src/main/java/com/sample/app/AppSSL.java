package com.sample.app;

import com.google.gson.JsonObject;
import com.sds.iot.sdk.IotClient;

/**
 * Sample AppSSL
 * Two-way SSL 인증 방법으로 IoT 서버와 Connect 하는 예제
 *
 */
public class AppSSL
{   
    public static void main( String[] args )
    {
        try {   
            // Setup the thing information
            // siteId, thingName - 서비스 포탈에서 정보 확인 (Thing - 사물 관리 - 사물 정보)
            String siteId 		= "CB00000000";
            String thingName 	= "TestModel.001";

            /*
            * biot_client.properties 에 반드시 다음 속성값이 세팅되어야 한다.
            * MUTUAL_TLS_CRT, MUTUAL_TLS_KEY, KEYSTORE_PASSWORD, KEYSTORE_KEY_PASSWORD, TRUST_KEYSTORE, TRUST_KEYSTORE_PASSWORD
            * MUTUAL_TLS_CRT, MUTUAL_TLS_KEY 는 서비스 포탈에서 해당 사물 정보의 인증 정보에서 확인/발행 할 수 있다.
            * (Thing - 사물 관리 - 사물 정보 - 인증 정보 - 발생)
            * 나머지 사항은 매뉴얼 참고하기 바람
            *
            */
            IotClient client = IotClient.thing(siteId, thingName).mutualTls().configFile("biot_client.properties");

    		client.connect();

        } catch (Exception e) {
            // You can exception handling
             e.printStackTrace();
        }
    }
}