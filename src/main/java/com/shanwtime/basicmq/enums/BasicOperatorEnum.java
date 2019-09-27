package com.shanwtime.basicmq.enums;


/**
 * @author h p
 */
public enum BasicOperatorEnum {

    PROVIDER(1, "生产者"),

    CONSUMER(2, "消费者");

    private int code;
    private String description;

    BasicOperatorEnum(int code, String description) {
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
