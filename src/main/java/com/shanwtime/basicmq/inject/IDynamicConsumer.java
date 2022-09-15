package com.shanwtime.basicmq.inject;

import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;

public interface IDynamicConsumer extends ChannelAwareMessageListener {

    void setContainer(SimpleMessageListenerContainer container);

    default void shutdown() {}
}
