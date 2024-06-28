
package com.sds.iot.sdk.auth.ita;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.sds.iot.sdk.IotClientConfig;
import com.sds.iot.sdk.exception.IotException;
import com.sds.iot.sdk.protocol.http.SimpleHttpClient;
import com.sds.iot.sdk.util.JsonUtil;

/**
 * 이 클래스는 클라이언트에서의 인증 방법에 따른 Rest API 요청/응답을 처리합니다.
 * 
 * @author SDS
 *
 */
class ItaAuthSupport {
	private static final Logger	LOGGER			= LoggerFactory.getLogger(ItaAuthSupport.class);

	private static final String	LOGIN_METHOD	= "POST";
	private static final String	LOGIN_URI		= "/v1.1/main/action/itaUserLogin";

	private static final String	ITACODE_METHOD	= "GET";
	private static final String	ITACODE_URI		= "/v1.1/sites/{siteId}/things/{thingName}/module/{moduleType}/itaThingAuthCode";
	private static final String	ITACODE_PARAM	= "?msgHeaderType={msgHeaderType}";

	/**
	 * 포탈 로그인(ID/Password)을 통한 사용자 토큰 획득
	 * 
	 * @param httpClient
	 *        http 클라이언트
	 * @param cfg
	 *        클라이언트 설정
	 * @param username
	 *        IoT 포탈 사용자 ID
	 * @param passwordSha
	 *        암호화된 IoT 포탈 사용자 Password
	 * @return 사용자 토큰
	 * @throws IotException
	 */
	static String httpRequestUserLoginToken(SimpleHttpClient httpClient, IotClientConfig cfg, String username, String passwordSha)
			throws IotException {
		String method = LOGIN_METHOD;
		String url = cfg.getApiServerAddr() + LOGIN_URI;

		JsonObject reqBodyJson = new JsonObject();
		reqBodyJson.addProperty("userId", username);
		reqBodyJson.addProperty("userPassword", passwordSha);
		byte[] reqBody = JsonUtil.toJson(reqBodyJson).getBytes(Charset.defaultCharset());

		int reqBodyLen = 0;

		Map<String, Object> reqHeaders = new HashMap<>();
		reqHeaders.put("Content-Type", "application/json");
		reqHeaders.put("Content-Length", reqBodyLen);
		reqHeaders.put("X-CSRF-TOKEN", "UL");

		byte[] resBody = httpClient.request(method, url, reqBody, reqHeaders);

		String jsonStr = new String(resBody, Charset.defaultCharset());
		Map<String, Object> resultMap = JsonUtil.fromJsonToMap(jsonStr);

		String userToken = (String) resultMap.get("authToken");
		return userToken;
	}

	/**
	 * Basic Auth credential 방식을 통한 인증
	 * 
	 * @param httpClient
	 *        http 클라이언트
	 * @param cfg
	 *        클라이언트 설정
	 * @param basicAuthCredential
	 *        Basic Auth credential 정보
	 * @return 인증 정보 (Map)
	 *         Map of ip, port, msgHeaderType, isSsl, sslOpts, encType, authCode
	 * @throws IotException
	 */
	static Map<String, Object> httpRequestThingAuthCodeWithBasicAuth(SimpleHttpClient httpClient, IotClientConfig cfg,
			String basicAuthCredential) throws IotException {

		Map<String, Object> reqHeaders = new HashMap<>();
		reqHeaders.put("X-CSRF-TOKEN", "Basic " + basicAuthCredential);

		return httpRequestThingAuthCode(httpClient, cfg, reqHeaders);
	}

	/**
	 * 사용자 토큰을 이용한 인증
	 * 
	 * @param httpClient
	 *        http 클라이언트
	 * @param cfg
	 *        클라이언트 설정
	 * @param userLoginToken
	 *        사용자 인증 토큰
	 * @return 인증 정보 (Map)
	 *         Map of ip, port, msgHeaderType, isSsl, sslOpts, encType, authCode
	 * @throws IotException
	 */
	static Map<String, Object> httpRequestThingAuthCodeWithUserLoginToken(SimpleHttpClient httpClient, IotClientConfig cfg,
			String userLoginToken) throws IotException {

		Map<String, Object> reqHeaders = new HashMap<>();
		reqHeaders.put("X-CSRF-TOKEN", "UL " + userLoginToken);

		return httpRequestThingAuthCode(httpClient, cfg, reqHeaders);
	}

