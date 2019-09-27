package com.shanwtime.basicmq.inject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class CustomizeDynamicConsumerContainer {

    private final Map<String, DynamicConsumer> customizeDynamicConsumerContainer = new ConcurrentHashMap<>();

    public void put(String queueName, DynamicConsumer dynamicConsumer) {
        customizeDynamicConsumerContainer.put(queueName, dynamicConsumer);
    }

    public DynamicConsumer getByQueueName(String queueName) {
        return customizeDynamicConsumerContainer.get(queueName);
    }

    public Map<String, DynamicConsumer> getCustomizeDynamicConsumerContainer() {
        return customizeDynamicConsumerContainer;
    }
}
