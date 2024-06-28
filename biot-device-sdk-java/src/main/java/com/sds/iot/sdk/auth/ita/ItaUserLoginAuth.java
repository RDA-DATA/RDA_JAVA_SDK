
package com.sds.iot.sdk.auth.ita;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;

import com.sds.iot.sdk.IotClientConfig;
import com.sds.iot.sdk.auth.IAuth;
import com.sds.iot.sdk.exception.IotException;
import com.sds.iot.sdk.protocol.http.SimpleHttpClient;

/**
 * 이 클래스는 포탈 로그인(ID/Password) 방식을 통한 인증 처리를 위한 함수를 제공합니다.
 * 
 * @author SDS
 */
public class ItaUserLoginAuth extends ItaDirectAuth implements IAuth {

	private IotClientConfig		cfg;
	private String				username;
	private String				passwordSha;
	private SimpleHttpClient	httpClient;

	/**
	 * after create object, you can call
	 * <code>setUsername()</code> and <code>setPassword()</code> (or <code>setPasswordSha()</code>).
	 */
	public ItaUserLoginAuth() {
		super();
	}

	/**
	 * @param username
	 *        사용자 ID
	 * @param password
	 *        : password 원본 문자열 설정.
	 */
	public ItaUserLoginAuth(String username, String password) {
		super();
		setUsername(username);
		try {
			setPassword(password);
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid password : " + e.toString(), e);
		}
	}

	/**
	 * @param username
	 *        사용자 ID
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * password 원본 문자열 설정.
	 * (password 또는 passwordSha 둘 중에 하나만 설정할 것)
	 * 
	 * @param password
	 *        password 원본 문자열
	 *
	 * @throws Exception 패스워드 설정 시 실패하면 예외 발생
	 */
	public void setPassword(String password) throws Exception {
		MessageDigest sh = MessageDigest.getInstance("SHA-256");
		sh.update(password.getBytes(Charset.defaultCharset()));
		this.passwordSha = new String(Hex.encodeHex(sh.digest()));
	}

	/**
	 * password 의 SHA-256 수행 결과 문자열 설정.
	 * (password 또는 passwordSha 둘 중에 하나만 설정할 것)
	 * 
	 * @param passwordSha
	 *        password 의 SHA-256 수행 결과 문자열
	 */
	public void setPasswordSha(String passwordSha) {
		this.passwordSha = passwordSha;
	}

	@Override
	public String getAuthTypeName() {
		return "ITA_UserLogin";
	}

	@Override
	public void setConfig(IotClientConfig cfg) {
		this.cfg = cfg;
		httpClient = new SimpleHttpClient(cfg);
	}

	@Override
	public void requestCredential() throws IotException {
		try {
			// id/pw login (http)
			String userLoginToken = ItaAuthSupport.httpRequestUserLoginToken(httpClient, cfg, username, passwordSha);

			// authCode 요청
			Map<String, Object> resultMap = ItaAuthSupport.httpRequestThingAuthCodeWithUserLoginToken(httpClient, cfg,
					userLoginToken);

			// authCode 설정
			String tmp = (String) resultMap.get("authCode");
			this.setAuthCode(tmp);

			// itaThingAuthCode API 호출 결과로 얻은 정보로, 실제 접근할 serverAddr 을 재 지정.
			ItaAuthSupport.changeServerAddr(cfg, resultMap);

			// itaThingAuthCode API 호출 결과로 얻은 정보로, 실제 사용할 encType 와 일치하는지 검사.
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
