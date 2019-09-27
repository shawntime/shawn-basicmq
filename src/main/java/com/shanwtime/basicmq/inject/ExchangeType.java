package com.shanwtime.basicmq.inject;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;

public enum ExchangeType {

    DIRECT, TOPIC, FANOUT, DEFAULT;

    public static Binding binding(Queue queue, Exchange exchange, String routingKey) {
        return BindingBuilder.bind(queue).to(exchange).with(queue.getName()).noargs();
    }
}
