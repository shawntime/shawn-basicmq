package com.shanwtime.basicmq.service.impl;

import java.lang.reflect.ParameterizedType;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import com.shanwtime.basicmq.entity.MessageQueueErrorRecord;
import com.shanwtime.basicmq.entity.MsgQueueBody;
import com.shanwtime.basicmq.enums.BasicOperatorEnum;
import com.shanwtime.basicmq.inject.CustomizeDynamicConsumerContainer;
import com.shanwtime.basicmq.inject.DynamicConsumer;
import com.shanwtime.basicmq.inject.DynamicConsumerContainerFactory;
import com.shanwtime.basicmq.inject.ExchangeType;
import com.shanwtime.basicmq.inject.RabbitMqConfig;
import com.shanwtime.basicmq.redis.RedisClient;
import com.shanwtime.basicmq.service.IMsgQueueManageService;
import com.shanwtime.basicmq.service.IMsgQueueService;
import com.shanwtime.basicmq.utils.DBStringUtil;
import com.shanwtime.basicmq.utils.JsonHelper;
import com.shanwtime.basicmq.utils.MapExtensions;
import com.shanwtime.basicmq.utils.RedisLockUtil;
import com.shanwtime.basicmq.utils.SpringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

/**
 *
 * @author shma
 * @date 2018/11/28
 */
public abstract class AbstractMsgQueueService<T> implements IMsgQueueService {

    @Resource
    private IMsgQueueManageService msgQueueManageService;

    @Resource(name = "rabbitProductExecutor")
    private ThreadPoolTaskExecutor rabbitProductExecutor;

    @Resource
    private RedisClient redisClient;

    @Resource
    private RabbitMqConfig.AmqpProducer amqpProducer;

    @Resource
    private CustomizeDynamicConsumerContainer customizeDynamicConsumerContainer;

    @Value("${spring.rabbitmq.basic.isOpenListener:false}")
    private boolean isOpenListener;

    @Value("${spring.rabbitmq.basic.appId:0}")
    private int appId;

    private static final Logger logger = LoggerFactory.getLogger(AbstractMsgQueueService.class);

    @Override
    public void provide(String msgBodyJson) {
        provide(msgBodyJson, false);
    }

    @Override
    public void provide(String msgBodyJson, boolean isAsync) {
        provide(msgBodyJson, isAsync, null);
    }

    @Override
    public void provide(String msgBodyJson, int originalId) {
        provide(msgBodyJson, false, null, originalId);
    }

    @Override
    public void provide(String msgBodyJson, Map headMap) {
        provide(msgBodyJson, false, headMap);
    }

    @Override
    public void provide(String msgBodyJson, boolean isAsync, Map headMap) {
        provide(msgBodyJson, isAsync, headMap, 0);
    }

    @Override
    public void provide(String msgBodyJson, boolean isAsync, Map<String, Object> headMap, int originalId) {
        try {
            logger.info("provide -> {}", msgBodyJson);
            String correlationDataId = "";
            // 必须自动注册的才允许接收回调
            if (isAutoRegistry() && isConfirmCallBack()) {
                MessageData data = getMessageData(msgBodyJson, originalId);
                redisClient.hset(Constant.queue_key, data.getId(), JsonHelper.serialize(data), -1);
                correlationDataId = data.getId();
            }
            provideMessage(msgBodyJson, correlationDataId, headMap);
        } catch (Throwable e) {
            exceptionHandle(new MsgQueueBody(BasicOperatorEnum.PROVIDER, msgBodyJson), e, isAsync, originalId);
        }
    }

    @Override
    public void consume(String msgBodyJson) {
        consume(msgBodyJson, 0);
    }

    @Override
    public void consume(String msgBodyJson, int originalId) {
        consume(msgBodyJson, originalId, null);
    }

    @Override
    public void consume(String msgBodyJson, MessageProperties messageProperties) {
        consume(msgBodyJson, 0, messageProperties);
    }

