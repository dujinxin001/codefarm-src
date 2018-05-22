/*******************************************************************************
 * Copyright (c) 2005, 2014 springside.github.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *******************************************************************************/
package com.codefarm.spring.modules.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 支持HMAC-SHA1消息签名 及 DES/AES对称加密的工具类.
 * 
 * 支持Hex与Base64两种编码方式.
 * 
 * @author calvin 
 */
public class Cryptos
{
    
    private static final String AES = "AES";
    
    private static final String AES_CBC = "AES/CBC/PKCS5Padding";
    
    private static final String HMACSHA1 = "HmacSHA1";
    
    private static final int DEFAULT_HMACSHA1_KEYSIZE = 160; // RFC2401
    
    private static final int DEFAULT_AES_KEYSIZE = 128;
    
    private static final int DEFAULT_IVSIZE = 16;
    
    private static SecureRandom random = new SecureRandom();
    
    // -- HMAC-SHA1 funciton --//
    /**
     * 使用HMAC-SHA1进行消息签名, 返回字节数组,长度为20字节.
     * 
     * @param input 原始输入字符数组
     * @param key HMAC-SHA1密钥
     */
    public static byte[] hmacSha1(byte[] input, byte[] key)
    {
        try
        {
            SecretKey secretKey = new SecretKeySpec(key, HMACSHA1);
            Mac mac = Mac.getInstance(HMACSHA1);
            mac.init(secretKey);
            return mac.doFinal(input);
        }
        catch (GeneralSecurityException e)
        {
            throw Exceptions.unchecked(e);
        }
    }
    
    /**
     * 校验HMAC-SHA1签名是否正确.
     * 
     * @param expected 已存在的签名
     * @param input 原始输入字符串
     * @param key 密钥
     */
    public static boolean isMacValid(byte[] expected, byte[] input, byte[] key)
    {
        byte[] actual = hmacSha1(input, key);
        return Arrays.equals(expected, actual);
    }
    
    /**
     * 生成HMAC-SHA1密钥,返回字节数组,长度为160位(20字节).
     * HMAC-SHA1算法对密钥无特殊要求, RFC2401建议最少长度为160位(20字节).
     */
    public static byte[] generateHmacSha1Key()
    {
        try
        {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(HMACSHA1);
            keyGenerator.init(DEFAULT_HMACSHA1_KEYSIZE);
            SecretKey secretKey = keyGenerator.generateKey();
            return secretKey.getEncoded();
        }
        catch (GeneralSecurityException e)
        {
            throw Exceptions.unchecked(e);
        }
    }
    
    // -- AES funciton --//
    /**
     * 使用AES加密原始字符串.
     * 
     * @param input 原始输入字符数组
     * @param key 符合AES要求的密钥
     */
    public static byte[] aesEncrypt(byte[] input, byte[] key)
    {
        return aes(input, key, Cipher.ENCRYPT_MODE);
    }
    
    /**
     * 使用AES加密原始字符串.
     * 
     * @param input 原始输入字符数组
     * @param key 符合AES要求的密钥
     * @param iv 初始向量
     */
    public static byte[] aesEncrypt(byte[] input, byte[] key, byte[] iv)
    {
        return aes(input, key, iv, Cipher.ENCRYPT_MODE);
    }
    
    /**
     * 使用AES解密字符串, 返回原始字符串.
     * 
     * @param input Hex编码的加密字符串
     * @param key 符合AES要求的密钥
     */
    public static String aesDecrypt(byte[] input, byte[] key)
    {
        byte[] decryptResult = aes(input, key, Cipher.DECRYPT_MODE);
        return new String(decryptResult);
    }
    
    /**
     * 使用AES解密字符串, 返回原始字符串.
     * 
     * @param input Hex编码的加密字符串
     * @param key 符合AES要求的密钥
     * @param iv 初始向量
     */
    public static String aesDecrypt(byte[] input, byte[] key, byte[] iv)
    {
        byte[] decryptResult = aes(input, key, iv, Cipher.DECRYPT_MODE);
        return new String(decryptResult);
    }
    
    /**
     * 使用AES加密或解密无编码的原始字节数组, 返回无编码的字节数组结果.
     *   
     * @param input 原始de字节数组
     * @param key AES要求的密钥
     * @param mode Cipher.ENCRYPT_MODE 或 Cipher.DECRYPT_MODE
     */
    private static byte[] aes(byte[] input, byte[] key, int mode)
    {
        try
        {
            SecretKey secretKey = new SecretKeySpec(key, AES);
            Cipher cipher = Cipher.getInstance(AES);
            cipher.init(mode, secretKey);
            return cipher.doFinal(input);
        }
        catch (GeneralSecurityException e)
        {
            throw Exceptions.unchecked(e);
        }
    }
    
    /**
     * 使用AES加密或解密无编码的原始字节数组, 返回无编码的字节数组结果.
     * 
     * @param input 原始字节数组
     * @param key 符合AES要求的密钥
     * @param iv 初始向量
     * @param mode Cipher.ENCRYPT_MODE 或 Cipher.DECRYPT_MODE
     */
    private static byte[] aes(byte[] input, byte[] key, byte[] iv, int mode)
    {
        try
        {
            SecretKey secretKey = new SecretKeySpec(key, AES);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance(AES_CBC);
            cipher.init(mode, secretKey, ivSpec);
            return cipher.doFinal(input);
        }
        catch (GeneralSecurityException e)
        {
            throw Exceptions.unchecked(e);
        }
    }
    
