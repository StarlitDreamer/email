package com.java.utils;

import org.apache.logging.log4j.util.Strings;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Util {
    private static final LogUtil logutil= LogUtil.getLogger(Md5Util.class);
    /**
     * 默认的密码字符串组合，用来将字节转换成 16 进制表示的字符,apache校验下载的文件的正确性用的就是默认的这个组合
     */
    protected static char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    protected static MessageDigest messagedigest = null;

    static {
        try {
            messagedigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException nsaex) {
            System.err.println(Md5Util.class.getName() + "初始化失败，MessageDigest不支持MD5Util。");
            nsaex.printStackTrace();
        }
    }

    /**
     * 生成字符串的md5校验值
     *
     * @param s
     * @return
     */
    public static String getMD5String(String s) {
        if (s == null){
            return null;
        }
        return getMD5String(s.getBytes());
    }

    /**
     * 判断字符串的md5校验码是否与一个已知的md5码相匹配
     *
     * @param password  要校验的字符串
     * @param md5PwdStr 已知的md5校验码
     * @return
     */
    public static boolean checkPassword(String password, String md5PwdStr) {
        if (Strings.isEmpty(password) ||  Strings.isEmpty(md5PwdStr)){
            return false;
        }
        String s = getMD5String(password);
        if (s == null){
            return false;
        }
        return s.equals(md5PwdStr);
    }


    public static String getMD5String(byte[] bytes) {
        if (bytes == null){
            return null;
        }
        messagedigest.update(bytes);
        return bufferToHex(messagedigest.digest());
    }

    private static String bufferToHex(byte bytes[]) {
        if (bytes == null){
            return null;
        }
        return bufferToHex(bytes, 0, bytes.length);
    }

    private static String bufferToHex(byte bytes[], int m, int n) {
        try {
            StringBuffer stringbuffer = new StringBuffer(2 * n);
            int k = m + n;
            for (int l = m; l < k; l++) {
                appendHexPair(bytes[l], stringbuffer);
            }
            return stringbuffer.toString();
        }catch (Exception e){
            logutil.error("Md5Util bufferToHex error : " + e);
            return null;
        }

    }

    private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
        try {
            char c0 = hexDigits[(bt & 0xf0) >> 4];// 取字节中高 4 位的数字转换, >> 4;
            // 为逻辑右移，将符号位一起右移,此处未发现两种符号有何不同
            char c1 = hexDigits[bt & 0xf];// 取字节中低 4 位的数字转换
            stringbuffer.append(c0);
            stringbuffer.append(c1);
        }catch (Exception e){
            logutil.error("Md5Util appendHexPair error : "  + e);
        }

    }

}

