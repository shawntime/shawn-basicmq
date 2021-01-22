package com.shanwtime.basicmq.service.impl;

import java.util.List;
import javax.annotation.Resource;

import com.shanwtime.basicmq.dao.MessageQueueErrorRecordMapper;
import com.shanwtime.basicmq.entity.MessageQueueErrorRecord;
import com.shanwtime.basicmq.service.IMsgQueueErrorLogService;
import org.springframework.stereotype.Service;

/**
 *
 * @author shma
 * @date 2018/11/29
 */
@Service
public class MsgQueueErrorLogService implements IMsgQueueErrorLogService {

    @Resource
    private MessageQueueErrorRecordMapper messageQueueErrorRecordMapper;

    @Override
    public int save(MessageQueueErrorRecord messageQueueErrorRecord) {
        return messageQueueErrorRecordMapper.save(messageQueueErrorRecord);
    }

    @Override
    public int update(MessageQueueErrorRecord messageQueueErrorRecord) {
        return messageQueueErrorRecordMapper.update(messageQueueErrorRecord);
    }

    @Override
    public int modifyStatusById(int id, int isRePush, int appId) {
        return messageQueueErrorRecordMapper.updateById(id, isRePush, appId);
    }

    @Override
    public int modifyStatusByIds(List<Integer> ids, int isRePush, int appId) {
        return messageQueueErrorRecordMapper.updateByIds(ids, isRePush, appId);
    }

    @Override
    public int modifyStatusByTypeId(int typeId, int isRePush, int appId) {
        return messageQueueErrorRecordMapper.updateByTypeId(typeId, isRePush, appId);
    }

    @Override
    public MessageQueueErrorRecord getById(int id, int appId) {
        return messageQueueErrorRecordMapper.getById(id, appId);
    }

    @Override
    public List<MessageQueueErrorRecord> getByTypeId(int typeId, int appId) {
        return messageQueueErrorRecordMapper.getByTypeId(typeId, appId);
    }

    @Override
    public List<MessageQueueErrorRecord> getByTypeIds(List<Integer> typeIds, int appId) {
        return messageQueueErrorRecordMapper.getByTypeIds(typeIds, appId);
    }

    @Override
    public List<MessageQueueErrorRecord> getAll(int appId) {
        return messageQueueErrorRecordMapper.getAll(appId);
    }
}