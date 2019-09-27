package com.shanwtime.basicmq.inject;

import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

public interface IDynamicConsumer extends ChannelAwareMessageListener {

    void setContainer(SimpleMessageListenerContainer container);

    default void shutdown() {}
}
