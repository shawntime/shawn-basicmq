package com.shanwtime.basicmq.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;

import com.shanwtime.basicmq.entity.MessageQueueErrorRecord;
import com.shanwtime.basicmq.enums.BasicOperatorEnum;
import com.shanwtime.basicmq.redis.RedisClient;
import com.shanwtime.basicmq.service.IMsgQueueErrorLogService;
import com.shanwtime.basicmq.service.IMsgQueueManageService;
import com.shanwtime.basicmq.utils.IntegerExtensions;
import com.shanwtime.basicmq.utils.JsonHelper;
import com.shanwtime.basicmq.utils.LocalDateUtil;
import com.shanwtime.basicmq.utils.MapExtensions;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * @author shma
 * @date 2018/11/28
 */
@Service
public class MsgQueueManageService implements IMsgQueueManageService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MsgQueueManageService.class);

    @Resource
    private IMsgQueueErrorLogService msgQueueErrorLogService;

    @Resource
    private RedisClient redisClient;

    @Resource
    private MsgQueueFactory msgQueueFactory;

    @Value("${spring.rabbitmq.basic.appId:0}")
    private int appId;

    @Override
    public void reProvideById(int id) {
        MessageQueueErrorRecord record = msgQueueErrorLogService.getById(id, appId);
        if (record == null) {
            return;
        }

        rePush(record);
    }

    @Override
    public void reProvideByIds(List<Integer> ids) {
        ids.forEach(this::reProvideById);
    }

    @Override
    public void reProvideByTypeIds(int typeId) {
        List<MessageQueueErrorRecord> records = msgQueueErrorLogService.getByTypeId(typeId, appId);
        if (CollectionUtils.isEmpty(records)) {
            return;
        }

        records.forEach(this::rePush);
    }

    @Override
    public void reProvideByTypeIds(List<Integer> typeIds) {
        List<MessageQueueErrorRecord> records = msgQueueErrorLogService.getByTypeIds(typeIds, appId);
        if (CollectionUtils.isEmpty(records)) {
            return;
        }

        records.forEach(this::rePush);
    }

    @Override
    public void reProvide() {
        List<MessageQueueErrorRecord> records = msgQueueErrorLogService.getAll(appId);
        if (CollectionUtils.isEmpty(records)) {
            return;
        }

        records.forEach(this::rePush);
    }

    @Override
    public void modifyStatusById(int id, int isRePush) {
        msgQueueErrorLogService.modifyStatusById(id, isRePush, appId);
    }

    @Override
    public void modifyStatusById(List<Integer> ids, int isRePush) {
        msgQueueErrorLogService.modifyStatusByIds(ids, isRePush, appId);
    }

    @Override
    public void modifyStatusByTypeId(int typeId, int isRePush) {
        msgQueueErrorLogService.modifyStatusByTypeId(typeId, isRePush, appId);
    }

    @Override
    public void save(MessageQueueErrorRecord record) {
        msgQueueErrorLogService.save(record);
    }

    @Override
    public void retry() {
        Map<String, String> keyMap = redisClient.hgetAll(Constant.queue_key);
        if (MapExtensions.isEmpty(keyMap)) {
            return;
        }
        keyMap.forEach((id, value) -> {
            MessageData data = JsonHelper.deSerialize(value, MessageData.class);
            if (System.currentTimeMillis() - data.getCurrTime() > 5 * 60 * 1000) {
                MessageQueueErrorRecord log = new MessageQueueErrorRecord();
                log.setBeanName(data.getBeanName());
                log.setErrorDesc("");
                log.setIsRePush(0);
                log.setMsgBody(data.getJsonData());
                log.setTypeDesc(data.getTypeDesc());
                log.setOperatorId(BasicOperatorEnum.PROVIDER.getCode());
                log.setTypeId(data.getTypeId());
                log.setOriginalId(data.getOriginalId());
                log.setAppId(data.getAppId());
                msgQueueErrorLogService.save(log);
                LOGGER.error("MQ生产端5分钟内未收到ack，消息落库：log -> {}", JsonHelper.serialize(log));
                redisClient.hdel(Constant.queue_key, id);
            }
        });
    }

    @Override
    public void consumerClosed(int typeId) {
        MessageQueueErrorRecord record = new MessageQueueErrorRecord();
        record.setTypeId(typeId);
        AbstractMsgQueueService messageQueueService =
                (AbstractMsgQueueService) msgQueueFactory.getMsgQueueService(record);
        messageQueueService.stopConsumerListener();
    }

    @Override
    public void consumerShutdown(int typeId) {
        MessageQueueErrorRecord record = new MessageQueueErrorRecord();
        record.setTypeId(typeId);
        AbstractMsgQueueService messageQueueService =
                (AbstractMsgQueueService) msgQueueFactory.getMsgQueueService(record);
        messageQueueService.shutdownConsumerListener();
    }

    @Override
    public void consumerStart(int typeId) {
        MessageQueueErrorRecord record = new MessageQueueErrorRecord();
        record.setTypeId(typeId);
        AbstractMsgQueueService messageQueueService =
                (AbstractMsgQueueService) msgQueueFactory.getMsgQueueService(record);
        messageQueueService.startConsumerListener();
    }

    @Override
    public void consumerClosed(String beanName) {
        MessageQueueErrorRecord record = new MessageQueueErrorRecord();
        record.setBeanName(beanName);
        AbstractMsgQueueService messageQueueService =
                (AbstractMsgQueueService) msgQueueFactory.getMsgQueueService(record);
        messageQueueService.stopConsumerListener();
    }

    @Override
    public void consumerShutdown(String beanName) {
        MessageQueueErrorRecord record = new MessageQueueErrorRecord();
        record.setBeanName(beanName);
        AbstractMsgQueueService messageQueueService =
                (AbstractMsgQueueService) msgQueueFactory.getMsgQueueService(record);
        messageQueueService.shutdownConsumerListener();
    }

    @Override
    public void consumerStart(String beanName) {
        MessageQueueErrorRecord record = new MessageQueueErrorRecord();
        record.setBeanName(beanName);
        AbstractMsgQueueService messageQueueService =
                (AbstractMsgQueueService) msgQueueFactory.getMsgQueueService(record);
        messageQueueService.startConsumerListener();
    }

    @Override
    public void addMsg(String jsonBody, int typeId) {
        MessageQueueErrorRecord record = new MessageQueueErrorRecord();
        record.setTypeId(typeId);
        AbstractMsgQueueService messageQueueService =
                (AbstractMsgQueueService) msgQueueFactory.getMsgQueueService(record);
        messageQueueService.provide(jsonBody);
    }

    @Override
    public void addMsg(String jsonBody, String beanName) {
        MessageQueueErrorRecord record = new MessageQueueErrorRecord();
        record.setBeanName(beanName);
        AbstractMsgQueueService messageQueueService =
                (AbstractMsgQueueService) msgQueueFactory.getMsgQueueService(record);
        messageQueueService.provide(jsonBody);
    }

    @Override
    public void openRePush(int id) {
        String key = getRedisKey(id);
        redisClient.del(key);
    }

    private void rePush(MessageQueueErrorRecord record) {
        int id = record.getOriginalId();
        if (!IntegerExtensions.isMoreThanZero(record.getOriginalId())) {
            id = record.getId();
        }
        if (!check(id)) {
            return;
        }
        AbstractMsgQueueService messageQueueService =
                (AbstractMsgQueueService) msgQueueFactory.getMsgQueueService(record);
        messageQueueService.provide(record.getMsgBody(), id);
        record.setIsRePush(1);
        msgQueueErrorLogService.update(record);
    }

    private boolean check(int id) {
        String key = getRedisKey(id);
        Long increment = redisClient.increment(key, 1L, 24 * 60 * 60);
        return increment == null || increment.longValue() <= 3;
    }

    private String getRedisKey(int id) {
        return "message.queue.consume.limit"
                + "." + LocalDateUtil.format(new Date(), "yyyyMMdd")
                + "#" + id;
    }
}
