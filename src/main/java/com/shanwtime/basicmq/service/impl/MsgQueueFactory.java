package com.shanwtime.basicmq.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Resource;

import com.shanwtime.basicmq.entity.MessageQueueErrorRecord;
import com.shanwtime.basicmq.service.IMsgQueueService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 *
 * @author shma
 * @date 2018/11/28
 */
@Service
public class MsgQueueFactory {

    @Resource
    private List<AbstractMsgQueueService> msgQueueServiceList;

    @Resource
    private Map<String, AbstractMsgQueueService> msgQueueServiceMap;

    public final IMsgQueueService getMsgQueueService(MessageQueueErrorRecord record) {
        int typeId = record.getTypeId();
        if (typeId > 0) {
            Optional<AbstractMsgQueueService> optional = msgQueueServiceList.stream()
                    .filter(mq -> mq.getMessageType() == typeId)
                    .findFirst();
            if (optional.isPresent()) {
                return optional.get();
            }
        }
        String beanName = record.getBeanName();
        if (StringUtils.isNotEmpty(beanName)) {
            AbstractMsgQueueService msgQueueService = msgQueueServiceMap.get(beanName);
            if (msgQueueService != null) {
                return msgQueueService;
            }
        }
        throw new IllegalArgumentException(
                String.format("Not typeId : %d, beanName : %s with AbstractMsgQueueService", typeId, beanName));
    }
}
