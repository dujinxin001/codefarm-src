package com.codefarm.spring.modules.util;

import java.io.UnsupportedEncodingException;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * MD5Util
 */
public final class MD5Util {

    /**
     * 缓冲区大小
     */
    private static final int INIT_SIZE = 20;

    private static final int STRINGPLANNINGLENGTH = 16;

    private static final int BYTEMINLENGTH = 256;


    private static BigInteger private_d = new BigInteger(
            "3206586642942415709865087389521403230384599658161226562177807849299468150139");
    private static BigInteger n = new BigInteger(
            "7318321375709168120463791861978437703461807315898125152257493378072925281977");

    /**
     * 定义数组，存放16进制字符
     */
    private static String[] hexDigits = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e",
            "f" };

    /**
     * constructor
     */
    private MD5Util() {

    }

    /**
     * 将字符串进行MD5编码
     * 
     * @param srcStr
     *            源字符串
     * @return 编码后的字符串
     */
    public static String digest(String srcStr) {
        if (null == srcStr) {
            return null;
        }

        byte sa[] = srcStr.getBytes();
        String destStr = digest(sa);

        return destStr;
    }

    /**
     * 将字符串进行MD5编码
     * 
     * @param srcStr
     *            源字符串
     * @return 编码后的字符串
     */
    public static String digest(String srcStr, String encoding) {
        if (null == srcStr) {
            return null;
        }

        String destStr = null;
        byte sa[];
        try {
            sa = srcStr.getBytes(encoding);
            destStr = digest(sa);
        } catch (UnsupportedEncodingException e) {
        }

        return destStr;
    }

    public static String digest(byte sa[]) {
        if (null == sa) {
            return null;
        }

        String destStr = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            md.update(sa);

            byte theDigest[] = md.digest();
            destStr = byteArrayToHexString(theDigest);
        } catch (NoSuchAlgorithmException ex) {
            throw new UnsupportedOperationException(ex);
        }

        return destStr;
    }

    /**
     * 将byte数组转换成16进制字符串
     * 
     * @param b
     *            byte 待转化的数组
     * @return String 转化完成生成的字符串
     */
    private static String byteArrayToHexString(byte[] b) {
        if (b == null || b.length == 0) {
            return null;
        }

        StringBuffer result = new StringBuffer(INIT_SIZE);

        for (int i = 0; i < b.length; i++) {
            result.append(byteToHexString(b[i]));
        }

        return result.toString();
    }

    /**
     * 将byte字节转化成16进制字符串
     * 
     * @param b
     *            byte 待转化的字节
     * @return String 转化完成生成的字符串
     */
    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0) {
            n = BYTEMINLENGTH + n;
        }
        int d1 = n / STRINGPLANNINGLENGTH;
        int d2 = n % STRINGPLANNINGLENGTH;
        return hexDigits[d1] + hexDigits[d2];
    }

    public static String hmacSign(String aValue, String aKey) {
        byte k_ipad[] = new byte[64];
        byte k_opad[] = new byte[64];
        byte keyb[];
        byte value[];
        try {
            keyb = aKey.getBytes("UTF-8");
            value = aValue.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            keyb = aKey.getBytes();
            value = aValue.getBytes();
        }
        Arrays.fill(k_ipad, keyb.length, 64, (byte) 54);
        Arrays.fill(k_opad, keyb.length, 64, (byte) 92);
        for (int i = 0; i < keyb.length; i++) {
            k_ipad[i] = (byte) (keyb[i] ^ 0x36);
            k_opad[i] = (byte) (keyb[i] ^ 0x5c);
        }
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        md.update(k_ipad);
        md.update(value);
        byte dg[] = md.digest();
        md.reset();
        md.update(k_opad);
        md.update(dg, 0, 16);
        dg = md.digest();
        return byteArrayToHexString(dg);
    }


    /**
     * Returns a MessageDigest for the given <code>algorithm</code>.
     *
     *            The MessageDigest algorithm name.
     * @return An MD5 digest instance.
     * @throws RuntimeException
     *             when a {@link java.security.NoSuchAlgorithmException} is
     *             caught
     */

    static MessageDigest getDigest() {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Calculates the MD5 digest and returns the value as a 16 element
     * <code>byte[]</code>.
     *
     * @param data
     *            Data to digest
     * @return MD5 digest
     */
    public static byte[] md5(byte[] data) {
        return getDigest().digest(data);
    }

    /**
     * Calculates the MD5 digest and returns the value as a 16 element
     * <code>byte[]</code>.
     *
     * @param data
     *            Data to digest
     * @return MD5 digest
     */
    public static byte[] md5(String data) {
        return md5(data.getBytes());
    }

    /**
     * Calculates the MD5 digest and returns the value as a 32 character hex
     * string.
     *
     * @param data
     *            Data to digest
     * @return MD5 digest as a hex string
     */
    public static String md5Hex(byte[] data) {
        return HexUtil.toHexString(md5(data));
    }

    /**
     * Calculates the MD5 digest and returns the value as a 32 character hex
     * string.
     *
     * @param data
     *            Data to digest
     * @return MD5 digest as a hex string
     */
    public static String md5Hex(String data) {
        return HexUtil.toHexString(md5(data));
    }

    public static void main(String args[]){
        String oldpwd = "of111111";//0a7b1752d64afbe90a3cdcc3458aeb6c8e479e9f52ed84fc24686160f43d35a8
        String tempflag = "0";

        System.out.print(MD5Util.md5Hex("HNHYJDCSSHA0010PEKAZ)suMyA"));
        if (!(tempflag.equals("1"))) {
            oldpwd = MD5Util.md5Hex(oldpwd);
        } else {
            oldpwd = MD5Util.md5Hex("A354957" + MD5Util.md5Hex(oldpwd)
                    + "da076095a5c7c54e9c9873076a50b531ff5111c7193e5e7021c6181469308552");
        }
        System.out.println("oldpwd------------" + oldpwd);
        String str=getDecryptLoginPassword("04158ce7360c8611fded5fe0281fdd3de0e534c1d2615242b04592e082a05ea8");
        System.out.println("str------------"+str);
    }

    public static String getDecryptLoginPassword(String str) {
        byte ptext[] = HexUtil.toByteArray(str);
        BigInteger encry_c = new BigInteger(ptext);

        BigInteger private_m = encry_c.modPow(private_d, n);
        // 计算明文对应的字符串
        byte[] mt = private_m.toByteArray();
        StringBuffer buffer = new StringBuffer();
        for (int i = mt.length - 1; i > -1; i--) {
            buffer.append((char) mt[i]);
        }

        return buffer.substring(0, buffer.length() - 10).toString();
    }
}
