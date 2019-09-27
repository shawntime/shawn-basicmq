package com.shanwtime.basicmq.config;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


/**
 * @author h p
 */
@Configuration("rabbitTaskExecutorConfiguration")
public class TaskExecutorConfiguration {

    @Bean(name = "rabbitProductExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(1);
        threadPoolTaskExecutor.setMaxPoolSize(2);
        threadPoolTaskExecutor.setQueueCapacity(1000);
        threadPoolTaskExecutor.setKeepAliveSeconds(300);
        threadPoolTaskExecutor.setRejectedExecutionHandler(callerRunsPolicy());
        return threadPoolTaskExecutor;
    }

    @Bean(name = "scheduledExecutor")
    public ScheduledExecutorService scheduledExecutor() {
        return Executors.newScheduledThreadPool(2);
    }

    @Bean
    public ThreadPoolExecutor.CallerRunsPolicy callerRunsPolicy() {
        return new ThreadPoolExecutor.CallerRunsPolicy();
    }
}
