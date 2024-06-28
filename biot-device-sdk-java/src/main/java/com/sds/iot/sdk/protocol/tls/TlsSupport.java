
package com.sds.iot.sdk.protocol.tls;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.security.x509.X509CertImpl;

/**
 * 이 클래스는 서버에서 클라이언트 SSL인증을 위한 TLS v1.2를 지원합니다.
 * 
 * @author SDS
 */
@SuppressWarnings("restriction")
public class TlsSupport {

	private static final Logger	LOGGER					= LoggerFactory.getLogger(TlsSupport.class);
	private static final String	TLS_V_1_2				= "TLSv1.2";
	private static final String	KEY_FACTORY_ALGORITHM	= "RSA";
	private static final String	KEY_STORE_TYPE			= KeyStore.getDefaultType(); // JKS
	private static final String	KEY_MANAGER_ALGORITHM	= KeyManagerFactory.getDefaultAlgorithm();
	private static final String	TRUST_MANAGER_ALGORITHM	= TrustManagerFactory.getDefaultAlgorithm();

	/**
	 * SSL 컨텍스트 생성.
	 * 
	 * @param sslProps
	 *        SSL/TLS 설정값(property)
	 * @return SSLContext
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	public static SSLContext createTlsContext(SslProperties sslProps) throws NoSuchAlgorithmException, KeyManagementException {
		if (sslProps == null) {
			throw new IllegalArgumentException("No SSL Properties.");
		}

		SSLContext sslCtx = SSLContext.getInstance(TLS_V_1_2);
		sslCtx.init(getKeyManagers(sslProps), getTrustManagers(sslProps), null);

		return sslCtx;
	}

	/**
	 * Two-way (mutual) SSL 가능 여부 확인.
	 * 
	 * @param sslProps
	 *        SSL/TLS 설정값(property)
	 * @return Two-way (mutual) SSL 가능 여부 (true/false)
	 */
	public static boolean isMutualTlsAvailable(SslProperties sslProps) {
		if (sslProps == null) {
			LOGGER.debug("isMutualTlsAvailable=false (sslProps=null)");
			return false;
		}

		if (sslProps.getMutualTlsCrtFilePath().isEmpty() || sslProps.getMutualTlsKeyFilePath().isEmpty()) {
			// crt+key 설정 누락
			if (sslProps.getKeyStoreFilePath().isEmpty()) {
				// jks 설정 누락
				LOGGER.debug("isMutualTlsAvailable=false");
				return false;
			}
		}

		LOGGER.debug("isMutualTlsAvailable=true");
		return true;
	}

	/* ======================================================================================= */

