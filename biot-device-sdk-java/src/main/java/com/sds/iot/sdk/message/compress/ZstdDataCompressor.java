
package com.sds.iot.sdk.message.compress;

import com.github.luben.zstd.Zstd;
import com.sds.iot.sdk.message.IotMessage;

/**
 * 이 클래스는 메시지 데이터 압축 관련 함수를 제공합니다.
 * 
 * @author SDS
 */
public class ZstdDataCompressor implements IDataCompressor {

	/**
	 * 메시지의 데이터부가 해당 바이트 크기 이상인 경우 압축 수행합니다.
	 * <li>디폴트 설정 0 byte (=압축 미적용)</li>
	 * <li>모든 데이터 압축 적용시 1 byte 설정</li>
	 * <li>1MB 이상 데이터 압축 적용시 1_048_576 bytes 설정</li>
	 */
	private final int compressUseBytes;

	public ZstdDataCompressor(int compressUseBytes) {
		if (compressUseBytes < 0) {
			throw new IllegalArgumentException("Invalid compressUseBytes.");
		}
		this.compressUseBytes = compressUseBytes;
	}

	@Override
	public String getCompressType() {
		return "Z";
	}

	@Override
	public byte[] compress(byte[] plainData) {
		return Zstd.compress(plainData);
	}

	@Override
	public byte[] decompress(byte[] encryptedData) {
		// the original size of a compressed buffer (if known)
		int originalSize = (int) Zstd.decompressedSize(encryptedData);

		return Zstd.decompress(encryptedData, originalSize);
	}

	/**
	 * 인증, keep-alive 메시지는 압축 수행 제외.
	 */
	@Override
	public boolean needCompress(IotMessage msg) {
		if (isNoCompressMessage(msg)) {
			return false;
		}
		return (msg.getData() != null && compressUseBytes !=0 && msg.getData().length >= compressUseBytes);
	}

}
