package com.shanwtime.basicmq.service;

import java.util.Map;

import org.springframework.amqp.core.MessageProperties;

/**
 * @author h p
 */
public interface IMsgQueueService {

    void provide(String msgBodyJson);

    void provide(String msgBodyJson, boolean isAsync);

    void provide(String msgBodyJson, int originalId);

    void provide(String msgBodyJson, Map<String, Object> headMap);

    void provide(String msgBodyJson, boolean isAsync, Map<String, Object> headMap);

    void provide(String msgBodyJson, boolean isAsync, Map<String, Object> headMap, int originalId);

    void consume(String msgBodyJson);

    void consume(String msgBodyJson, int originalId);

    void consume(String msgBodyJson, MessageProperties messageProperties);

    void consume(String msgBodyJson, int originalId, MessageProperties messageProperties);

    void stopConsumerListener();

    void shutdownConsumerListener();

    void startConsumerListener();
}
