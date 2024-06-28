
package com.sds.iot.sdk;

import java.util.Locale;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.iot.sdk.protocol.IProtocolConfig;
import com.sds.iot.sdk.protocol.RetryConfig;
import com.sds.iot.sdk.protocol.tls.SslProperties;
import com.sds.iot.sdk.util.ConfigFileUtil;

/**
 * 이 클래스는 B.IoT 라이브러리의 기본값을 제공합니다.
 * 여기에 정의된 값은 {@link IotClient}를 통해 런타임에 재정의 할 수 있습니다.
 * 
 * 
 * @author SDS
 */
public class IotClientConfig {
	private static final Logger		LOGGER						= LoggerFactory.getLogger(IotClientConfig.class);

	/* ============================================================= */

	private final String			siteId;
	private final String			thingName;

	/* ============================================================= */

	private String					apiServerAddr				= "https://test-brighticsiot.samsungsds.com:8088";

	/** Server address for connect (If you use ITA auth, you do not need to set this address. */
	private String					serverAddr;

	/** Protocol type. default 'MQTT'. */
	private String					protocolType				= "MQTT";

	/** Message header type. default 'D'. (D:Delimiter / J:JSON) */
	private String					headerType					= "D";

	/**
	 * Encryption type. default '0'.
	 * <li>0 : None</li>
	 * <li>1 : DH_AES256(Deprecated)</li>
	 * <li>2 : SEAL(Unsupported)</li>
	 * <li>3 : ITA_AES128</li>
	 * <li>4 : WBC(Unsupported)</li>
	 * <li>5 : ITA_AES256(Unsupported)</li>
	 */
	private String					encType						= "0";

	/**
	 * Minimum data bytes to compress. default '0'.
	 * (Note: Data is compressed when sending message.
	 * But auth and keep-alive messages will NOT be compressed regardless of this config.)
	 * e.g.
	 * if compressUseBytes=0, No compression.
	 * if compressUseBytes=1, All message data will be compressed.
	 * if compressUseBytes=1048576, Message data greater than 1MB will be compressed.
	 * if compressUseBytes greater than N, Message data greater than N bytes will be compressed.
	 */
	private int						compressUseBytes			= 0;

	private String					httpProxyIp					= System.getProperty("http.proxyHost");
	private String					httpProxyPort				= System.getProperty("http.proxyPort");

	/**
	 * Interval of sending keep alive message to server. (unit: seconds)
	 * default 300 sec.(= 5 minutes)
	 */
	private int						keepAliveInterval			= 300;

	/**
	 * Default timeout of waiting response for sync request message. (unit: seconds)
	 * default 5 sec.
	 */
	private int						defaultSyncResponseTimeout	= 5;

	private RetryConfig				retryCfg					= new RetryConfig();

	/* ============================================================= */

	private SslProperties			sslProperties				= null;

	private IProtocolConfig			protocolCfg					= null;

	/* ============================================================= */

	transient private Properties	props						= null;
	transient private String		tempStr;

	/* ============================================================= */

	/**
	 * @param siteId
	 *        IoT 서버에 등록된 사이트 ID
	 *        : e.g. "CB00000000"
	 * @param thingName
	 *        IoT 서버에 등록된 사물명
	 */
	public IotClientConfig(String siteId, String thingName) {
		this(siteId, thingName, null);
	}

	/**
	 * @param siteId
	 *        IoT 서버에 등록된 사이트 ID
	 *        : e.g. "CB00000000"
	 * @param thingName
	 *        IoT 서버에 등록된 사물명
	 * @param configFilePath
	 *        : configuration properties file path. (relative or abstract)
	 *        e.g. "biot_client.properties"
	 */
	public IotClientConfig(String siteId, String thingName, String configFilePath) {
		this.siteId = siteId;
		this.thingName = thingName;

		if (configFilePath != null) {
			Properties props = new Properties();
			ConfigFileUtil.loadProps(props, configFilePath);

			initFromProperties(props);

			this.props = props;
		}
	}

	/**
	 * @param siteId
	 *        : e.g. "CB00000000"
	 * @param thingName
	 * @param apiServerAddr
	 *        : e.g. "https://test-brighticsiot.samsungsds.com:8088"
	 * @param serverAddr
	 *        : if it uses 'ITA' auth, set null.
	 *        e.g. "ssl://test-brighticsiot.samsungsds.com:8001"
	 */
	public IotClientConfig(String siteId, String thingName, String apiServerAddr, String serverAddr) {
		this.siteId = siteId;
		this.thingName = thingName;
		this.apiServerAddr = apiServerAddr;
		this.serverAddr = serverAddr;
	}

