
package com.sds.iot.sdk.util;

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 * 이 클래스는 Json 데이터 포맷을 관리합니다.
 * 
 * @author SDS
 *
 */
public class JsonUtil {
	private static final Gson		GSON		= new GsonBuilder().create();
	private static final Type		MAP_TYPE	= new TypeToken<HashMap<String, Object>>() {}.getType();
	private static final JsonParser	PARSER		= new JsonParser();

	/**
	 * JsonElement를 Json으로 변환.
	 * @param src
	 * @return 변환된 Json 데이터
	 */
	public static String toJson(JsonElement src) {
		return GSON.toJson(src);
	}

	/**
	 * Object를 Json으로 변환.
	 * @param src
	 * @return 변환된 Json 데이터
	 */
	public static String toJson(Object src) {
		return GSON.toJson(src);
	}

	/**
	 * Json데이터를 Map으로 변환.
	 * @param jsonStr
	 * @return 변환된 Map 데이터
	 */
	public static Map<String, Object> fromJsonToMap(String jsonStr) {
		return GSON.fromJson(jsonStr, MAP_TYPE);
	}

	/**
	 * Json데이터를 Map으로 변환.
	 * @param jsonBytes
	 * @return 변환된 Map 데이터
	 */
	public static Map<String, Object> fromJsonToMap(byte[] jsonBytes) {
		return GSON.fromJson(new String(jsonBytes, Charset.defaultCharset()), MAP_TYPE);
	}

	/**
	 * Json데이터 파싱
	 * @param jsonStr
	 * @return
	 */
	public static JsonElement parse(String jsonStr) {
		return PARSER.parse(jsonStr);
	}
}
