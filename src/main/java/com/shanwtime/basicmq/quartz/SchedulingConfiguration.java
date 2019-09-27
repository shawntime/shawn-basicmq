package com.shanwtime.basicmq.quartz;

import java.util.concurrent.ScheduledExecutorService;
import javax.annotation.Resource;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
public class SchedulingConfiguration implements SchedulingConfigurer {

    @Resource
    private ScheduledExecutorService scheduledExecutor;

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.setScheduler(scheduledExecutor);
    }


}
