package com.example.smsmanager.tools;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class EncryptAndDecryptUtils {
    protected final static String KEY = "ScAKC0XhadTHT3Al0QIDAQAB";

    public static String desEncrypt(String value,String key){
        key = key == null ? KEY : key;
        String result = null;

        try {
            if(value != null && !"".equals(value.trim())){
                result = encrypt(value, key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    public static String desDecrypt(String value,String key){
        key = key == null ? KEY : key;
        String result = null;

        try {
            if(value != null && !"".equals(value.trim())){
                result =  decrypt(value, key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @SuppressWarnings("restriction")
    protected static String encrypt(String data,String key) {
        String encryptedData = null;
        try {
            // DES算法要求有一个可信任的随机数源
            SecureRandom sr = new SecureRandom();
            DESKeySpec deskey = new DESKeySpec(key.getBytes());
            // 创建一个密匙工厂，然后用它把DESKeySpec转换成一个SecretKey对象
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(deskey);
            // 加密对象
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, sr);
            // 加密，并把字节数组编码成字符串
            encryptedData = new BASE64Encoder().encode(cipher.doFinal(data.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("加密错误，错误信息：", e);
        }
        return encryptedData;
    }


    @SuppressWarnings("restriction")
    protected static String decrypt(String cryptData,String key) {
        String decryptedData = null;
        try {
            // DES算法要求有一个可信任的随机数源
            SecureRandom sr = new SecureRandom();
            DESKeySpec deskey = new DESKeySpec(key.getBytes());
            // 创建一个密匙工厂，然后用它把DESKeySpec转换成一个SecretKey对象
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(deskey);
            // 解密对象
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, sr);
            // 把字符串解码为字节数组，并解密
            decryptedData = new String(cipher.doFinal(new BASE64Decoder().decodeBuffer(cryptData)));
        } catch (Exception e) {
            throw new RuntimeException("解密错误，错误信息：", e);
        }
        return decryptedData;
    }

}
