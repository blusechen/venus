package com.meidusa.venus.io.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class StringUtil {

    private final static char[] c = { '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', 'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', 'a', 's', 'd', 'f',
            'g', 'h', 'j', 'k', 'l', 'z', 'x', 'c', 'v', 'b', 'n', 'm', 'Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P', 'A', 'S', 'D', 'F', 'G', 'H', 'J',
            'K', 'L', 'Z', 'X', 'C', 'V', 'B', 'N', 'M' };
    private final static int CHART_LENGHT = c.length;
    private final static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    public static String getRandomString(int size) {
        Random random = new Random();
        StringBuffer sb = new StringBuffer(size);
        for (int i = 0; i < size; i++) {
            sb.append(c[Math.abs(random.nextInt(CHART_LENGHT))]);
        }
        return sb.toString();
    }

    public static String md5(String source) throws NoSuchAlgorithmException {
        java.security.MessageDigest md;
        md = java.security.MessageDigest.getInstance("MD5");

        md.update(source.getBytes());
        byte tmp[] = md.digest();
        char str[] = new char[16 * 2];
        int k = 0;
        for (int i = 0; i < 16; i++) {
            byte byte0 = tmp[i];
            str[k++] = hexDigits[byte0 >>> 4 & 0xf];
            str[k++] = hexDigits[byte0 & 0xf];
        }
        String retStr = new String(str);
        return retStr;
    }

    public static byte[] scramble411(String password, String seed) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1"); //$NON-NLS-1$

        byte[] passwordHashStage1 = md.digest(password.getBytes());
        md.reset();

        byte[] passwordHashStage2 = md.digest(passwordHashStage1);
        md.reset();

        byte[] seedAsBytes = seed.getBytes(); // for debugging
        md.update(seedAsBytes);
        md.update(passwordHashStage2);

        byte[] toBeXord = md.digest();

        int numToXor = toBeXord.length;

        for (int i = 0; i < numToXor; i++) {
            toBeXord[i] = (byte) (toBeXord[i] ^ passwordHashStage1[i]);
        }

        return toBeXord;
    }

    public static void main(String[] args) {
        Random random = new Random();
        System.out.println(random.nextInt(2));
    }
}
