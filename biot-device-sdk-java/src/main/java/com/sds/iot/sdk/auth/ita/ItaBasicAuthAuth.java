
package com.sds.iot.sdk.auth.ita;

import java.util.Map;

import com.sds.iot.sdk.IotClientConfig;
import com.sds.iot.sdk.auth.IAuth;
import com.sds.iot.sdk.exception.IotException;
import com.sds.iot.sdk.protocol.http.SimpleHttpClient;

/**
 * 이 클래스는 Basic Auth credential 방식을 통한 인증 처리를 위한 함수를 제공합니다.
 * 
 * @author SDS
 */
public class ItaBasicAuthAuth extends ItaDirectAuth implements IAuth {

	private IotClientConfig		cfg;
	private String				basicAuthCredential;
	private SimpleHttpClient	httpClient;

	public ItaBasicAuthAuth() {
		super();
	}

	public ItaBasicAuthAuth(String basicAuthCredential) {
		this.basicAuthCredential = basicAuthCredential;
	}

	public String getBasicAuthCredential() {
		return basicAuthCredential;
	}

	public void setBasicAuthCredential(String basicAuthCredential) {
		this.basicAuthCredential = basicAuthCredential;
	}

	@Override
	public String getAuthTypeName() {
		return "ITA_BasicAuth";
	}

	@Override
	public void setConfig(IotClientConfig cfg) {
		this.cfg = cfg;
		httpClient = new SimpleHttpClient(cfg);
	}

	@Override
	public void requestCredential() throws IotException {
		try {
			// authCode 요청
			Map<String, Object> resultMap = ItaAuthSupport.httpRequestThingAuthCodeWithBasicAuth(httpClient, cfg,
					basicAuthCredential);

			// authCode 설정
			String tmp = (String) resultMap.get("authCode");
			this.setAuthCode(tmp);

			// itaThingAuthCode API 호출 결과로 얻은 정보로, 실제 접근할 serverAddr 을 재 지정.
			ItaAuthSupport.changeServerAddr(cfg, resultMap);

			// itaThingAuthCode API 호출 결과로 얻은 정보로, encType 확인
			ItaAuthSupport.validateEncType(cfg, resultMap);

		} catch (Exception e) {
			if (e instanceof IotException && ((IotException) e).isClientError()) {
				throw new IotException("401", "Fail to get ITA credential : " + e.toString(), e);
			} else {
				throw new IotException("500", "Fail to get ITA credential : " + e.toString(), e);
			}
		}
	}

}
