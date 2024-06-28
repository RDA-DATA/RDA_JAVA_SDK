
package com.sds.iot.sdk.protocol.http;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.iot.sdk.IotClientConfig;
import com.sds.iot.sdk.exception.IotException;
import com.sds.iot.sdk.protocol.tls.TlsSupport;

/**
 * 이 클래스는 클라이언트에서 웹소켓을 이용한 서버 연결 요청/응답에 대한 함수를 제공합니다.
 * 
 * @author SDS
 */
public class SimpleHttpClient {
	private static final Logger		LOGGER					= LoggerFactory.getLogger(SimpleHttpClient.class);

	// 16KB
	private static final int		MAX_RESPONSE_BODY_LEN	= 16_384;

	private final IotClientConfig	cfg;

	private int						connectTimeout			= 3_000;
	private int						readTimeout				= 5_000;

	private SSLContext				sslCtx					= null;

	/**
	 * Proxy, SSL 설정이 없는 경우 사용 가능한 생성자.
	 */
	public SimpleHttpClient() {
		this.cfg = null;
	}

	/**
	 * @param cfg
	 *        : Proxy 설정, SSL 설정 정보 참고 용도.
	 */
	public SimpleHttpClient(IotClientConfig cfg) {
		this.cfg = cfg;
	}

	/**
	 * 서버로 해당 URL 데이터 요청.
	 * 
	 * @param method
	 *        GET/POST/HEAD/OPTIONS/PUT/DELETE/TRACE
	 * @param urlStr
	 *        request url
	 * @param requestBody
	 *        request body
	 * @param requestHeaders
	 *        request header
	 * @return 서버 응답 내용
	 * @throws IotException
	 */
	public byte[] request(String method, String urlStr, byte[] requestBody, Map<String, Object> requestHeaders)
			throws IotException {
		URL url;
		try {
			url = new URL(urlStr);
		} catch (MalformedURLException e) {
			throw new IotException("400", e);
		}
		return request(method, url, requestBody, requestHeaders);
	}

