package com.shanwtime.basicmq.quartz;

import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
public class QuartzTask {

    private IScheduledTask scheduledTask;

    public IScheduledTask getScheduledTask() {
        return scheduledTask;
    }

    public void setScheduledTask(IScheduledTask scheduledTask) {
        this.scheduledTask = scheduledTask;
    }
}
