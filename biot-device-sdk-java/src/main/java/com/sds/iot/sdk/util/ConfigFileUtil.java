
package com.sds.iot.sdk.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.iot.sdk.protocol.tls.SslProperties;

/**
 * 이 클래스는 설정 파일(*.properties)을 관리합니다.
 * 설정 파일을 읽거나, 객체에 매핑할 수 있습니다.
 * 
 * @author SDS
 */
public class ConfigFileUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigFileUtil.class);

	/**
	 * 설정 파일 읽기
	 * @param props
     *        설정값
	 * @param propsFilePath
     *        설정 파일 경로
	 */
	public static void loadProps(Properties props, String propsFilePath) {

		if (propsFilePath == null || propsFilePath.isEmpty()) {
			LOGGER.error("****** Empty property file path");
			throw new IllegalArgumentException("Empty property file path");
		}

		InputStream propsFileInputStream = ConfigFileUtil.createFileInputStream(propsFilePath);

		if (propsFileInputStream == null) {
			LOGGER.error("****** Cannot find property file : {}", propsFilePath);
			throw new IllegalArgumentException("Cannot find property file : " + propsFilePath);
		}

		try {
			// Load default property file.
			props.load(propsFileInputStream);

		} catch (IOException e) {
			LOGGER.warn("****** exception : {}", e.toString(), e);
		} finally {
			try {
				propsFileInputStream.close();
			} catch (IOException e) {
				LOGGER.warn("Fail to close InputStream", e);
			}
		}
	}

	/**
	 * 설정 파일 생성
	 * 
	 * @param filePath
     *        설정 파일 경로
	 * @return 생성된 파일 (InputStream)
	 */
	public static InputStream createFileInputStream(String filePath) {

		// URL Path 는 \ 가 아닌 / 로만 처리 가능함
		if (filePath.contains("\\")) {
			filePath = replace(filePath, "\\", "/");
		}

		// 파일로부터 FileInputStream 객체를 얻음
		File file = new File(filePath);
		String path = "";
		if (file.exists()) {
			path = file.getAbsolutePath();
			LOGGER.trace("****** file : {}", path);

		} else {
			path = SslProperties.class.getClassLoader().getResource("").getPath() + filePath;
			file = new File(path);
			LOGGER.trace("****** file (classpath) : {}", path);
		}

		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			LOGGER.warn("****** cannot read a file : {}", file.getAbsolutePath());
			return null;
		}
	}

	/**
	 * 설정 정보 생성
	 * 
	 * @param in
     *        input stream
	 * @return 생성된 설정 정보 (byte[])
	 */
	public static byte[] createBytesFromInputStream(InputStream in) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			int read = 0;
			while ((read = in.read(buf)) != -1) {
				bos.write(buf, 0, read);
			}
			bos.close();

			return bos.toByteArray();
		} catch (IOException e) {
			LOGGER.warn("****** cannot make byte[] from InputStream : {}", e.toString(), e);
			return null;
		}
	}

	/**
	 * Replace all occurrences of a substring within a string with another string.
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
	 * 파일에서 읽은 설정 정보를 객체 변수에 매핑
     * 
	 * @param props
     *        설정
	 * @param propertyPrefix
     *        설정 키 접두사
	 * @param obj
     *        설정 값 매핑 대상 객체
	 * @param varName
     *        설정 값 매핑 변수명
	 * @param valType
     *        설정 값 매핑 타입
	 */
	public static void invokeSetterIfExists(Properties props, String propertyPrefix, Object obj, String varName, Class<?> valType) {
		String propertyName = propertyPrefix + varName;
		String valStr = props.getProperty(propertyName);
		if (valStr != null) {
			Object val;
			if (valType == String.class) {
				val = valStr;
			} else if (valType == int.class || valType == Integer.class) {
				val = Integer.parseInt(valStr.trim());
			} else if (valType == long.class || valType == Long.class) {
				val = Long.parseLong(valStr.trim());
			} else if (valType == boolean.class || valType == Boolean.class) {
				val = Boolean.parseBoolean(valStr.trim());
			} else {
				val = valStr;
			}

			String setterMethodName = "set" + varName.substring(0, 1).toUpperCase() + varName.substring(1);
			try {
				Method setter = obj.getClass().getMethod(setterMethodName, valType);
				setter.invoke(obj, val);
				LOGGER.trace("set config  : {}{}={}", propertyPrefix, varName, valStr);
			} catch (Exception e) {
				LOGGER.trace("{} : {}", setterMethodName, e.toString()); // 무시함
			}
		} else {
			LOGGER.trace("use default : {}{}", propertyPrefix, varName);
		}
	}

}
