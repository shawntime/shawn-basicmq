package com.shanwtime.basicmq.dao;

import java.util.List;

import com.shanwtime.basicmq.entity.MessageQueueErrorRecord;
import org.apache.ibatis.annotations.Param;

/**
 * @author h p
 */
public interface MessageQueueErrorRecordMapper {

    int save(MessageQueueErrorRecord messageQueueErrorRecord);

    int update(MessageQueueErrorRecord messageQueueErrorRecord);

    MessageQueueErrorRecord getById(@Param("id") int id, @Param("appId") int appId);

    List<MessageQueueErrorRecord> getByIds(@Param("ids")List<Integer> ids, @Param("appId") int appId);

    List<MessageQueueErrorRecord> getByTypeId(@Param("typeId") int typeId, @Param("appId") int appId);

    List<MessageQueueErrorRecord> getByTypeIds(@Param("typeIds") List<Integer> typeIds, @Param("appId") int appId);

    List<MessageQueueErrorRecord> getAll(@Param("appId") int appId);

    int updateById(@Param("id") int id, @Param("isRePush") int isRePush, @Param("appId") int appId);

    int updateByIds(@Param("ids") List<Integer> ids, @Param("isRePush") int isRePush, @Param("appId") int appId);

    int updateByTypeId(@Param("typeId") int typeId, @Param("isRePush") int isRePush, @Param("appId") int appId);
}
