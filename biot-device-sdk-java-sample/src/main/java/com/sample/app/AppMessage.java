package com.sample.app;
 
import com.google.gson.JsonObject;
import com.sds.iot.sdk.IotClient;
import com.sds.iot.sdk.IotClientConfig;
import com.sds.iot.sdk.IotStatusCode;
import com.sds.iot.sdk.auth.ita.ItaUserLoginAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sample App(AppMessage)
 * 메시지 헤더 타입 설정, 메시지 데이터 압축 및 암호화 설정 예제
 * 동기 방식 API에서 상태 코드 별로 비지니스 로직 구현할 수 있음
 *
 */
public class AppMessage
{
    private static final Logger LOGGER	= LoggerFactory.getLogger(AppFirmware.class);

    public static void main( String[] args )
    {
    	 try {
            // Setup the thing information
            String userId	 	= "test@samsung.com";
            String userPw 		= "test1!";
            String siteId 		= "CB00000000";
            String thingName 	= "TestModelITA.030";

            ItaUserLoginAuth auth = new ItaUserLoginAuth(userId, userPw);
            IotClientConfig config = new IotClientConfig(siteId, thingName, null);
            String	apiServerAddr	= "http://local.insator.io:8088";
            config.setApiServerAddr(apiServerAddr);
            // 서버의 mqtt 모듈 설정과 아래의 헤더타입, 압축, 암호화 설정이 동일해야 함.
            // 메시지 헤더 타입 결정
            config.setHeaderType("D");
            // 메시지 데이터 압축 설정
            config.setCompressUseBytes(1);
            // 메시지 암호화 설정 (AES128만 지원함.)
            config.setEncType("3");
            IotClient client =  new IotClient(auth, config);

            client.connect();

            // Send activation message
            try {
                JsonObject data = new JsonObject();
                data.addProperty("temperature", 90);
                data.addProperty("humidity", 70);
                IotStatusCode code = IotStatusCode.getErrorCodeByCode(client.sendAttributesSync("Basic-AttrGroup", data.toString()));
                if(code.toString().startsWith("SUCCESS"))
                {
                    LOGGER.info("Succeeded to update data of the thing");
                }
                else
                {
                    LOGGER.info("Fail to update data of the thing");
                    // 실패했을 경우 다른 비지니스 로직을 수행할 수 있다.
                    switch (code)
                    {
                        case CLIENT_ERROR_BAD_REQUEST:
                        case CLIENT_ERROR_FORBIDDEN:
                        case CLIENT_ERROR_NOT_FOUND:
                            // 1번 비지니스 로직 수행
                            break;
                        case SERVER_ERROR_BAD_GATEWAY:
                        case SERVER_ERROR_INTERNAL:
                            // 2번 비지니스 로직 수행
                            break;
                        default:
                            // 디폴트 로직 수행
                            break;
                    }
                }

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