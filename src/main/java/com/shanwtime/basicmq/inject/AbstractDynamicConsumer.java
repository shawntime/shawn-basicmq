package com.shanwtime.basicmq.inject;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

public abstract class AbstractDynamicConsumer implements IDynamicConsumer {

    private volatile boolean isEnd;

    private SimpleMessageListenerContainer container;

    private boolean isManual;

    @Override
    public void setContainer(SimpleMessageListenerContainer container) {
        this.container = container;
        isManual = container.getAcknowledgeMode().isManual();
    }

    @Override
    public void shutdown() {
        isEnd = true;
    }

    protected void autoAck(Message message, Channel channel, boolean success) throws IOException {
        if (!isManual) {
            return;
        }
        if (success) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } else {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        try {
            autoAck(message, channel, process(message, channel));
        } catch (Exception e) {
            autoAck(message, channel, false);
            throw e;
        } finally {
            if (isEnd) {
                container.stop();
            }
        }
    }

    protected abstract boolean process(Message message, Channel channel);
}