    /**
     * 生成AES密钥,返回字节数组, 默认长度为128位(16字节).
     */
    public static byte[] generateAesKey()
    {
        return generateAesKey(DEFAULT_AES_KEYSIZE);
    }
    
    /**
     * 生成AES密钥,可选长度为128,192,256位.
     */
    public static byte[] generateAesKey(int keysize)
    {
        try
        {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(AES);
            keyGenerator.init(keysize);
            SecretKey secretKey = keyGenerator.generateKey();
            return secretKey.getEncoded();
        }
        catch (GeneralSecurityException e)
        {
            throw Exceptions.unchecked(e);
        }
    }
    
    /**
     * 生成随机向量,默认大小为cipher.getBlockSize(), 16字节.
     */
    public static byte[] generateIV()
    {
        byte[] bytes = new byte[DEFAULT_IVSIZE];
        random.nextBytes(bytes);
        return bytes;
    }
    
    /**将16进制转换为二进制 
     * @param hexStr 
     * @return 
     */  
    public static byte[] parseHexStr2Byte(String hexStr) {  
            if (hexStr.length() < 1)  
                    return null;  
            byte[] result = new byte[hexStr.length()/2];  
            for (int i = 0;i< hexStr.length()/2; i++) {  
                    int high = Integer.parseInt(hexStr.substring(i*2, i*2+1), 16);  
                    int low = Integer.parseInt(hexStr.substring(i*2+1, i*2+2), 16);  
                    result[i] = (byte) (high * 16 + low);  
            }  
            return result;  
    } 
    
    public static String getMD5(String str) {

		try {
			MessageDigest md = MessageDigest.getInstance("md5");
			byte[] b = md.digest(str.getBytes());
			return new String(b);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;

	}
    
    public static void main(String[] args) throws IOException {
    	String mchKey = "Monitor1111111111111111111111111";
    	String sss="sRhpO4dlwtuYDPJYuOpVXnpGOJyvAssfrP/wP+3COqD8xmmcnG68fg5XC7YTHHOJ6+Sv+c14nuM+MIoyBaTWOpuqBz+1OaTYW0ra3qmjj6yLNA93rcWGsge/oa6fSusBxqmjwbROugsOH4pNSo6bVZAVr3RtuXuVL+T/agv2i4pw2LD2NwSkPIDSX2v2rrXsCrfVNZNZo+IrO7lUkmk9PCnLaadBjKDoJj4ksFUu6pJHgUZQdPlqGA8JeWlwFEu/Y8zhMvJt90PxWRgM4VsdVHydDpiprnh3zPVLozTzb2lZehUTXkkS0D+kNg9RE+6Q8KeGR6zrKrqWR0XX9IdyZ3+3A0a2YrE1SS+Kr+6CiaPUfKLK9UrBe5uU35fEoFHHgn7RR0XNMlIPhcxHm8AdQNkAsdqmrWTApibn1jNcn34zh2wEXcEUy+WbQTZreKi1CHdrr/HiZpaEEdKPY9/HnEnZdVkfpvzvhSfnsGA4AzL+j8BBWq5RaH/7PmzkwnlJjp+z+7vqg1bkY2SkjRLfFqYm/o+3srkbF337/+8sOXhKGs8jd0iMfyvJJzXSPIhjW3p85tzNtFEtZ/6PctXndGi+zAZvyKYDnub3SuB7afKrnaSaXiFnmS7r0qiIVxU65zW4axDJ6PFfZr8T0PnpzMrAczh3JMs1oWxjpZ5TSOlczr29tx7WlvcNnUc91q5r8Cvc52dn2/lW5ReTA9UA/s9Rxr0TBHkNuHtioguIglaShHG7617Ci/arYw9WAM/SXOESiKbo8Dye0oaJE3z4KX6WoD26nojoeOm7GmY2eUaP4QfuskkyxPNSzm99Wec1Q4uKUnoYgHrsWevUvRlj6OQavOUtFwo+/39ipbtewCN/I8P6tsOjbQjp7/WghtChBPJr8BH4KIymGIugWb4AA7BayUgCs3yjjJgAx0vjYwg+hPNGQ9GB70hJrrEH7kaYXb2Z0O0U5a8shWW9kfXFo8RjT53ctn7UrptQbKwjVhqUePBVFMtCGfqXrC8I2k5u7n+/PELQ/ms8uIZaw/xaPRNS5e5Pf7zEVKvmyvDcKeM=";
    	byte[] deBase64 = Encodes.decodeBase64(sss);
    	System.out.println(new String(deBase64));
    	ByteArrayInputStream input = new ByteArrayInputStream(
    			 mchKey.getBytes("UTF-8"));
         byte[] md5 = Digests.md5(input);
         String dsfdsf = Encodes.encodeHex(md5).toUpperCase();
    	System.out.println(dsfdsf);
    	//dsfdsf=Encodes.encodeBase64(md5);
    	String dd = Cryptos.aesDecrypt(deBase64, dsfdsf.getBytes("UTF-8"));
    	
    	
    	
	}
}