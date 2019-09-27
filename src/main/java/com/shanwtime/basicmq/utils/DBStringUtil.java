package com.shanwtime.basicmq.utils;

import org.springframework.util.StringUtils;

/**
 * Created on 2017/1/4 11:08
 *
 * @author ChenZhiqiang
 */
public final class DBStringUtil {

    private DBStringUtil() {
        // private construct
    }

    private static boolean isLetter(char character) {
        return character / 0x80 == 0;
    }

    private static int length(String str) {
        if (StringUtils.hasText(str)) {
            return str.replaceAll("[^\\x00-\\xff]", "**").length();
        } else {
            return 0;
        }
    }

    public static String subString(String str, int max) {
        if (!StringUtils.hasText(str)) {
            return str;
        }

        if (length(str) <= max) {
            return str;
        }

        char[] chars = str.toCharArray();
        int currentLength = 0;
        int index = 0;
        StringBuilder sb = new StringBuilder();
        while (currentLength <= max) {
            if (index >= chars.length) {
                break;
            }
            if (isLetter(chars[index])) {
                currentLength += 1;
            } else {
                currentLength += 2;
            }
            if (currentLength <= max) {
                sb.append(chars[index]);
            }
            index ++;
        }
        return sb.toString();
    }
}
