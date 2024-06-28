
package com.sds.iot.sdk.protocol.tls;

import java.io.InputStream;
import java.security.SecureRandom;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.iot.sdk.util.ConfigFileUtil;

/**
 * 이 클래스는 클라이언트에서 사용하는 Security Communication( SSL/TLS ) property의 기본값을 정의합니다.<br>
 * 여기에 정의된 값은 프로토콜 별 설정값(property)을 이용할 때 사용합니다.
 * 
 * @author SDS
 */
public class SslProperties {
	private static final Logger	LOGGER						= LoggerFactory.getLogger(SslProperties.class);

	private static final String	MUTUAL_TLS_CRT				= "MUTUAL_TLS_CRT";
	private static final String	MUTUAL_TLS_KEY				= "MUTUAL_TLS_KEY";

	/* mutual tls 를 위한 jks 파일 설정 방식은 구버전 호환을 위해 남겨둠 */
	private static final String	KEYSTORE					= "KEYSTORE";
	private static final String	KEYSTORE_PASSWORD			= "KEYSTORE_PASSWORD";
	private static final String	KEYSTORE_KEY_PASSWORD		= "KEYSTORE_KEY_PASSWORD";

	private static final String	TRUST_KEYSTORE_PUBLIC_YN	= "TRUST_KEYSTORE_PUBLIC_YN";
	private static final String	TRUST_KEYSTORE				= "TRUST_KEYSTORE";
	private static final String	TRUST_KEYSTORE_PASSWORD		= "TRUST_KEYSTORE_PASSWORD";

	private final Properties	props						= new Properties();

	/* ======================================================================================= */

	/**
	 * @param propsFilePath
	 *        설정 파일(*.properties)의 경로
	 */
	public SslProperties(String propsFilePath) {
		ConfigFileUtil.loadProps(props, propsFilePath);
	}

	/**
	 * @param props
	 */
	public SslProperties(Properties props) {
		this.props.putAll(props);
	}

	/**
	 * get property.
	 * 
	 * @param key
	 *        String
	 * @return String
	 */
	private String getProperty(String key) {
		String value = props.getProperty(key);

		if (null == value || value.isEmpty()) {
			LOGGER.trace("****** Cert Properties : '{}' value is empty.", key);
			return "";
		}

		return value;
	}

	/* ======================================================================================= */

	/**
	 * @return CRT 값
	 */
	public String getMutualTlsCrtFilePath() {
		return getProperty(SslProperties.MUTUAL_TLS_CRT);
	}

	/**
	 * @return KEY 값
	 */
	public String getMutualTlsKeyFilePath() {
		return getProperty(SslProperties.MUTUAL_TLS_KEY);
	}

	/**
	 * KeyStore file path.
	 * 
	 * @return KeyStore 파일 경로
	 */
	public String getKeyStoreFilePath() {
		return getProperty(SslProperties.KEYSTORE);
	}

	/**
	 * @return KeyStore Password
	 */
	public String getKeyStorePassword() {
		return getProperty(SslProperties.KEYSTORE_PASSWORD);
	}

	void setKeyStorePasswordRandomIfNotNull() {
		if (props.getProperty(SslProperties.KEYSTORE_PASSWORD) == null) {
			props.setProperty(SslProperties.KEYSTORE_PASSWORD, makeRandomString(8));
		}
	}

	/**
	 * @return KeyStore Key Password
	 */
	public String getKeyStoreKeyPassword() {
		return getProperty(SslProperties.KEYSTORE_KEY_PASSWORD);
	}

	void setKeyStoreKeyPasswordRandomIfNotNull() {
		if (props.getProperty(SslProperties.KEYSTORE_KEY_PASSWORD) == null) {
			props.setProperty(SslProperties.KEYSTORE_KEY_PASSWORD, makeRandomString(8));
		}
	}

	/**
	 * Trust KeyStore file path
	 * 
	 * @return 신뢰할 수 있는 KeyStore 파일 경로
	 */
	public String getTrustKeyStoreFilePath() {
		return getProperty(SslProperties.TRUST_KEYSTORE);
	}

	/**
	 * @return 신뢰할 수 있는 KeyStore Password
	 */
	public String getTrustStorePassword() {
		return getProperty(SslProperties.TRUST_KEYSTORE_PASSWORD);
	}

	/**
	 * default 'true' if not defined 'TRUST_KEYSTORE_PUBLIC_YN'.
	 * 
	 * @return 공인인증서 사용 여부 (true/false)
	 */
	public boolean isPublicCertUsed() {
		String val = getProperty(SslProperties.TRUST_KEYSTORE_PUBLIC_YN);
		return "".equals(val) ? true : Boolean.parseBoolean(val);
	}

	/* ======================================================================================= */

	/**
	 * @return 생성된 클라이언트 인증서 파일 (byte[])
	 */
	byte[] createClientCertFileBytes() {

		if ("".equals(getMutualTlsCrtFilePath())) {
			return null;
		}

		InputStream in = ConfigFileUtil.createFileInputStream(getMutualTlsCrtFilePath());

		return ConfigFileUtil.createBytesFromInputStream(in);
	}

	/**
	 * @return
	 */
	byte[] createClientPrivateKeyFileBytes() {

		if ("".equals(getMutualTlsKeyFilePath())) {
			return null;
		}

		InputStream in = ConfigFileUtil.createFileInputStream(getMutualTlsKeyFilePath());

		return ConfigFileUtil.createBytesFromInputStream(in);
	}

	/**
	 * @return
	 */
	InputStream createClientKeyStoreInputStream() {

		if ("".equals(getKeyStoreFilePath())) {
			return null;
		}

		return ConfigFileUtil.createFileInputStream(getKeyStoreFilePath());
	}

	/**
	 * @return
	 */
	InputStream createTrustKeyStoreInputStream() {

		if ("".equals(getTrustKeyStoreFilePath()) || isPublicCertUsed()) {
			// public certification 
			return null;
		}

		return ConfigFileUtil.createFileInputStream(getTrustKeyStoreFilePath());
	}

	/* ======================================================================================= */

	private static String makeRandomString(int len) {
		SecureRandom rand = new SecureRandom();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < len; i++) {
			//0:number, 1:uppercase, 2:lowercase
			int kind = rand.nextInt(3);
			int c;
			if (kind == 0) {
				c = '0' + rand.nextInt(10); // 0 ~ 9
			} else if (kind == 1) {
				c = 'A' + rand.nextInt(26); // 0 ~ 25
			} else {
				c = 'a' + rand.nextInt(26); // 0 ~ 25
			}
			sb.append((char) c);
		}
		return sb.toString();
	}

}
