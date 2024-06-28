
package com.sds.iot.sdk.protocol;

/**
 * 이 클래스는 재연결의 기본값을 제공합니다.
 * 여기에 정의된 값은 {IotCoreClient}를 통해  런타임에 재정의 할 수 있습니다.
 * 
 * @author SDS
 *
 */
public class RetryConfig {

	/* CONNECTION RETRY ============================================================= */

	private int		connectRetryMaxCount			= 10_000;
	private long	connectRetryBaseDelay			= 3_000;
	private long	connectRetryMaxDelay			= 120_000;
	private int		connectRetryDelayRandomRange	= 2_000;

	/**
	 * @return 재연결 최대 시도 횟수
	 */
	public int getConnectRetryMaxCount() {
		return connectRetryMaxCount;
	}

	/**
	 * default is 10,000.
	 * 
	 * @param connectRetryMaxCount
	 *        재연결 최대 시도 횟수
	 */
	public void setConnectRetryMaxCount(int connectRetryMaxCount) {
		this.connectRetryMaxCount = connectRetryMaxCount;
	}

	/**
	 * @return 재연결 지연 시간 (초)
	 */
	public long getConnectRetryBaseDelay() {
		return connectRetryBaseDelay;
	}

	/**
	 * milliseconds. default 3,000ms
	 * 
	 * @param connectRetryBaseDelay
	 *        재연결 지연 시간 (초)
	 */
	public void setConnectRetryBaseDelay(long connectRetryBaseDelay) {
		this.connectRetryBaseDelay = connectRetryBaseDelay;
	}

	/**
	 * @return 재연결 최대 지연 시간 (초)
	 */
	public long getConnectRetryMaxDelay() {
		return connectRetryMaxDelay;
	}

	/**
	 * milliseconds. default 120,000ms (=2minutes)
	 * 
	 * @param connectRetryMaxDelay
	 *        재연결 최대 지연 시간 (초)
	 */
	public void setConnectRetryMaxDelay(long connectRetryMaxDelay) {
		this.connectRetryMaxDelay = connectRetryMaxDelay;
	}

	/**
	 * @return 재연결 지연 랜덤 범위 (초)
	 */
	public int getConnectRetryDelayRandomRange() {
		return connectRetryDelayRandomRange;
	}

	/**
	 * milliseconds. default 2,000ms
	 * if the value equals or is smaller than 0, random range is not added.
	 * 
	 * @param connectRetryDelayRandomRange
	 *        재연결 지연 랜덤 범위 (초)
	 */
	public void setConnectRetryDelayRandomRange(int connectRetryDelayRandomRange) {
		this.connectRetryDelayRandomRange = connectRetryDelayRandomRange;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("  connectRetryMaxCount=").append(connectRetryMaxCount).append("\n  connectRetryBaseDelay=")
				.append(connectRetryBaseDelay).append("\n  connectRetryMaxDelay=").append(connectRetryMaxDelay)
				.append("\n  connectRetryDelayRandomRange=").append(connectRetryDelayRandomRange);
		return builder.toString();
	}

}
