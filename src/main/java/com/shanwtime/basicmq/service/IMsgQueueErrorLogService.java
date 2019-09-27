package com.shanwtime.basicmq.service;

import java.util.List;

import com.shanwtime.basicmq.entity.MessageQueueErrorRecord;

/**
 * Created by shma on 2018/11/28.
 */
public interface IMsgQueueErrorLogService {

    int save(MessageQueueErrorRecord messageQueueErrorRecord);

    int update(MessageQueueErrorRecord messageQueueErrorRecord);

    int modifyStatusById(int id, int isRePush);

    int modifyStatusByIds(List<Integer> ids, int isRePush);

    int modifyStatusByTypeId(int typeId, int isRePush);

    MessageQueueErrorRecord getById(int id);

    List<MessageQueueErrorRecord> getByTypeId(int typeId);

    List<MessageQueueErrorRecord> getByTypeIds(List<Integer> typeIds);

    List<MessageQueueErrorRecord> getAll();
}
