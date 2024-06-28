
package com.sds.iot.sdk.exception;

/**
 * 이 클래스는 런타임 오류 시 내부에서 사용되는 예외 클래스 입니다.
 * 
 * @author SDS
 */
public class IotException extends RuntimeException {
	private static final long	serialVersionUID	= -2831996521479743122L;
	private final String		errorCode;

	/**
	 * @param errorCode
	 *        3자리
	 */
	public IotException(String errorCode) {
		super();
		validate(errorCode);
		this.errorCode = errorCode;
	}

	/**
	 * @param errorCode
	 *        3자리
	 * @param message
	 *        메시지
	 */
	public IotException(String errorCode, String message) {
		super(message);
		validate(errorCode);
		this.errorCode = errorCode;
	}

	/**
	 * @param errorCode
	 *        3자리
	 * @param cause
	 *        오류 객체
	 */
	public IotException(String errorCode, Throwable cause) {
		super(cause);
		validate(errorCode);
		this.errorCode = errorCode;
	}

	/**
	 * @param errorCode
	 *        3자리
	 * @param message
	 *        메시지
	 * @param cause
	 *        오류 객체
	 */
	public IotException(String errorCode, String message, Throwable cause) {
		super(message, cause);
		validate(errorCode);
		this.errorCode = errorCode;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public boolean isClientError() {
		return errorCode.startsWith("4");
	}

	public boolean isServerError() {
		return errorCode.startsWith("5");
	}

	private static void validate(String errorCode) {
		if (errorCode == null || errorCode.length() != 3) {
			throw new IllegalArgumentException("Invalid errorCode [" + errorCode + "]");
		}
	}

	@Override
	public String toString() {
		String s = getClass().getName();
		String message = getLocalizedMessage();
		return (message != null) ? (s + ": " + "[" + errorCode + "] " + message) : s + ": " + "[" + errorCode + "]";
	}
}
