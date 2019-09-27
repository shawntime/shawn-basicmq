package com.shanwtime.basicmq.entity;

import com.shanwtime.basicmq.enums.BasicOperatorEnum;

/**
 *
 * @author shma
 * @date 2018/11/28
 */
public class MsgQueueBody {

    private BasicOperatorEnum basicOperatorEnum;

    private String msgQueueBody;

    public MsgQueueBody(BasicOperatorEnum basicOperatorEnum, String msgQueueBody) {
        this.basicOperatorEnum = basicOperatorEnum;
        this.msgQueueBody = msgQueueBody;
    }

    public BasicOperatorEnum getBasicOperatorEnum() {
        return basicOperatorEnum;
    }

    public void setBasicOperatorEnum(BasicOperatorEnum basicOperatorEnum) {
        this.basicOperatorEnum = basicOperatorEnum;
    }

    public String getMsgQueueBody() {
        return msgQueueBody;
    }

    public void setMsgQueueBody(String msgQueueBody) {
        this.msgQueueBody = msgQueueBody;
    }
}
