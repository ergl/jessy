package org.imdea.benchmark.rubis.util;

import java.util.Random;

public class TextUtils {
    private static final String AVAILABLE_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static Random rand = new Random();

    public static String randomString(int length) {
        return randomString(length, length);
    }

    public static String randomString(int minLength, int maxLength) {
        int length = rand.nextInt(maxLength - minLength + 1) + minLength;
        char[] charArray = new char[length];

        for (int i = 0; i < length; i++)
            charArray[i] = AVAILABLE_CHARS.charAt(rand.nextInt(AVAILABLE_CHARS.length()));

        return new String(charArray);
    }
}
