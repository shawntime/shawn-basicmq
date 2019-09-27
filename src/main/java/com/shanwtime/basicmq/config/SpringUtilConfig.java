package com.shanwtime.basicmq.config;

import com.shanwtime.basicmq.utils.SpringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringUtilConfig {

    @Bean
    public SpringUtils getSpringUtils() {
        return new SpringUtils();
    }
}
