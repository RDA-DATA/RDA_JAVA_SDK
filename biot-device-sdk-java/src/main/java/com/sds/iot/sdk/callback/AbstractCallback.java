
package com.sds.iot.sdk.callback;

/**
 * 이 클래스는 콜백 Abstract 클래스입니다.
 * 
 * @author SDS
 */
public abstract class AbstractCallback implements IResponseCallback {
	protected final long	createTime	= System.currentTimeMillis();

	/** callback default time out (milliseconds) */
	protected long			timeout		= 10_000L;

	@Override
	public long getCreateTime() {
		return this.createTime;
	}

	@Override
	public long getTimeout() {
		return this.timeout;
	}

	@Override
	public void setTimeout(long timeoutMillis) {
		this.timeout = timeoutMillis;
	}

}
