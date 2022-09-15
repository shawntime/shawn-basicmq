package com.shanwtime.basicmq.inject;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.FactoryBean;

@Data
@Builder
public class DynamicConsumerContainerFactory implements FactoryBean<SimpleMessageListenerContainer> {

    private ExchangeType exchangeType;

    private String exchangeName;

    private String queue;
    private String routingKey;

    private Boolean autoDeleted = false;
    private Boolean durable = true;
    private Boolean autoAck = true;

    private ConnectionFactory connectionFactory;

    private RabbitAdmin rabbitAdmin;

    private Integer concurrentNum;

    // 消费方
    private IDynamicConsumer consumer;

    private Exchange buildExchange() {
        if (exchangeType == ExchangeType.DIRECT) {
            return new DirectExchange(exchangeName);
        }
        if (exchangeType == ExchangeType.TOPIC) {
            return new TopicExchange(exchangeName);
        }
        if (exchangeType == ExchangeType.FANOUT) {
            return new FanoutExchange(exchangeName);
        }
        if (StringUtils.isEmpty(routingKey)) {
            throw new IllegalArgumentException("defaultExchange's routingKey should not be null!");
        }
        exchangeType = ExchangeType.DEFAULT;
        return new DirectExchange("");
    }

    private Queue buildQueue() {
        if (StringUtils.isEmpty(queue)) {
            throw new IllegalArgumentException("queue name should not be null!");
        }
        return new Queue(queue, durable == null ? false : durable, false, autoDeleted == null ? true : autoDeleted);
    }


    private Binding bind(Queue queue, Exchange exchange) {
        return ExchangeType.binding(queue, exchange, routingKey);
    }

    @Override
    public SimpleMessageListenerContainer getObject() {
        if (null == rabbitAdmin || null == connectionFactory) {
            throw new IllegalArgumentException("rabbitAdmin and connectionFactory should not be null!");
        }
        Queue queue = buildQueue();
        Exchange exchange = buildExchange();
        Binding binding = bind(queue, exchange);

        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareExchange(exchange);
        rabbitAdmin.declareBinding(binding);

        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setAmqpAdmin(rabbitAdmin);
        container.setConnectionFactory(connectionFactory);
        container.setQueues(queue);
        container.setPrefetchCount(1);
        container.setAutoDeclare(false);
        container.setAcknowledgeMode(autoAck ? AcknowledgeMode.AUTO : AcknowledgeMode.MANUAL);

        if (null != consumer) {
            container.setMessageListener(consumer);
        }
        return container;
    }

    @Override
    public Class<?> getObjectType() {
        return SimpleMessageListenerContainer.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
