package com.shanwtime.basicmq.inject;

import java.util.HashMap;
import java.util.Map;

import com.shanwtime.basicmq.service.listener.ConfirmCallBackListener;
import com.shanwtime.basicmq.service.listener.ReturnCallBackListener;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 *
 * @author shma
 * @date 2018/8/17
 *
 * 消息队列配置
 *
 */
@Configuration("basicRabbitMqConfig")
public class RabbitMqConfig {

    @Bean(name = "basicConnectionFactory")
    public ConnectionFactory basicConnectionFactory(@Value("${spring.rabbitmq.basic.host}") String host,
                                                   @Value("${spring.rabbitmq.basic.port}") int port,
                                                   @Value("${spring.rabbitmq.basic.username}") String username,
                                                   @Value("${spring.rabbitmq.basic.password}") String password,
                                                   @Value("${spring.rabbitmq.basic.vhost}") String vhost) {
        CachingConnectionFactory connectionFactory = getCachingConnectionFactory(host, port, username, password, vhost);
        connectionFactory.setChannelCacheSize(25);
        connectionFactory.setRequestedHeartBeat(10);
        connectionFactory.setPublisherReturns(true);
        connectionFactory.setPublisherConfirms(true);
        return connectionFactory;
    }

    private CachingConnectionFactory getCachingConnectionFactory(String host, int port, String username, String password, String vhost) {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setVirtualHost(vhost);
        return connectionFactory;
    }

    @Bean(name = "basicRabbitAdmin")
    public RabbitAdmin rabbitAdmin(@Qualifier("basicConnectionFactory") ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public AmqpProducer amqpProducer(@Qualifier("basicConnectionFactory") ConnectionFactory connectionFactory,
                                              @Qualifier("confirmCallBackListener") ConfirmCallBackListener confirmCallBackListener,
                                              @Qualifier("returnCallBackListener") ReturnCallBackListener returnCallBackListener) {
        return new AmqpProducer(connectionFactory, confirmCallBackListener, returnCallBackListener);
    }

    public class AmqpProducer {

        private RabbitTemplate rabbitTemplate;

        public AmqpProducer(ConnectionFactory connectionFactory,
                            ConfirmCallBackListener confirmCallBackListener,
                            ReturnCallBackListener returnCallBackListener) {
            rabbitTemplate = new RabbitTemplate(connectionFactory);
            rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
            rabbitTemplate.setConfirmCallback(confirmCallBackListener);
            rabbitTemplate.setReturnCallback(returnCallBackListener);
            rabbitTemplate.setMandatory(true);
            rabbitTemplate.setRetryTemplate(attemptsRetry());
        }

        private RetryTemplate attemptsRetry() {
            RetryTemplate retryTemplate = new RetryTemplate();
            Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
            retryableExceptions.put(Exception.class, true);
            SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(3, retryableExceptions);
            retryTemplate.setRetryPolicy(retryPolicy);
            FixedBackOffPolicy backoffPolicy = new FixedBackOffPolicy();
            backoffPolicy.setBackOffPeriod(5000);
            retryTemplate.setBackOffPolicy(backoffPolicy);
            return retryTemplate;
        }

        /**
         * 将消息发送到指定的交换器上
         */
        public void publishMsg(String exchange, String routingKey, Object msg, CorrelationData correlationData) {
            rabbitTemplate.convertAndSend(exchange, routingKey, msg, correlationData);
        }

        public void publishMsg(String exchange, String routingKey, Object msg,
                               CorrelationData correlationData, MessagePostProcessor messagePostProcessor) {
            rabbitTemplate.convertAndSend(exchange, routingKey, msg, messagePostProcessor, correlationData);
        }
    }
}
