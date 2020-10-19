package com.auth.get.away.business.config;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;


public class Encrypt {
	/**
	 * 用户密码加密算法
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static String encryptRMS(String data) throws Exception {
		String key = "tkGGRmBErvc=";
		String iv = "Kl7ZgtM1dvQ=";
		byte[] keybyte = Base64.decodeBase64(key);
		byte[] ivbyte = Base64.decodeBase64(iv);
		byte[] desstring = Encrypt.encrypt(data.getBytes(), keybyte, ivbyte);
		String rrr = Base64.encodeBase64String(desstring);
		return rrr;

	}
	/**
	 * Description DES CBC模式加密
	 * @param data
	 * @param key 加密键byte数组
	 * @return
	 * @throws Exception
	 */
  
	public static byte[] encrypt(byte[] data, byte[] key,byte[] iv) throws Exception {
		// 生成一个可信任的随机数源
		// 从原始密钥数据创建DESKeySpec对象
		DESKeySpec dks = new DESKeySpec(key);
		// 创建一个密钥工厂，然后用它把DESKeySpec转换成SecretKey对象
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey securekey = keyFactory.generateSecret(dks);
		// Cipher对象实际完成加密操作
		Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
		// 用密钥初始化Cipher对象
		IvParameterSpec iv2 = new IvParameterSpec(iv);
		cipher.init(Cipher.ENCRYPT_MODE, securekey, iv2);//IV的方式
 		return cipher.doFinal(data);

	}
}
