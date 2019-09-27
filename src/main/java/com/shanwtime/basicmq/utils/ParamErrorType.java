package com.shanwtime.basicmq.utils;

/**
 * Created by zhouxiaoming on 2015/9/7.
 */
public enum ParamErrorType {


    MISS_REQUIRE(101, "缺少必要的请求参数:"),
    WRONG_FORMAT(102, "请求参数格式错误:"),
    MISS_APP_ID(103, "缺少参数_appId"),
    MUST_MORE_THAN_OR_EQUALS_ZERO(106, "必须是大于或等于0的数");

    private final String name;

    private final int value;

    ParamErrorType(int value, String name) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }
}