	/**
	 * 서버에서 클라이언트를 인증하기 위한 KeyManager[] 리턴.
	 * two-way (mutual) ssl 인 경우에는 KeyManager 필요.
	 * one-way ssl 인 경우에는 공인인증서의 경우 null 리턴 가능.
	 * 
	 * @param sslProps
	 * @return KeyManager[]
	 */
	private static KeyManager[] getKeyManagers(SslProperties sslProps) {

		KeyStore keyStore = getKeyStoreFromCertAndKey(sslProps);
		if (keyStore == null) {
			keyStore = getKeyStoreFromClientKeyStoreInputStream(sslProps);
			if (keyStore == null) {
				LOGGER.info("****** one-way ssl will be used.");
				// Two-way (mutual) ssl 사용 불가
				return null;
			} else {
				LOGGER.info("****** one-way & two-way ssl can be used : KEYSTORE={}", sslProps.getKeyStoreFilePath());
			}
		} else {
			LOGGER.info("****** one-way & two-way ssl can be used : MUTUAL_TLS_CRT={}, MUTUAL_TLS_KEY={}",
					sslProps.getMutualTlsCrtFilePath(), sslProps.getMutualTlsKeyFilePath());
		}

		try {
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KEY_MANAGER_ALGORITHM);
			kmf.init(keyStore, sslProps.getKeyStoreKeyPassword().toCharArray());
			return kmf.getKeyManagers();

		} catch (Exception e) {
			LOGGER.warn("****** Fail to generate KeyManager", e);
		}
		return null;
	}

	private static KeyStore getKeyStoreFromCertAndKey(SslProperties sslProps) {

		// 메모리 상의 KeyStore 객체에 사용할 패스워드를 설정 대신 랜덤값으로 설정
		sslProps.setKeyStorePasswordRandomIfNotNull();
		sslProps.setKeyStoreKeyPasswordRandomIfNotNull();
		
		byte[] encodedKey = sslProps.createClientPrivateKeyFileBytes();
		byte[] certData = sslProps.createClientCertFileBytes();
		if (encodedKey == null || certData == null) {
			return null;
		}
		try {
			KeyFactory kf = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
			PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));

			// sun.security.x509.X509CertImpl is internal proprietary API and may be removed in a future release
			X509Certificate crt = (X509Certificate) new X509CertImpl(certData);

			// keystore 저장
			String alias = ""; //cnValue
			KeyStore keystore = KeyStore.getInstance(KEY_STORE_TYPE);
			keystore.load(null, sslProps.getKeyStorePassword().toCharArray());
			keystore.setKeyEntry(alias, privateKey, sslProps.getKeyStoreKeyPassword().toCharArray(), new X509Certificate[]{ crt });
			//keystore.store(outputStream, keyPw.toCharArray());

			return keystore;
		} catch (Exception e) {
			LOGGER.warn("****** Fail to create KeyStore : cert={}, key={}", sslProps.getMutualTlsCrtFilePath(),
					sslProps.getMutualTlsKeyFilePath(), e);
		}
		return null;
	}

	private static KeyStore getKeyStoreFromClientKeyStoreInputStream(SslProperties sslProps) {
		InputStream keyStoreStream = sslProps.createClientKeyStoreInputStream();
		if (keyStoreStream == null) {
			return null;
		}
		try {
			KeyStore keyStore = KeyStore.getInstance(KEY_STORE_TYPE);
			keyStore.load(keyStoreStream, sslProps.getKeyStorePassword().toCharArray());
		} catch (Exception e) {
			LOGGER.warn("****** Fail to create KeyStore : KEYSTORE={}", sslProps.getKeyStoreFilePath(), e);
		} finally {
			closeQuietly(keyStoreStream);
		}
		return null;
	}

	/**
	 * 일반적인 SSL 서버 인증을 위한 TrustManager[] 리턴.
	 * 사설인증서를 사용하는 경우 TrustManager 필요.
	 * 공인인증서를 사용하는 경우 null 리턴.
	 * 
	 * @param sslProps
	 * @return
	 */
	private static TrustManager[] getTrustManagers(SslProperties sslProps) {

		InputStream keyStoreStream = sslProps.createTrustKeyStoreInputStream();
		if (keyStoreStream == null) {
			LOGGER.info("****** public certification will be used.");
			// 서버가 사설인증서인 경우 사용 불가
			return null;
		}

		try {
			LOGGER.info("****** private certification will be used : TRUST_KEYSTORE={}", sslProps.getTrustKeyStoreFilePath());
			KeyStore trustStore = KeyStore.getInstance(KEY_STORE_TYPE);
			trustStore.load(keyStoreStream, sslProps.getTrustStorePassword().toCharArray());

			TrustManagerFactory trustManager = TrustManagerFactory.getInstance(TRUST_MANAGER_ALGORITHM);
			trustManager.init(trustStore);
			return trustManager.getTrustManagers();

		} catch (Exception e) {
			LOGGER.warn("****** Fail to generate TrustManager", e);
		} finally {
			closeQuietly(keyStoreStream);
		}
		return null;
	}

	private static void closeQuietly(InputStream in) {
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				LOGGER.warn("Fail to close InputStream", e);
			}
		}
	}
}