	/**
	 * 서버로 사물 인증 코드 요청
	 * @param httpClient
	 *        http 클라이언트
	 * @param cfg
	 *        클라이언트 설정
	 * @param reqHeaders
	 *        request header
	 * @return 사물 인증 정보 (Map)
	 *         Map of ip, port, msgHeaderType, isSsl, sslOpts, encType, authCode
	 * @throws IotException
	 */
	private static Map<String, Object> httpRequestThingAuthCode(SimpleHttpClient httpClient, IotClientConfig cfg,
			Map<String, Object> reqHeaders) throws IotException {
		String method = ITACODE_METHOD;
		String uri;
		uri = replace(ITACODE_URI, "{siteId}", cfg.getSiteId());
		uri = replace(uri, "{thingName}", cfg.getThingName());
		uri = replace(uri, "{moduleType}", cfg.getProtocolType().toLowerCase(Locale.getDefault()));
		// 참고: 2.8 서버에서는 moduleType 소문자만 지원. 2.9 서버에서는 moduleType 대소문자 무관 
		String param = replace(ITACODE_PARAM, "{msgHeaderType}", cfg.getHeaderType());
		String url = cfg.getApiServerAddr() + uri + param;

		reqHeaders.put("Content-Type", "application/json");

		byte[] resBody = httpClient.request(method, url, null, reqHeaders);

		String jsonStr = new String(resBody, Charset.defaultCharset());
		Map<String, Object> resultMap = JsonUtil.fromJsonToMap(jsonStr);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("{}", resultMap);
		}
		return resultMap;
	}

	/**
	 * itaThingAuthCode API 호출 결과로 얻은 정보로, 실제 접근할 serverAddr 을 재 지정.
	 * 
	 * @param cfg
	 * @param resultMap
	 */
	static void changeServerAddr(IotClientConfig cfg, Map<String, Object> resultMap) {
		StringBuilder sb = new StringBuilder();
		boolean ssl = Boolean.valueOf((String) resultMap.get("isSsl"));
		if ("MQTT".equals(cfg.getProtocolType())) {
			if (ssl) {
				sb.append("ssl://");
			} else {
				sb.append("tcp://");
			}
		} else if ("WEBS".equals(cfg.getProtocolType())) {
			if (ssl) {
				sb.append("wss://");
			} else {
				sb.append("ws://");
			}
		} else {
			throw new IotException("400", "Unknown protocol type [" + cfg.getProtocolType() + "]");
		}

		String newAddr = sb.append(resultMap.get("ip")).append(":").append(resultMap.get("port")).toString();
		if (!newAddr.equals(cfg.getServerAddr())) {
			if (cfg.getServerAddr() == null || cfg.getServerAddr().isEmpty()) {
				LOGGER.info("****** Set server address : {}", newAddr);
			} else {
				LOGGER.info("****** Change server address : => {}", newAddr);
			}
			cfg.setServerAddr(newAddr);
		}
	}

	/**
	 * itaThingAuthCode API 호출 결과로 얻은 정보로, encType 확인
	 * 
	 * @param cfg
	 * @param resultMap
	 */
	static void validateEncType(IotClientConfig cfg, Map<String, Object> resultMap) {
		String encType = (String) resultMap.get("encType");
		if (!encType.equals(cfg.getEncType())) {
			// 서버의 encType 과 사물의 encType 이 맞지 않는 경우 처리
			throw new IotException("400",
					"Check configuration 'encType'. (server=" + encType + ", client=" + cfg.getEncType() + ")");
		} else if (!"0".equals(encType)) {
			// 암호화 유형 로그
			LOGGER.info("****** Data encryption type : {}", encType);
		}
	}

	/**
	 * Replace all occurences of a substring within a string with another string.
	 * 
	 * @param str
	 *        String to examine
	 * @param oldPattern
	 *        String to replace
	 * @param newPattern
	 *        String to insert
	 * @return a String with the replacements
	 */
	private static String replace(String str, String oldPattern, String newPattern) {
		if (!(str != null && str.length() > 0) || !(oldPattern != null && oldPattern.length() > 0) || newPattern == null) {
			return str;
		}
		StringBuilder sb = new StringBuilder();
		int pos = 0; // our position in the old string
		int index = str.indexOf(oldPattern);
		// the index of an occurrence we've found, or -1
		int patLen = oldPattern.length();
		while (index >= 0) {
			sb.append(str.substring(pos, index));
			sb.append(newPattern);
			pos = index + patLen;
			index = str.indexOf(oldPattern, pos);
		}
		sb.append(str.substring(pos));
		// remember to append any characters to the right of a match
		return sb.toString();
	}

	/**
	 * 서버 응답으로 부터 인증 토큰 획득
	 * @param jsonStr
	 *        서버 응답 데이터
	 * @return 인증 토큰
	 */
	static String parseAuthToken(String jsonStr) {
		Map<String, Object> resultMap = JsonUtil.fromJsonToMap(jsonStr);
		return (String) resultMap.get("authToken");
	}

}
