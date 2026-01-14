package com.sample.app;

import com.google.gson.JsonObject;
import com.sds.iot.sdk.IotClient;
import com.sds.iot.sdk.auth.ita.ItaDirectAuth;

/**
 * Sample App
 * ITA 인증 방법으로 IoT 서버와 Connect 하는 예제
 * <p>
 * -- 설명
 * PoC 접속 후 사물 > 사물관리 메뉴에서 생성한 사물을 검색하여 하단의 정보를 입력해주세요.
 * 입력된 정보를 통해 인증 객체를 생성하고 제공되는 SDK의 IoTClient 클래스를 통해 사물객체를 받습니다.
 * 해당 사물객체를 연결 및 활성화를 하여 데이터를 받을 수 있는 상태로 전환합니다.
 * 수집데이터 전송을 위해 해당 사물의 속성명과 속성타입에 맞는 값을 입력하여 json형식의 데이터를 생성합니다.
 * PoC와 연결된 사물객체를 통해 데이터를 전송합니다.
 */
public class App {
    public static void main(String[] args) {
        try {
            // authCode - ita 장비 인증정보 입니다.  등록한 사물의 사물정보 화면에서 정보를 입력해주세요.
            String siteId = "C000000004";
            String thingName = "SDK_SAMPLE_MODEL.0625";
            String authCode = "373f8d68055caf9e";

            ItaDirectAuth auth = new ItaDirectAuth(authCode);
            IotClient client = new IotClient(auth, siteId, thingName, "biot_client.properties");

            // PoC에 등록된 사물에 연결합니다.
            client.connect();

            // 연결한 사물을 활성화합니다.
            client.activateThing("SDK_SAMPLE_MODEL", "0625");

            // 데이터 전송부분입니다.
            try {
                // 전송할 데이터를 Json형식으로 만들어줍니다.
                JsonObject data = new JsonObject();
                data.addProperty("temperature", 1.5);
                data.addProperty("humidity", 2.5);

                // Json형식으로 만든 데이터를 1회 전송 합니다.
                client.sendAttributes("Basic-AttrGroup", data.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
