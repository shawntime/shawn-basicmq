package com.shanwtime.basicmq.quartz;

public interface IScheduledTask {

    void retryToQueue();

    void retrySendErrorMsg();
}
