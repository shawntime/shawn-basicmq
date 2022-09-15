package com.shanwtime.basicmq.service.listener;

import javax.annotation.Resource;

import com.shanwtime.basicmq.redis.RedisClient;
import com.shanwtime.basicmq.service.impl.Constant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.stereotype.Component;

@Component("confirmCallBackListener")
public class ConfirmCallBackListener implements RabbitTemplate.ConfirmCallback {

    @Resource
    private RedisClient redisClient;

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        String id = correlationData.getId();
        if (StringUtils.isEmpty(id)) {
            return;
        }
        if (ack) {
            redisClient.hdel(Constant.queue_key, id);
        }
    }

}