
package com.sds.iot.sdk.message.encrypt;

import java.nio.charset.Charset;
import java.security.*;
import java.util.Arrays;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.iot.sdk.auth.IAuth;

/**
 * 이 클래스는 ITA_AES128, ITA_AES256 암/복호화 관련 함수를 제공합니다.
 * 
 * @author SDS
 */
public class ItaAesDataEncryptor implements IDataEncryptor {
	private static final Logger	LOGGER						= LoggerFactory.getLogger(ItaAesDataEncryptor.class);

	private static final String	KEY_ALGORITHM				= "AES";
	private static final String	HASH_ALGORITHM				= "SHA-256";
	private static final String	CIPHER_TRANSFORMATION_CBC	= "AES/CBC/PKCS5Padding";
	private static final String	CIPHER_TRANSFORMATION_GCM	= "AES/GCM/NoPadding";
	private static final int	CBC_IV_LENGTH				= 16;

	/** "3" or "5" */
	// 3 : AES128_CBC
	private final String		ENC_TYPE;

	/** 128 or 256 */
	private final int			BIT_SIZE;

	/** 16 or 32 */
	private final int			BYTE_SIZE;

	private IAuth				auth						= null;
	private String				cachedCredential			= null;
	private Key					cachedKey					= null;

	public ItaAesDataEncryptor(String encType, int bitSize) {
		this.ENC_TYPE = encType;

		if (bitSize < 128 || bitSize % 128 != 0) {
			throw new IllegalArgumentException("Invalid bit size. It should be multiple of 128. (bitSize=" + bitSize + ")");
		}
		this.BIT_SIZE = bitSize;
		this.BYTE_SIZE = bitSize / 8;
	}

	@Override
	public String getEncType() {
		return this.ENC_TYPE;
	}

	@Override
	public void setAuth(IAuth auth) {
		this.auth = auth;

		if (!auth.getAuthTypeName().startsWith("ITA")) {
			throw new IllegalArgumentException("Invalid configuration! ITA_AES data encryptor can be used ITA auth! (authType="
					+ auth.getAuthTypeName() + ", encType=" + this.getEncType() + ")");
		}
	}

	@Override
	public byte[] encrypt(byte[] plainData) {

		byte[] ivBytes = new byte[CBC_IV_LENGTH];
		SecureRandom random = new SecureRandom();
		random.nextBytes(ivBytes);

		byte[] encryptedBytes;
		try {
			Key key = getKey();

			Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION_CBC);
			cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(ivBytes));

