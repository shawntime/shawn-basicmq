package com.shanwtime.basicmq.service.impl;

/**
 * @title 仅生产
 * @description
 *
 * @date 2021/1/7 10:28
 */
public abstract class AbstractOnlyProviderMsgQueueService<T> extends AbstractMsgQueueService<T> {

    @Override
    protected final boolean isOpenListener() {
        return false;
    }
}
