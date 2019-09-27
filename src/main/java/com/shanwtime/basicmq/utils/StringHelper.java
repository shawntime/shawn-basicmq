package com.shanwtime.basicmq.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Created by wangwenchang on 2016/3/2.
 */
public final class StringHelper {

    private static final String REG_EX = "[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";

    private static final Pattern PATTERN = Pattern.compile(REG_EX);

    private StringHelper() {
        // Utility classes should always be final and have a private constructor
    }

    /**
     * 从字符串中提取数字
     */
    public static List<String> extractNumberFromString(String input) {
        if (StringUtils.isBlank(input)) {
            return null;
        }
        List<String> output = new ArrayList<>();
        String regex = "([0-9]\\d*\\.\\d*)|(0\\.\\d*[1-9]\\d*)|(\\d+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            output.add(matcher.group(0));
        }
        return output;
    }

    /**
     * 获取字符串中有效的整型元素放到整型数组中
     */
    public static Integer[] getValidIntegerElements(String elementString) {
        if (StringUtils.isBlank(elementString)) {
            return null;
        }

        String[] elementStringArray = elementString.split(CommonConst.COMMA);
        if (ArrayUtils.isEmpty(elementStringArray)) {
            return null;
        }

        List<Integer> elementIntegerList = new ArrayList<>();
        for (int i = 0; i < elementStringArray.length; i++) {
            if (StringUtils.isNumeric(elementStringArray[i])) {
                elementIntegerList.add(Integer.parseInt(elementStringArray[i]));
            }
        }

        if (CollectionUtils.isEmpty(elementIntegerList)) {
            return null;
        }

        return elementIntegerList.stream().toArray(Integer[]::new);
    }

    public static boolean isSpecialChar(String str) {
        Matcher matcher = PATTERN.matcher(str);
        return matcher.find();
    }
}
