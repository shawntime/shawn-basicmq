package com.shanwtime.basicmq.service.listener;

import java.nio.charset.Charset;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ReturnCallback;
import org.springframework.stereotype.Component;

@Component("returnCallBackListener")
public class ReturnCallBackListener implements ReturnCallback {

    private static final Logger logger = LoggerFactory.getLogger(ReturnCallBackListener.class);

    @Override
    public void returnedMessage(Message message, int replyCode,
                                String replyText, String exchange, String routingKey) {
        String body = new String(message.getBody(), Charset.defaultCharset());
        MessageProperties messageProperties = message.getMessageProperties();
        Map<String, Object> headers = messageProperties.getHeaders();
        logger.error("returnCallBack -> replyCode:{}, replyText:{}, exchange:{}, routingKey:{}, body:{}, head:{}",
                replyCode, replyText, exchange, routingKey, body, headers);
    }
}
