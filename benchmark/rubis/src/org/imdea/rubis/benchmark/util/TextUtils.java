/*
 * RUBiS Benchmark
 * Copyright (C) 2016 IMDEA Software Institute
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or any later
 * version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

package org.imdea.rubis.benchmark.util;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class TextUtils {
    private static final String AVAILABLE_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
                                                + "1234567890 ,;.:-_!\"£$%&/()=?^";

    public static String randomString(int length) {
        return randomString(length, length);
    }

    public static String randomString(int minLength, int maxLength) {
        Random rand = ThreadLocalRandom.current();
        int length = rand.nextInt(maxLength - minLength + 1) + minLength;
        char[] charArray = new char[length];

        for (int i = 0; i < length; i++)
            charArray[i] = AVAILABLE_CHARS.charAt(rand.nextInt(AVAILABLE_CHARS.length()));

        return new String(charArray);
    }
}
