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

    MessageQueueErrorRecord getById(@Param("id") int id);

    List<MessageQueueErrorRecord> getByIds(List<Integer> ids);

    List<MessageQueueErrorRecord> getByTypeId(@Param("typeId") int typeId);

    List<MessageQueueErrorRecord> getByTypeIds(List<Integer> typeIds);

    List<MessageQueueErrorRecord> getAll();

    int updateById(@Param("id") int id, @Param("isRePush") int isRePush);

    int updateByIds(@Param("ids") List<Integer> ids, @Param("isRePush") int isRePush);

    int updateByTyped(@Param("typeId") int typeId, @Param("isRePush") int isRePush);

    int updateByTypeds(@Param("typeIds") List<Integer> typeIds, @Param("isRePush") int isRePush);
}
