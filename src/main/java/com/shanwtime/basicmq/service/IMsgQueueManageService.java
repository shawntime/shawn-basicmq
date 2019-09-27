package com.shanwtime.basicmq.service;

import java.util.List;

import com.shanwtime.basicmq.entity.MessageQueueErrorRecord;

/**
 * Created by shma on 2018/11/28.
 */
public interface IMsgQueueManageService {

    void reProvideById(int id);

    void reProvideByIds(List<Integer> ids);

    void reProvideByTypeIds(int typeId);

    void reProvideByTypeIds(List<Integer> typeIds);

    void reProvide();

    void modifyStatusById(int id, int isRePush);

    void modifyStatusById(List<Integer> ids, int isRePush);

    void modifyStatusByTypeId(int typeId, int isRePush);

    void save(MessageQueueErrorRecord record);

    void retry();

    void consumerClosed(int typeId);

    void consumerShutdown(int typeId);

    void consumerStart(int typeId);

    void consumerClosed(String beanName);

    void consumerShutdown(String beanName);

    void consumerStart(String beanName);

    void addMsg(String jsonBody, int typeId);

    void addMsg(String jsonBody, String beanName);

    void openRePush(int id);
}
