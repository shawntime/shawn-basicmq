package com.shanwtime.basicmq.quartz;

import javax.annotation.Resource;

import com.shanwtime.basicmq.service.IMsgQueueManageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
public class ScheduledTask implements IScheduledTask {

    @Resource
    private IMsgQueueManageService msgQueueManageService;

    @Scheduled(fixedRate = 1000 * 60 * 5)
    @Override
    public void retryToQueue() {
        log.warn("定时任务处理：生产失败重试");
        msgQueueManageService.retry();
    }

    @Scheduled(fixedRate = 1000 * 60 * 10)
    @Override
    public void retrySendErrorMsg() {
        log.warn("定时任务处理：消费失败重试");
        msgQueueManageService.reProvide();
    }

}