	/* ============================================================= */

	/**
	 * config 파일로 부터 로드된 B.IoT SDK 속성을 이용하여 연결 재시도, SSL 인증 관련 설정값을 준비합니다.
	 * @param props 속성값들
	 */
	private void initFromProperties(Properties props) {

		// common config
		ConfigFileUtil.invokeSetterIfExists(props, "", this, "apiServerAddr", String.class);
		ConfigFileUtil.invokeSetterIfExists(props, "", this, "serverAddr", String.class);
		ConfigFileUtil.invokeSetterIfExists(props, "", this, "protocolType", String.class);
		ConfigFileUtil.invokeSetterIfExists(props, "", this, "headerType", String.class);
		ConfigFileUtil.invokeSetterIfExists(props, "", this, "encType", String.class);
		ConfigFileUtil.invokeSetterIfExists(props, "", this, "compressUseBytes", int.class);
		ConfigFileUtil.invokeSetterIfExists(props, "", this, "httpProxyIp", String.class);
		ConfigFileUtil.invokeSetterIfExists(props, "", this, "httpProxyPort", String.class);
		ConfigFileUtil.invokeSetterIfExists(props, "", this, "keepAliveInterval", int.class);
		ConfigFileUtil.invokeSetterIfExists(props, "", this, "defaultSyncResponseTimeout", int.class);

		// retry config
		ConfigFileUtil.invokeSetterIfExists(props, "retry.", this.retryCfg, "connectRetryMaxCount", int.class);
		ConfigFileUtil.invokeSetterIfExists(props, "retry.", this.retryCfg, "connectRetryBaseDelay", long.class);
		ConfigFileUtil.invokeSetterIfExists(props, "retry.", this.retryCfg, "connectRetryMaxDelay", long.class);
		ConfigFileUtil.invokeSetterIfExists(props, "retry.", this.retryCfg, "connectRetryDelayRandomRange", int.class);

		// ssl config
		this.sslProperties = new SslProperties(props); // 내부 Properties 객체가 동일

		// 참고 : protocolCfg 가 null 인 경우, 
		// IotClient 의 connect() 시점에 initStrategy() 내부에서 객체 생성된 후 설정됨
	}

	/**
	 * 프로토콜 설정 파일로 프로토콜 설정 초기화
	 * @param protocolCfg
     *        프로토콜 설정
	 */
	void initProtocolCfgFromProperties(IProtocolConfig protocolCfg) {

		setProtocolCfg(protocolCfg);

		if (this.props != null) {
			this.protocolCfg.initFromProperties(this.props);
		}
	}

	/* ============================================================= */

	/**
	 * 연결된 사물의 사이트 아이드를 획득합니다.
	 * @return 사이트 아이디
	 */
	public String getSiteId() {
		return siteId;
	}

	/**
	 * 연결된 사물의 이름을 획득합니다.
	 * @return 사물 이름
	 */
	public String getThingName() {
		return thingName;
	}

	/* ============================================================= */

	/**
	 * B.IoT의 API 서버 주소를 획득합니다.
	 * @return IoT API 서버 주소
	 */
	public String getApiServerAddr() {
		return apiServerAddr;
	}

	/**
	 * B.IoT의 API 서버 주소를 설정합니다.
	 * @param apiServerAddr
	 *        IoT API 서버 주소
	 *        : e.g. "https://test-brighticsiot.samsungsds.com:8088"
	 */
	public void setApiServerAddr(String apiServerAddr) {
		this.apiServerAddr = apiServerAddr;
	}

	/**
	 * API 서버 주소가 HTTPS 지원하는지 여부를 알려 줍니다.
	 * @return API 서버 주소가 HTTPS 이면 true 를 반환하고 아니면 false 를 반환합니다.
	 */
	public boolean isApiServerAddrSsl() {
		return apiServerAddr == null ? false : serverAddr.startsWith("https") ? true : false;
	}

	/**
	 * B.IoT의 서버 주소를 획득합니다.
	 * @return IoT 서버 주소
	 */
	public String getServerAddr() {
		return serverAddr;
	}

	/**
	 * @param serverAddr 설정할 B.IoT의 서버 주소
	 *        : if use 'ITA' auth, set null.
	 *        e.g. "ssl://test-brighticsiot.samsungsds.com:8001"
	 */
	public void setServerAddr(String serverAddr) {
		this.serverAddr = serverAddr;
	}

	/**
	 * Return true if 'serverAddress' starts with 'ssl://' or 'wss://', false if not.
	 * 
	 * @return  IoT 서버의 SSL 통신 여부 (true/false)
	 */
	public boolean isServerAddrSsl() {
		return serverAddr == null ? false : serverAddr.startsWith("ssl") || serverAddr.startsWith("wss") ? true : false;
	}