			encryptedBytes = cipher.doFinal(plainData);

		} catch (Exception e) {
			throw new IllegalStateException("Fail to encrypt : " + e.toString(), e);
		}

		return concatBytes(ivBytes, encryptedBytes);
	}

	@Override
	public byte[] decrypt(byte[] encryptedData) {

		byte[] ivBytes = new byte[CBC_IV_LENGTH];
		SecureRandom random = new SecureRandom();
		random.nextBytes(ivBytes);

		byte[] encryptedBytes;

		ivBytes = Arrays.copyOfRange(encryptedData, 0, CBC_IV_LENGTH);
		encryptedBytes = Arrays.copyOfRange(encryptedData, CBC_IV_LENGTH, encryptedData.length);

		try {
			Key key = getKey();

			Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION_CBC);
			cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(ivBytes));

			return cipher.doFinal(encryptedBytes);

		} catch (Exception e) {
			throw new IllegalStateException("Fail to decrypt : " + e.toString(), e);
		}

	}

	public byte[] encryptGcm(byte[] plainData)
	{
		byte[] ivBytes = new byte[CBC_IV_LENGTH];
		SecureRandom random = new SecureRandom();
		random.nextBytes(ivBytes);

		byte[] encryptedBytes;
		try {
			Key key = getKey();
			Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION_GCM);
			try {
				cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(ivBytes));
			} catch (InvalidKeyException e) {
				throw new IllegalStateException("Invalid key for AES128 encrypt. ", e);
			} catch (InvalidAlgorithmParameterException e) {
				throw new IllegalStateException("Invalid Algorithm for AES128 encrypt. ", e);
			}

			encryptedBytes = cipher.doFinal(plainData);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			throw new IllegalStateException("Invalid data for AES128 encrypt. ", e);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			throw new IllegalStateException("Invalid Algorithm for AES128 encrypt. ", e);
		} catch (Exception e) {
			throw new IllegalStateException("Fail to encrypt : " + e.toString(), e);
		}

		return concatBytes(ivBytes, encryptedBytes);
	}

	public byte[] decryptGcm(byte[] encryptedData)
	{
		byte[] ivBytes = new byte[CBC_IV_LENGTH];
		SecureRandom random = new SecureRandom();
		random.nextBytes(ivBytes);

		byte[] encryptedBytes;
		byte[] plainText = null;

		ivBytes = Arrays.copyOfRange(encryptedData, 0, CBC_IV_LENGTH);
		encryptedBytes = Arrays.copyOfRange(encryptedData, CBC_IV_LENGTH, encryptedData.length);

		try {

		} catch (Exception e) {
			throw new IllegalStateException("Fail to decrypt : " + e.toString(), e);
		}

		Cipher cipher;
		try {
			Key key = getKey();
			cipher = Cipher.getInstance(CIPHER_TRANSFORMATION_GCM);
			try {
				cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(ivBytes));
			} catch (InvalidKeyException e) {
				throw new IllegalStateException("Invalid key for AES128 decrypt. ", e);
			} catch (InvalidAlgorithmParameterException e) {
				throw new IllegalStateException("Invalid Algorithm for AES128 decrypt. ", e);
			}
			try {
				plainText = cipher.doFinal(encryptedBytes);
			} catch (IllegalBlockSizeException | BadPaddingException e) {
				throw new IllegalStateException("Invalid data for AES128 decrypt. ", e);
			}
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			throw new IllegalStateException("Invalid Algorithm for AES128 decrypt. ", e);
		} catch (Exception e) {
			throw new IllegalStateException("Fail to decrypt : " + e.toString(), e);
		}

		return plainText;
	}

	private Key getKey() {
		if (auth == null || auth.getCredential() == null) {
			throw new IllegalStateException("Cannot create secret key. Authcode is not ready.");
		}

		if (auth.getCredential().equals(this.cachedCredential)) {
			return this.cachedKey;
		}

		byte[] authCodeBytes = auth.getCredential().getBytes(Charset.defaultCharset());
		if (authCodeBytes.length < BYTE_SIZE) {
			throw new IllegalStateException("Authcode length is too short to use AES" + BIT_SIZE + ".");
		}

		try {
			MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
			byte[] hashValue = md.digest(authCodeBytes);

			byte[] keyMaterial = new byte[BYTE_SIZE];
			System.arraycopy(Base64.encodeBase64(hashValue), 0, keyMaterial, 0, BYTE_SIZE);

			// 보관
			this.cachedCredential = auth.getCredential();
			this.cachedKey = new SecretKeySpec(keyMaterial, KEY_ALGORITHM);

			LOGGER.debug("new secret key created.");
			return this.cachedKey;

		} catch (Exception e) {
			throw new IllegalStateException("Exception occurred when create key", e);
		}
	}

	private byte[] generateRandomIV() {
		// IV 값 랜덤 생성
		byte[] initialVector = new byte[CBC_IV_LENGTH];
		SecureRandom random = new SecureRandom();
		random.nextBytes(initialVector);
		return initialVector;
	}

	private byte[] concatBytes(byte[] a, byte[] b) {
		byte[] result = new byte[a.length + b.length];
		System.arraycopy(a, 0, result, 0, a.length);
		System.arraycopy(b, 0, result, a.length, b.length);
		return result;
	}

}