	/**
	 * @param method
	 *        GET/POST/HEAD/OPTIONS/PUT/DELETE/TRACE
	 * @param url
	 *        request url
	 * @param requestBody
	 *        request body
	 * @param requestHeaders
	 *        request header
	 * @return 서버 응답 내용
	 * @throws IotException
	 */
	public byte[] request(String method, URL url, byte[] requestBody, Map<String, Object> requestHeaders) throws IotException {
		HttpURLConnection connection = null;

		try {
			connection = makeConnection(method, url, connectTimeout, readTimeout);

			if (requestHeaders != null) {
				for (Entry<String, Object> e : requestHeaders.entrySet()) {
					connection.setRequestProperty(e.getKey(), e.getValue().toString());
					LOGGER.trace("       request header - {} : {}", e.getKey(), e.getValue());
				}
			}

			LOGGER.trace("[HTTP] Send request");

			if (requestBody != null && requestBody.length > 0) {
				if ("POST".equals(method) || "PUT".equals(method)) {
					// POST, PUT 의 경우, 이 메서드에서 실제로 socket connect 를 수행
					sendHttpRequestBody(connection, requestBody);
				} else {
					throw new IotException("400", "HTTP request body can be used for only POST and PUT method.");
				}
			} else if ("POST".equals(method) || "PUT".equals(method)) {
				LOGGER.warn("[HTTP] Request body is empty");
			}

			// GET, DELETE 의 경우, 이 메서드에서 실제로 socket connect 를 수행, send request, receive response.
			// read timed out 발생 시 여기서 IOException 을 던짐
			int resCode = connection.getResponseCode();

			if (isResponseCodeOk(resCode)) {
				// SUCCESS
				if (LOGGER.isDebugEnabled()) {
					LOGGER.trace("[HTTP] Response received : {} {}", resCode, connection.getResponseMessage());
				}
				byte[] resBody = getReturnValueByte(connection.getInputStream(), connection.getContentLengthLong());
				if (LOGGER.isDebugEnabled()) {
					LOGGER.trace("[HTTP] Response body : {}", new String(resBody, Charset.defaultCharset()));
				}
				return resBody;
			} else {
				// FAIL
				LOGGER.warn("[HTTP] Response received : {} {}", resCode, connection.getResponseMessage());
				byte[] resBody = getReturnValueByte(connection.getErrorStream(), -1);
				LOGGER.warn("[HTTP] Response body : {}", new String(resBody, Charset.defaultCharset()));
				throw new IotException(String.valueOf(resCode),
						"Server '" + url.getHost() + ":" + url.getPort() + "' returned HTTP response code: " + resCode);
			}

		} catch (IOException e) {
			LOGGER.warn("[HTTP] {}", e.toString());
			throw new IotException("500", e);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	/**
	 * 서버로 연결
	 * 
	 * @param method
	 *        GET/POST/HEAD/OPTIONS/PUT/DELETE/TRACE
	 * @param url
	 *        request url
	 * @param connectTimeout
	 *        an int that specifies the connect timeout value in milliseconds
	 * @param readTimeout
	 *        an int that specifies the connect timeout value in milliseconds
	 * @return HttpURLConnection instance
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	private HttpURLConnection makeConnection(String method, URL url, int connectTimeout, int readTimeout)
			throws IllegalArgumentException, IOException {
		LOGGER.info("[HTTP] Connect : {} {}", method, url);

		Proxy proxy = null;
		if (cfg != null && cfg.getHttpProxyIp() != null && cfg.getHttpProxyPort() != null) {
			//url.getHost()
			proxy = new Proxy(Proxy.Type.HTTP,
					new InetSocketAddress(cfg.getHttpProxyIp(), Integer.parseInt(cfg.getHttpProxyPort())));
		}

		HttpURLConnection connection = null;
		if (proxy != null) {
			LOGGER.debug("[HTTP] with proxy");
			connection = (HttpURLConnection) url.openConnection(proxy);
		} else {
			connection = (HttpURLConnection) url.openConnection();
		}

		if ("https".equals(url.getProtocol())) {
			LOGGER.debug("[HTTP] with ssl");
			withSSL((HttpsURLConnection) connection);
		}

		connection.setDoOutput(true);
		connection.setConnectTimeout(connectTimeout);
		connection.setReadTimeout(readTimeout);

		try {
			connection.setRequestMethod(method.toUpperCase(Locale.getDefault()));
		} catch (ProtocolException e) {
			throw new IotException("400", "Unsupported http method. method=" + method, e);
		}

		return connection;
	}

	private void withSSL(HttpsURLConnection connection) {

		// init ssl context
		if (this.sslCtx == null) {
			if (cfg == null || cfg.getSslProperties() == null) {
				//LOGGER.error("Cannot SSL context initialize. No Config.");
				throw new IllegalStateException("Cannot SSL context initialize. No Config.");
			} else {
				try {
					this.sslCtx = TlsSupport.createTlsContext(cfg.getSslProperties());
				} catch (Exception e) {
					//LOGGER.error("Fail to SSL context initialize : {}", e.toString(), e);
					throw new IllegalStateException("Fail to SSL context initialize", e);
				}
			}
		}

		// socket factory
		connection.setSSLSocketFactory(sslCtx.getSocketFactory());

		// hostname verifier
		//connection.setHostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier());
		connection.setHostnameVerifier(new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				LOGGER.trace("[HTTP] NoopHostnameVerifier");
				return true;
			}
		});
	}

	private boolean isResponseCodeOk(int resCode) {
		if (resCode >= 200 && resCode < 400) {
			return true;
		}
		return false;
	}

	private byte[] getReturnValueByte(InputStream is, long contentLength) throws IOException {

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		int nRead;
		byte[] data = new byte[contentLength == -1 || contentLength > MAX_RESPONSE_BODY_LEN ? MAX_RESPONSE_BODY_LEN
				: (int) contentLength];

		try {
			while ((nRead = is.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}

			buffer.flush();
			data = buffer.toByteArray();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					LOGGER.info("[HTTP] IOException : {}", e.toString());
				}
			}
			if (buffer != null) {
				try {
					buffer.close();
				} catch (IOException e) {
					LOGGER.info("[HTTP] IOException : {}", e.toString());
				}
			}
		}

		return data;
	}

	private void sendHttpRequestBody(HttpURLConnection connection, byte[] content) throws IOException {

		OutputStream os = connection.getOutputStream();

		DataOutputStream wr = new DataOutputStream(os);

		try {
			wr.write(content);
			wr.flush();
		} finally {
			if (wr != null) {
				try {
					wr.close();
				} catch (IOException e) {
					LOGGER.info("[HTTP] IOException : {}", e.toString());
				}
			}
		}
	}

}