	/**
	 * B.IoT와 연결한 프로토콜 타입을 획득합니다.
	 * @return 프로토콜 타입
	 */
	public String getProtocolType() {
		return protocolType;
	}

	/**
	 *  B.IoT와 연결할 프로토콜 타입을 설정합니다.
	 * @param protocolType
	 *        : e.g. "MQTT"
	 */
	public void setProtocolType(String protocolType) {
		this.protocolType = protocolType.toUpperCase(Locale.getDefault());
	}

	/**
	 * B.IoT 송수신 메시지 헤더 타입을 획득합니다.
	 * @return 헤더 타입
	 *        : e.g. "D"
	 */
	public String getHeaderType() {
		return headerType;
	}

	/**
	 * B.IoT 송수신 메시지 헤더 타입을 설정합니다.
	 * @param headerType 메시지 헤더 타입
	 *        : e.g. "D"
	 */
	public void setHeaderType(String headerType) {
		this.headerType = headerType.toUpperCase(Locale.getDefault());
	}

	/**
	 * B.IoT 송수신 메시지 암호화 방식을 획득합니다.
	 * @return encType
	 */
	public String getEncType() {
		return encType;
	}

	/**
	 * B.IoT 송수신 메시지 암호화 방식을 획득합니다.<br>
	 * Encryption type. default '0'.
	 * <ul>
	 * <li>0 : None</li>
	 * <li>1 : DH_AES256(Deprecated)</li>
	 * <li>2 : SEAL(Unsupported)</li>
	 * <li>3 : ITA_AES128</li>
	 * <li>4 : WBC(Unsupported)</li>
	 * <li>5 : ITA_AES256(Unsupported)</li>
	 * </ul>
	 * 
	 * @param encType
	 *        : e.g. "0"
	 */
	public void setEncType(String encType) {
		this.encType = encType;
	}

	/**
	 * B.IoT 송수신 메시지를 압축할 때, 사용하는 최소 바이트 수를 획득합니다.
	 * @return compressUseBytes
	 */
	public int getCompressUseBytes() {
		return compressUseBytes;
	}

	/**
	 * <pre>
	 * B.IoT 송수신 메시지를 압축할 때, 사용하는 최소 바이트 수를 설정합니다.
	 * Minimum data bytes to compress. default '0'.
	 * (Note: Data is compressed when sending message.
	 * But auth and keep-alive messages will NOT be compressed regardless of this config.)
	 * e.g.
	 * if compressUseBytes=0, No compression.
	 * if compressUseBytes=1, All message data will be compressed.
	 * if compressUseBytes=1048576, Message data greater than 1MB will be compressed.
	 * if compressUseBytes greater than N, Message data greater than N bytes will be compressed.
	 * </pre>
     *
     * @param compressUseBytes
     *        압축 크기
	 */
	public void setCompressUseBytes(int compressUseBytes) {
		this.compressUseBytes = compressUseBytes;
	}

	/**
	 * 프록시 IP를 획득합니다.
	 * @return httpProxyIp
	 */
	public String getHttpProxyIp() {
		return httpProxyIp;
	}

	/**
	 * 프록시 IP를 설정합니다.
	 * @param httpProxyIp
     *        프록시 IP
	 */
	public void setHttpProxyIp(String httpProxyIp) {
		this.httpProxyIp = httpProxyIp;
	}

	/**
	 * 프록시 포트를 획득합니다.
	 * @return httpProxyPort
	 */
	public String getHttpProxyPort() {
		return httpProxyPort;
	}

	/**
	 * 프록시 포트를 설정합니다.
	 * @param httpProxyPort
     *        프록시 Port
	 */
	public void setHttpProxyPort(String httpProxyPort) {
		this.httpProxyPort = httpProxyPort;
	}

	/**
	 * Interval of sending keep alive message to server. (unit: seconds)
	 * default 300 sec.(= 5 minutes)
	 * 
	 * @return keepAliveInterval
	 */
	public int getKeepAliveInterval() {
		return keepAliveInterval;
	}

	/**
	 * Interval of sending keep alive message to server. (unit: seconds)
	 * default 300 sec.(= 5 minutes)
	 * 
	 * @param keepAliveInterval
     *        메시지 전송 Interval
	 */
	public void setKeepAliveInterval(int keepAliveInterval) {
		this.keepAliveInterval = keepAliveInterval;
	}

