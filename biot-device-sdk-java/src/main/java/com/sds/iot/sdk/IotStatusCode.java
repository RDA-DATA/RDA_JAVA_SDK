package com.sds.iot.sdk;

/**
 * B.IoT 서버에서 디바이스로의 메시지에 포함된 상태 코드를 정의합니다.
 * 또한 SDK 내부의 실행 결과 및 상태를 나타내기도 합니다.
 *
 * @author SDS
 */
public enum IotStatusCode {
    // Internal Code
    /** 1 - Not Connected - 프로토콜과 상관 없이 서버와 connect 할 수 없을 때 */
    NOT_CONNECT(1, "Not Connected"), // 프로토콜과 상관 없이 서버와 connect 할 수 없을 때,
    /** 2 - No Response - 서버로 부터 응답값을 받을 수 없을 때 */
    NO_RESPONSE(2, "No Response"),  // 서버로 부터 응답값을 받을 수 없을 때,

    // External Code
    /** Null */
    NULL(800, "NULL"),

    /** 200 - OK */
    SUCCESS_OK(200, "OK"),
    /** 201 - Created */
    SUCCESS_CREATED(201, "Created"),
    /** 202 - Accepted */
    SUCCESS_ACCEPTED(202, "Accepted"),
    /** 203 - Non-Authoritative Information */
    SUCCESS_NON_AUTH_INFO(203, "Non-Authoritative Information"),
    /** 204 - No Content */
    SUCCESS_NO_CONTENT(204, "No Content"),
    /** 302 - Found */
    REDIRECTION_FOUND(302, "Found"),

    /** 400 - Bad Request */
    CLIENT_ERROR_BAD_REQUEST(400, "Bad Request"),
    /** 401 - Unauthorized */
    CLIENT_ERROR_UNAUTHORIZED(401, "Unauthorized"),
    /** 402 - Payment Required */
    CLIENT_ERROR_PAYMENT_REQUIRED(402, "Payment Required"),
    /** 403 - Forbidden */
    CLIENT_ERROR_FORBIDDEN(403, "Forbidden"),
    /** 404 - Not Found */
    CLIENT_ERROR_NOT_FOUND(404, "Not Found"),
    /** 405 - Method Not Allowed */
    CLIENT_ERROR_METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    /** 406 - Not Acceptable */
    CLIENT_ERROR_NOT_ACCEPTABLE(406, "Not Acceptable"),
    /** 407 - Proxy Authentication Required */
    CLIENT_ERROR_PROXY_AUTHENTICATION(407, "Proxy Authentication Required"),
    /** 408 - Request Timeout */
    CLIENT_ERROR_REQUEST_TIMEOUT(408, "Request Timeout"),
    /** 409 - Conflict */
    CLIENT_ERROR_CONFLICT(409, "Conflict"),
    /** 412 - Precondition Failed */
    CLIENT_ERROR_PRECONDITION_FAILED(412, "Precondition Failed"),
    /** 415 - Unsupported Media Type */
    CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),

    /** 500 - Internal Server Error */
    SERVER_ERROR_INTERNAL(500, "Internal Server Error"),
    /** 501 - Not Implemented */
    SERVER_ERROR_NOT_IMPLEMENTED(501, "Not Implemented"),
    /** 502 - Bad Gateway */
    SERVER_ERROR_BAD_GATEWAY(502, "Bad Gateway"),
    /** 503 - Service Unavailable */
    SERVER_ERROR_SERVICE_UNAVAILABLE(503, "Service Unavailable"),
    /** 504 - Gateway Timeout */
    SERVER_ERROR_GATEWAY_TIMEOUT(504, "Gateway Timeout"),
    /** 505 - HTTP Version Not Supported */
    SERVER_ERROR_HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported"),

    /** 999 - Unclassified Status - 서버에서 분류할 수 없는 오류가 발생했을 때 */
    UNCLASSIFIED_FAIL(999, "Unclassified Status"); 	//999 분류할 수 없는 오류

    private int		code;
    private String	message;

    /**
     * IotStatusCode constructor.
     *
     * @param {int}
     *        code
     * @param {String}
     *        message
     */
    IotStatusCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * get statusCode's int(code).
     *
     * @return {int} code
     */
    public int getCode() {
        return code;
    }

    /**
     * get statusCode's String(code)
     *
     * @return {string} code
     */
    public String getCodeString() {
        return Integer.toString(code);
    }

    /**
     * get statusCode's String(message).
     *
     * @return {String} message
     */
    public String getMessage() {
        return message;
    }

    /**
     * get OCPResultCode by int(code).
     *
     * @param errorCode error code
     * @return {IotResultCode} IoTResultCode
     */
    public static IotStatusCode getErrorCodeByCode(int errorCode) {
        for (IotStatusCode code : IotStatusCode.values()) {
            if (code.getCode() == errorCode) {
                return code;
            }
        }

        return IotStatusCode.SUCCESS_OK;
    }

    /**
     * get String(code+message).
     *
     * @return {String} message
     */
    public String getCodeMessage() {
        return "[" + code + "] " + message;
    }
}
