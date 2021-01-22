package com.shanwtime.basicmq.service;

import java.util.List;

import com.shanwtime.basicmq.entity.MessageQueueErrorRecord;

/**
 * Created by shma on 2018/11/28.
 */
public interface IMsgQueueErrorLogService {

    int save(MessageQueueErrorRecord messageQueueErrorRecord);

    int update(MessageQueueErrorRecord messageQueueErrorRecord);

    int modifyStatusById(int id, int isRePush, int appId);

    int modifyStatusByIds(List<Integer> ids, int isRePush, int appId);

    int modifyStatusByTypeId(int typeId, int isRePush, int appId);

    MessageQueueErrorRecord getById(int id, int appId);

    List<MessageQueueErrorRecord> getByTypeId(int typeId, int appId);

    List<MessageQueueErrorRecord> getByTypeIds(List<Integer> typeIds, int appId);

    List<MessageQueueErrorRecord> getAll(int appId);
}