    @Override
    public void consume(String msgBodyJson, int originalId, MessageProperties messageProperties) {
        try {
            logger.info("consume -> {}", msgBodyJson);
            T obj = JsonHelper.deSerialize(msgBodyJson,
                    (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
            if (messageProperties == null || messageProperties.getCorrelationId() == null) {
                consumeMessage(obj, messageProperties);
                return;
            }
            String correlationId = new String(messageProperties.getCorrelationId(), Charset.defaultCharset());
            if (StringUtils.isEmpty(correlationId)) {
                consumeMessage(obj, messageProperties);
                return;
            }
            if (exchangeType() == ExchangeType.TOPIC || exchangeType() == ExchangeType.FANOUT) {
                consumeMessage(obj, messageProperties);
                return;
            }
            String lockKey = "lock." + correlationId;
            boolean isLock = RedisLockUtil.lock(lockKey, correlationId, 60);
            if (isLock) {
                try {
                    consumeMessage(obj, messageProperties);
                } finally {
                    RedisLockUtil.unLock(lockKey, correlationId);
                }
            } else {
                new RuntimeException("重复消费");
            }
        } catch (Throwable e) {
            exceptionHandle(new MsgQueueBody(BasicOperatorEnum.CONSUMER, msgBodyJson), e, false, originalId);
        }
    }

    private void exceptionHandle(MsgQueueBody msg, Throwable throwable, boolean isAsync, int originalId) {
        logger.error(getMessageDesc() + "|" + msg.getMsgQueueBody(), throwable);
        MessageQueueErrorRecord log = new MessageQueueErrorRecord();
        log.setMsgBody(msg.getMsgQueueBody());
        log.setErrorDesc(DBStringUtil.subString(throwable.getMessage(), 1500));
        log.setOperatorId(msg.getBasicOperatorEnum().getCode());
        String beanName = getSpringBeanName();
        log.setBeanName(beanName);
        log.setTypeId(getMessageType());
        log.setTypeDesc(StringUtils.defaultString(getMessageDesc(), ""));
        log.setOriginalId(originalId);
        log.setAppId(appId);
        logger.info("dbLog -> {}", JsonHelper.serialize(log));
        if (isAsync) {
            rabbitProductExecutor.submit(() -> saveLog(log));
        } else {
            saveLog(log);
        }
    }

    private MessageData getMessageData(String msgBodyJson, int originalId) {
        String id = UUID.randomUUID().toString();
        MessageData data = new MessageData();
        data.setCurrTime(System.currentTimeMillis());
        data.setId(id);
        data.setJsonData(msgBodyJson);
        data.setTypeDesc(getMessageDesc());
        data.setTypeId(getMessageType());
        data.setOriginalId(originalId);
        data.setBeanName(getSpringBeanName());
        data.setAppId(appId);
        return data;
    }

    private String getSpringBeanName() {
        Service service = AopUtils.getTargetClass(this).getAnnotation(Service.class);
        String beanName = service.value();
        if (StringUtils.isNotEmpty(beanName)) {
            return beanName;
        }
        return toLowerCaseFirstOne(this.getClass().getSimpleName());
    }

    private String toLowerCaseFirstOne(String data) {
        if (Character.isLowerCase(data.charAt(0))) {
            return data;
        }
        return (new StringBuilder()).append(Character.toLowerCase(data.charAt(0))).append(data.substring(1)).toString();
    }

    private void saveLog(MessageQueueErrorRecord record) {
        try {
            msgQueueManageService.save(record);
        } catch (Exception e) {
            logger.error("错误消息入库失败：{}", JsonHelper.serialize(record), e);
        }
    }

    protected int getMessageType() {
        return 0;
    }

    protected String getMessageDesc() {
        return getSpringBeanName();
    }

    protected void provideMessage(String msgBodyJson) throws Throwable {
        throw new UnsupportedOperationException();
    }

    protected void provideMessage(String msgBodyJson,
                                  String correlationDataId) throws Throwable {
        provideMessage(msgBodyJson);
    }

    protected void provideMessage(String msgBodyJson,
                                  String correlationDataId,
                                  Map<String, Object> headMap) throws Throwable {
        if (!isAutoRegistry()) {
            provideMessage(msgBodyJson, correlationDataId);
            return;
        }
        String exchangeName = getExchangeName();
        String queueName = getQueueName();
        MessagePostProcessor messagePostProcessor = message -> {
            MessageProperties messageProperties = message.getMessageProperties();
            if (MapExtensions.isNotEmpty(headMap)) {
                headMap.forEach((key, value) -> {
                    messageProperties.setHeader(key, value);
                });
            }
            messageProperties.setCorrelationId(correlationDataId.getBytes());
            return message;
        };
        amqpProducer.publishMsg(exchangeName, queueName, msgBodyJson,
                new CorrelationData(correlationDataId), messagePostProcessor);
    }

    protected void consumeMessage(T msgBody, MessageProperties messageProperties) throws Throwable {
        consumeMessage(msgBody);
    }

    protected void consumeMessage(T msgBody) throws Throwable {
        throw new UnsupportedOperationException();
    }

    protected String getQueueName() {
        return getRabbitmqQueueNamedPrefix() + "queue." + getAliasName();
    }

    protected String getDirectExchangeName() {
        return getRabbitmqQueueNamedPrefix() + "exchange.direct." + getAliasName();
    }

    protected String getTopicExchangeName() {
        return getRabbitmqQueueNamedPrefix() + "exchange.topic." + getAliasName();
    }

    protected String getFanoutExchangeName() {
        return getRabbitmqQueueNamedPrefix() + "exchange.fanout." + getAliasName();
    }

    protected String getRabbitmqQueueNamedPrefix() {
        return "basic.rabbitmq.";
    }

    protected ExchangeType exchangeType() {
        return ExchangeType.DIRECT;
    }

    protected String getRoutingKey() {
        return getQueueName();
    }

    protected boolean isOpenListener() {
        return isOpenListener;
    }

    protected String getAliasName() {
        String beanName = getSpringBeanName();
        return beanName.replace("Service", "");
    }

    protected boolean isAutoRegistry() {
        return true;
    }

    protected boolean isConfirmCallBack() {
        return true;
    }

    private String getExchangeName() {
        ExchangeType exchangeType = exchangeType();
        if (exchangeType == ExchangeType.DIRECT) {
            return getDirectExchangeName();
        }
        if (exchangeType == ExchangeType.FANOUT) {
            return getFanoutExchangeName();
        }
        if (exchangeType == ExchangeType.TOPIC) {
            return getTopicExchangeName();
        }
        if (exchangeType == ExchangeType.DEFAULT) {
            return "";
        }
        return "";
    }

    @PostConstruct
    public void autoRegistry() {
        if (!isAutoRegistry()) {
            return;
        }
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) SpringUtils.getBeanFactory();
        ConnectionFactory connectionFactory = (ConnectionFactory) beanFactory.getBean("basicConnectionFactory");
        RabbitAdmin rabbitAdmin = (RabbitAdmin) beanFactory.getBean("basicRabbitAdmin");
        DynamicConsumer consumer = null;
        try {
            DynamicConsumerContainerFactory fac = DynamicConsumerContainerFactory.builder()
                    .exchangeType(exchangeType())
                    .exchangeName(getExchangeName())
                    .queue(getQueueName())
                    .autoDeleted(false)
                    .autoAck(true)
                    .durable(true)
                    .routingKey(getRoutingKey())
                    .rabbitAdmin(rabbitAdmin)
                    .connectionFactory(connectionFactory).build();
            consumer = new DynamicConsumer(fac, this);
        } catch (Exception e) {
            logger.error("系统异常", e);
        }
        customizeDynamicConsumerContainer.put(getQueueName(), consumer);
        if (isOpenListener()) {
            consumer.start();
        }
    }

    @PreDestroy
    public void destroyMethod() throws Exception {
        stopConsumerListener();
    }

    @Override
    public void stopConsumerListener() {
        DynamicConsumer dynamicConsumer = customizeDynamicConsumerContainer.getByQueueName(getQueueName());
        if (dynamicConsumer != null) {
            dynamicConsumer.stop();
        }
    }

    @Override
    public void shutdownConsumerListener() {
        DynamicConsumer dynamicConsumer = customizeDynamicConsumerContainer.getByQueueName(getQueueName());
        if (dynamicConsumer != null) {
            dynamicConsumer.shutdown();
        }
    }

    @Override
    public void startConsumerListener() {
        DynamicConsumer dynamicConsumer = customizeDynamicConsumerContainer.getByQueueName(getQueueName());
        if (dynamicConsumer != null) {
            dynamicConsumer.start();
        }
    }
}