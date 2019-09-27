package com.shanwtime.basicmq.utils;

import java.util.Collection;

import com.google.common.collect.ImmutableList;


/**
 * Created by zhouxiaoming on 2015/8/28.
 * 封包协议
 */
public class Protocol {

    private int returncode;

    private String message;

    private Object result;

    public static <T> Protocol oldProtocol(T object) {
        Protocol protocol = new Protocol();
        protocol.setResult(object);
        return protocol;
    }

    public static Protocol autoRecommendProtocol(Collection list, int pageSize, int pageIndex, int rowCount) {
        Protocol protocol = new Protocol();
        protocol.setResult(new AutoRecommendProtocolBody(list, pageSize, pageIndex, rowCount));
        return protocol;
    }

    public Protocol() {
        //空构造器
    }

    public Protocol(Collection list, int pageSize, int pageIndex, int rowCount) {
        result = new ProtocolBody(list, pageSize, pageIndex, rowCount);
    }

    public <T> Protocol(T object) {
        Collection list = null;
        if (object != null) {
            if (object instanceof Collection) {
                list = (Collection) object;
            } else {
                list = ImmutableList.of(object);
            }
        }
        int size = (list == null) ? 0 : list.size();
        result = new ProtocolBody(list, size, 1, size);
    }

    public Protocol(Boolean result, String message) {
        this.returncode = result ? 1 : 0;
        this.message = message;
    }

    public Protocol(int returncode, String message) {
        this.returncode = returncode;
        this.message = message;
    }

    public Protocol(int returnCode, String message, Object result) {
        this.returncode = returnCode;
        this.message = message;
        this.result = result;
    }

    public int getReturncode() {
        return returncode;
    }

    public void setReturncode(int returncode) {
        this.returncode = returncode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}