	/**
	 * 디폴트 응답 타임아웃 시간을 획득합니다.
	 * default 5 sec.
	 * 
	 * @return defaultSyncResponseTimeout
	 */
	public int getDefaultSyncResponseTimeout() {
		return defaultSyncResponseTimeout;
	}

	/**
	 * 디폴트 응답 타임아웃 시간을 설정합니다.
	 * default 5 sec.
	 * 
	 * @param defaultSyncResponseTimeout
     *        동기화 응답 시간 초과 Timeout
	 */
	public void setDefaultSyncResponseTimeout(int defaultSyncResponseTimeout) {
		this.defaultSyncResponseTimeout = defaultSyncResponseTimeout;
	}

	/* ============================================================= */

	/**
	 * 재연결 시도 설정값을 획득합니다.
	 * @return retryCfg
	 */
	public RetryConfig getRetryCfg() {
		return retryCfg;
	}

	/**
	 * 재연결 시도 설정값을 획득합니다.
	 * @param retryCfg 재연결 시도 설정값 (시도 횟수 등등)
	 */
	public void setRetryCfg(RetryConfig retryCfg) {
		if (this.retryCfg != null) {
			LOGGER.info("Replace retryCfg");
		}
		this.retryCfg = retryCfg;
	}

	/**
	 * 프로토콜 관련 설정값을 획득합니다.
	 * @return
	 */
	public IProtocolConfig getProtocolCfg() {
		return protocolCfg;
	}

	/**
	 * 프로토콜 관련 설정값을 설정합니다.
	 * see <code>MqttProtocolConfig</code> class.
	 * 
	 * @param protocolCfg 프로토콜 설정값 (연결 타임아웃 설정 등등)
	 */
	public void setProtocolCfg(IProtocolConfig protocolCfg) {
		if (this.protocolCfg != null) {
			LOGGER.info("Replace protocolCfg");
		}
		this.protocolCfg = protocolCfg;
	}

	/**
	 * SSL 인증 관련 설정값을 획득합니다.
	 * @return
	 */
	public SslProperties getSslProperties() {
		return sslProperties;
	}

	/**
	 * SSL 인증 관련 설정값을 설정합니다.
	 * set 'ssl' config.
	 * 
	 * @param sslProperties
	 */
	public void setSslProperties(SslProperties sslProperties) {
		if (this.sslProperties != null) {
			LOGGER.info("Replace sslProperties");
		}
		this.sslProperties = sslProperties;
	}

	/**
	 * SSL 인증 관련 설정값을 config 파일을 로드하여 설정합니다.
	 * set 'ssl' config.
	 * 
	 * @param sslPropsFilePath
	 */
	public void setSslPropsFilePath(String sslPropsFilePath) {
		if (this.sslProperties != null) {
			LOGGER.info("Replace sslPropsFilePath : {}", sslPropsFilePath);
		}
		this.sslProperties = new SslProperties(sslPropsFilePath);
	}

	/* ============================================================= */

	public String toStringShort() {
		if (tempStr == null) {
			tempStr = String.format("%s/%s", getSiteId(), getThingName());
		}
		return tempStr;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("<");
		builder.append(siteId);
		builder.append("/");
		builder.append(thingName);
		builder.append(">\n");
		if (apiServerAddr != null) {
			builder.append("apiServerAddr=");
			builder.append(apiServerAddr);
			builder.append("\n");
		}
		if (serverAddr != null) {
			builder.append("serverAddr=");
			builder.append(serverAddr);
			builder.append("\n");
		}
		builder.append("protocolType=");
		builder.append(protocolType);
		builder.append("\n");
		builder.append("headerType=");
		builder.append(headerType);
		builder.append("\n");
		builder.append("compressUseBytes=");
		builder.append(compressUseBytes);
		builder.append("\n");
		builder.append("encType=");
		builder.append(encType);
		builder.append("\n");
		if (httpProxyIp != null) {
			builder.append("httpProxyIp=");
			builder.append(httpProxyIp);
			builder.append("\n");
		}
		if (httpProxyPort != null) {
			builder.append("httpProxyPort=");
			builder.append(httpProxyPort);
			builder.append("\n");
		}
		builder.append("keepAliveInterval=");
		builder.append(keepAliveInterval);
		builder.append("\n");
		builder.append("defaultSyncResponseTimeout=");
		builder.append(defaultSyncResponseTimeout);
		builder.append("\n");
		builder.append("retryCfg=[\n");
		builder.append(retryCfg);
		builder.append("\n]");
		if (protocolCfg != null) {
			builder.append("\n");
			builder.append("protocolCfg=[\n");
			builder.append(protocolCfg);
			builder.append("\n]");
		}
		return builder.toString();
	}

}
