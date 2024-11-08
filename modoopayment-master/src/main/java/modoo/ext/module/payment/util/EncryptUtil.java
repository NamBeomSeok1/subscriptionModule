package modoo.ext.module.payment.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/*import ezwelcrypto.EzwelCrypto;*/

@Component
public class EncryptUtil {


	private static final Logger LOGGER = LoggerFactory.getLogger(EncryptUtil.class);
	
	@Value("${payment.ezwel.publickey}")
	private String ezwelPublicKey;	
	
	
	public Cipher getCipher(String iv, String key) {		
		Cipher cipher = null;
		try {
			SecretKey skey = new SecretKeySpec(key.getBytes(), "AES");		
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");		
			cipher.init(Cipher.ENCRYPT_MODE, skey, new IvParameterSpec(iv.getBytes()));
		}catch(Exception e) {
			LOGGER.error("EncryptUtil - getCipher ERROR : {}", e);
		}
		return cipher;
	}
	
	public String getHash(String type, String src) {
		
		if(src == null || src.isEmpty()) {
			return null;
		}


		
//		String hashval = null;
//		
//		try {
//			byte[] bytes = src.getBytes(StandardCharsets.UTF_8);
//			
//			String utf8EncinputVal = new String(bytes, StandardCharsets.UTF_8);	
//			MessageDigest md = MessageDigest.getInstance(type);
//			
//			byte[] messageDigest = md.digest(utf8EncinputVal.getBytes());			
//			BigInteger no = new BigInteger(1, messageDigest);
//			hashval = no.toString(16);
//			
//			while(hashval.length()<32){
//				hashval = "0" + hashval;
//			}
//		}catch(Exception e) {
//			LOGGER.error("EncryptUtil - getHash ERROR : {}", e);
//		}
		
		String hashval = null;
		try {
		    MessageDigest digest = MessageDigest.getInstance("SHA-512");
		    digest.reset();
		    digest.update(src.getBytes("utf8"));
		    hashval = String.format("%0128x", new BigInteger(1, digest.digest()));
		} catch (Exception e) {
		    e.printStackTrace();
		}
		
		return hashval;
	}
	
	
	/*public String encodeParamForEzwel(String strEncode) {
		String resultStr = null;
		try {
			EzwelCrypto ezwelCrypto = new EzwelCrypto();
			Encoder encoder = Base64.getEncoder();

			if(strEncode == null || strEncode.isEmpty()){
				strEncode  = "0";
			}

			resultStr = new String(encoder.encode(ezwelCrypto.encrypt(strEncode, ezwelPublicKey.getBytes(), "UTF-8")));
		}catch(Exception e) {
			LOGGER.error("EncryptUtil - encodeEzwel ERROR : {}", e);
		}
		return resultStr;
	}*/
	
	
	/*public String decodeResultForEzwel(String strDecode) {
		String resultStr = null;
		try {
			EzwelCrypto ezwelCrypto = new EzwelCrypto();
			Decoder decoder = Base64.getDecoder();

			if(strDecode != null && !strDecode.isEmpty()){
				byte[] encryptbytes = decoder.decode(strDecode);
				resultStr = ezwelCrypto.decryptAsString(encryptbytes, ezwelPublicKey.getBytes(), "UTF-8");
			}
		}catch(Exception e) {			
			LOGGER.error("EncryptUtil - decodeEzwel ERROR : {}", e);
			return null;
		}
		return resultStr;
	}*/
		
	
	public String encodeParamBase64(String strEncode) {
		String resultStr = null;
		try {
			Encoder encoder = Base64.getEncoder();
			
			if(!strEncode.equals("") && strEncode != null){
				resultStr = new String(encoder.encode(strEncode.getBytes()));
				LOGGER.info("resultStr : {}, {} ", strEncode , resultStr);
			}
		}catch(Exception e) {
			LOGGER.error("EncryptUtil - encodeEzwel ERROR : {}", e);
		}
		return resultStr;
	}
}
