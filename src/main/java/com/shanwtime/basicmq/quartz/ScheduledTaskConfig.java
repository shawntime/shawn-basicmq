package com.shanwtime.basicmq.quartz;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "openScheduledTask", havingValue = "false")
public class ScheduledTaskConfig {

    @Bean
    public QuartzTask quartzTask() {
        QuartzTask quartzTask = new QuartzTask();
        quartzTask.setScheduledTask(scheduledTask());
        return quartzTask;
    }

    @Bean
    public IScheduledTask scheduledTask() {
        return new ScheduledTask();
    }
}
