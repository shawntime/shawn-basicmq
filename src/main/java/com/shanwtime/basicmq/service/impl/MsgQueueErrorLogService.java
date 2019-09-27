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
    public int modifyStatusById(int id, int isRePush) {
        return messageQueueErrorRecordMapper.updateById(id, isRePush);
    }

    @Override
    public int modifyStatusByIds(List<Integer> ids, int isRePush) {
        return messageQueueErrorRecordMapper.updateByIds(ids, isRePush);
    }

    @Override
    public int modifyStatusByTypeId(int typeId, int isRePush) {
        return messageQueueErrorRecordMapper.updateByTyped(typeId, isRePush);
    }

    @Override
    public MessageQueueErrorRecord getById(int id) {
        return messageQueueErrorRecordMapper.getById(id);
    }

    @Override
    public List<MessageQueueErrorRecord> getByTypeId(int typeId) {
        return messageQueueErrorRecordMapper.getByTypeId(typeId);
    }

    @Override
    public List<MessageQueueErrorRecord> getByTypeIds(List<Integer> typeIds) {
        return messageQueueErrorRecordMapper.getByTypeIds(typeIds);
    }

    @Override
    public List<MessageQueueErrorRecord> getAll() {
        return messageQueueErrorRecordMapper.getAll();
    }
}
