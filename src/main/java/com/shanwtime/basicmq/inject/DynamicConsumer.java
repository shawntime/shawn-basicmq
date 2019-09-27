package com.shanwtime.basicmq.inject;

import com.shanwtime.basicmq.service.impl.AbstractMsgQueueService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

public class DynamicConsumer {

    private static final Logger logger = LoggerFactory.getLogger(DynamicConsumer.class);
    private SimpleMessageListenerContainer container;
    
    private MessageConverter msgConverter = new Jackson2JsonMessageConverter();

    public DynamicConsumer(DynamicConsumerContainerFactory fac,
                           final AbstractMsgQueueService msgQueueService) throws Exception {
        SimpleMessageListenerContainer container = fac.getObject();
        AbstractDynamicConsumer consumer = new AbstractDynamicConsumer() {
            @Override
            protected boolean process(Message message, Channel channel) {
                logger.info("DynamicConsumer -> queue:{}, msg:{}", fac.getQueue(), new String(message.getBody()));
                distributionConsumerMsg(message, channel, msgQueueService);
                return true;
            }
        };
        consumer.setContainer(container);
        container.setMessageListener(consumer);
        this.container = container;
    }

    //启动消费者监听
    public void start() {
        container.start();
    }

    //消费者停止监听
    public void stop() {
        container.stop();
    }

    //消费者重启
    public void shutdown() {
        container.shutdown();
    }

    /**
     * 用户扩展处理消息
     */
    public void distributionConsumerMsg(Message message,
                                        Channel channel,
                                        AbstractMsgQueueService msgQueueService) {
        Object msg = msgConverter.fromMessage(message);
        MessageProperties messageProperties = message.getMessageProperties();
        msgQueueService.consume(msg.toString(), messageProperties);
    }
}
