package com.shanwtime.basicmq.enums;

/**
 * @author mashaohua
 * @date 2022/9/13 15:33
 */
public enum MessageStorageSourceEnum {

    ORIGINAL(1, "原样数据"),

    MODIFIED(2, "修改数据");

    private int code;
    private String description;

    MessageStorageSourceEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

}
