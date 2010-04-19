package org.parandroid.encryption;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import org.parandroid.encoding.Base64Coder;
import org.parandroid.sms.R;

import android.content.Context;
import android.util.Log;

/**
 * Wrapper class to encrypt and decrypt messages, using the underlying factory
 */
public abstract class MessageEncryption {
	
	private static final String TAG = "PD MEncr";
	
	/**
	 * Encrypt a message using AES with a secrey key, generated by the Diffie-Hillman algorithm
	 * with our private key and the receiver's public key. The keys need to be stored locally.
	 * 
	 * @param context
	 * @param number
	 * @param text
	 * @return encrypted message
	 * @throws GeneralSecurityException
	 * @throws IOException - Key(s) missing
	 */
	public static byte[] encrypt(Context context, String number, String text) throws Exception {
		PrivateKey privateKey = MessageEncryptionFactory.getPrivateKey(context);
		PublicKey publicKey  = MessageEncryptionFactory.getPublicKey(context, number);
		SecretKey secretKey = MessageEncryptionFactory.generateSecretKey(privateKey, publicKey);
		
		Cipher cipher = Cipher.getInstance(MessageEncryptionFactory.ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] cipherText = cipher.doFinal(text.getBytes());
		
		return cipherText;
	}
	
	public static byte[] encrypt(Context context, byte[] keyBytes, String text) throws Exception {
		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFact = KeyFactory.getInstance(MessageEncryptionFactory.KEY_EXCHANGE_PROTOCOL);
        PublicKey publicKey = keyFact.generatePublic(x509KeySpec);
		
		PrivateKey privateKey = MessageEncryptionFactory.getPrivateKey(context);
		SecretKey secretKey = MessageEncryptionFactory.generateSecretKey(privateKey, publicKey);
		
		Cipher cipher = Cipher.getInstance(MessageEncryptionFactory.ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] cipherText = cipher.doFinal(text.getBytes());
		
		return cipherText;
	}
	
	/**
	 * Decrypt a message using AES with a secrey key, generated by the Diffie-Hillman algorithm
	 * with our private key and the receiver's public key. The keys need to be stored locally.
	 * 
	 * @param context
	 * @param number
	 * @param cipherText
	 * @return decrypted message
	 * @throws GeneralSecurityException
	 * @throws IOException - Key(s) missing
	 */
	public static String decrypt(Context context, String number, byte[] cipherText) throws Exception {
		PrivateKey privateKey = MessageEncryptionFactory.getPrivateKey(context);
		return decrypt(context, privateKey, number, cipherText);
	}
	
	public static String decrypt(Context context, PrivateKey privateKey, String number, byte[] cipherText) throws Exception {	
		PublicKey publicKey  = MessageEncryptionFactory.getPublicKey(context, number);
		
		if(privateKey == null || publicKey == null){
			return context.getString(R.string.parandroid_snippet);
		}
		
		SecretKey secretKey = MessageEncryptionFactory.generateSecretKey(privateKey, publicKey);
		Cipher cipher = Cipher.getInstance(MessageEncryptionFactory.ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
                
        byte[] text = cipher.doFinal(cipherText);

		return new String(text);
	}
}
