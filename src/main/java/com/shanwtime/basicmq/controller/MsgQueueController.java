package com.shanwtime.basicmq.controller;

import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;

import com.shanwtime.basicmq.entity.MsgQueueErrorLogCondition;
import com.shanwtime.basicmq.service.IMsgQueueManageService;
import com.google.common.base.Splitter;
import com.shanwtime.basicmq.utils.IntegerExtensions;
import com.shanwtime.basicmq.utils.ParamsValid;
import com.shanwtime.basicmq.utils.Protocol;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by shma on 2019/3/6.
 */
@RestController
@RequestMapping("/mq")
public class MsgQueueController {

    @Resource
    private IMsgQueueManageService msgQueueManageService;

    @RequestMapping(value = "/repush/all")
    public Protocol rePushById(String _appId) {
        ParamsValid valid = new ParamsValid();
        valid.validAppId(_appId);
        if (!valid.isValid()) {
            return valid.showInValidMessage();
        }
        msgQueueManageService.reProvide();
        return new Protocol();
    }

    @RequestMapping(value = "/repush/ids")
    public Protocol rePushByIds(String _appId, String ids) {
        ParamsValid valid = new ParamsValid();
        valid.validAppId(_appId).validNotNull("ids", ids);
        if (!valid.isValid()) {
            return valid.showInValidMessage();
        }
        List<Integer> idList = Splitter.on(",").trimResults()
                .splitToList(ids)
                .stream()
                .map(t -> Integer.parseInt(t))
                .collect(Collectors.toList());
        msgQueueManageService.reProvideByIds(idList);
        return new Protocol();
    }

    @RequestMapping(value = "/repush/id")
    public Protocol rePushById(String _appId, Integer id) {
        ParamsValid valid = new ParamsValid();
        valid.validAppId(_appId).validMoreThenZero("id", id);
        if (!valid.isValid()) {
            return valid.showInValidMessage();
        }
        msgQueueManageService.reProvideById(id);
        return new Protocol();
    }

    @RequestMapping(value = "/repush/typeIds")
    public Protocol rePushByTypeIds(String _appId, String typeIds) {
        ParamsValid valid = new ParamsValid();
        valid.validAppId(_appId).validNotNull("typeIds", typeIds);
        if (!valid.isValid()) {
            return valid.showInValidMessage();
        }
        List<Integer> typeIdList = Splitter.on(",").trimResults()
                .splitToList(typeIds)
                .stream()
                .map(t -> Integer.parseInt(t))
                .collect(Collectors.toList());
        msgQueueManageService.reProvideByTypeIds(typeIdList);
        return new Protocol();
    }

    @RequestMapping(value = "/repush/typeId")
    public Protocol rePushByTypeId(String _appId, Integer typeId) {
        ParamsValid valid = new ParamsValid();
        valid.validAppId(_appId).validMoreThenZero("typeId", typeId);
        if (!valid.isValid()) {
            return valid.showInValidMessage();
        }
        msgQueueManageService.reProvideByTypeIds(typeId);
        return new Protocol();
    }

    @RequestMapping(value = "/modify/status/id", method = RequestMethod.POST)
    public Protocol updateRePushById(String _appId, @RequestBody MsgQueueErrorLogCondition condition) {
        ParamsValid valid = new ParamsValid();
        valid.validAppId(_appId)
                .validMoreThenZero("id", condition.getId());
        if (!valid.isValid()) {
            return valid.showInValidMessage();
        }
        int isRePush = IntegerExtensions.getIntValue(condition.getIsRePush(), 0);
        msgQueueManageService.modifyStatusById(condition.getId(), isRePush);
        return new Protocol(0, "成功");
    }

    @RequestMapping(value = "/modify/status/ids", method = RequestMethod.POST)
    public Protocol updateRePushByIds(String _appId, @RequestBody MsgQueueErrorLogCondition condition) {
        ParamsValid valid = new ParamsValid();
        valid.validAppId(_appId)
                .validNotNullOrEmpty("ids", condition.getIds());
        if (!valid.isValid()) {
            return valid.showInValidMessage();
        }
        List<Integer> idList = Splitter.on(",").trimResults()
                .splitToList(condition.getIds())
                .stream()
                .map(t -> Integer.parseInt(t))
                .collect(Collectors.toList());
        int isRePush = IntegerExtensions.getIntValue(condition.getIsRePush(), 0);
        msgQueueManageService.modifyStatusById(idList, isRePush);
        return new Protocol(0, "成功");
    }

    @RequestMapping(value = "/modify/status/typeId", method = RequestMethod.POST)
    public Protocol updateRePushByTypeId(String _appId, @RequestBody MsgQueueErrorLogCondition condition) {
        ParamsValid valid = new ParamsValid();
        valid.validAppId(_appId)
                .validMoreThenZero("typeId", condition.getTypeId());
        if (!valid.isValid()) {
            return valid.showInValidMessage();
        }
        int isRePush = IntegerExtensions.getIntValue(condition.getIsRePush(), 0);
        msgQueueManageService.modifyStatusByTypeId(condition.getTypeId(), isRePush);
        return new Protocol(0, "成功");
    }

    @RequestMapping(value = "/add/typeId", method = RequestMethod.POST)
    public Protocol addByTypeId(String _appId, @RequestBody MsgQueueErrorLogCondition condition) {
        ParamsValid valid = new ParamsValid();
        valid.validAppId(_appId)
                .validMoreThenZero("typeId", condition.getTypeId())
                .validNotNullOrEmpty("jsonBody", condition.getJsonBody());
        if (!valid.isValid()) {
            return valid.showInValidMessage();
        }
        msgQueueManageService.addMsg(condition.getJsonBody(), condition.getTypeId());
        return new Protocol(0, "成功");
    }

    @RequestMapping(value = "/add/beanName", method = RequestMethod.POST)
    public Protocol addByBeanName(String _appId, @RequestBody MsgQueueErrorLogCondition condition) {
        ParamsValid valid = new ParamsValid();
        valid.validAppId(_appId)
                .validNotNull("beanName", condition.getBeanName())
                .validNotNullOrEmpty("jsonBody", condition.getJsonBody());
        if (!valid.isValid()) {
            return valid.showInValidMessage();
        }
        msgQueueManageService.addMsg(condition.getJsonBody(), condition.getBeanName());
        return new Protocol(0, "成功");
    }

    @RequestMapping(value = "/retry", method = RequestMethod.POST)
    public Protocol retry(String _appId) {
        ParamsValid valid = new ParamsValid();
        valid.validAppId(_appId);
        if (!valid.isValid()) {
            return valid.showInValidMessage();
        }
        msgQueueManageService.retry();
        return new Protocol(0, "成功");
    }

    @RequestMapping(value = "/listener/close/typeId", method = RequestMethod.POST)
    public Protocol closeListenerByTypeId(String _appId, @RequestBody MsgQueueErrorLogCondition condition) {
        ParamsValid valid = new ParamsValid();
        valid.validAppId(_appId)
                .validMoreThenZero("typeId", condition.getTypeId());
        if (!valid.isValid()) {
            return valid.showInValidMessage();
        }
        msgQueueManageService.consumerClosed(condition.getTypeId());
        return new Protocol(0, "成功");
    }

    @RequestMapping(value = "/listener/close/beanName", method = RequestMethod.POST)
    public Protocol closeListenerByBeanName(String _appId, @RequestBody MsgQueueErrorLogCondition condition) {
        ParamsValid valid = new ParamsValid();
        valid.validAppId(_appId)
                .validNotNull("beanName", condition.getBeanName());
        if (!valid.isValid()) {
            return valid.showInValidMessage();
        }
        msgQueueManageService.consumerClosed(condition.getBeanName());
        return new Protocol(0, "成功");
    }

    @RequestMapping(value = "/listener/shutdown/beanName", method = RequestMethod.POST)
    public Protocol restartListenerByBeanName(String _appId, @RequestBody MsgQueueErrorLogCondition condition) {
        ParamsValid valid = new ParamsValid();
        valid.validAppId(_appId)
                .validNotNull("beanName", condition.getBeanName());
        if (!valid.isValid()) {
            return valid.showInValidMessage();
        }
        msgQueueManageService.consumerShutdown(condition.getBeanName());
        return new Protocol(0, "成功");
    }

    @RequestMapping(value = "/listener/shutdown/typeId", method = RequestMethod.POST)
    public Protocol restartListenerByTypeId(String _appId, @RequestBody MsgQueueErrorLogCondition condition) {
        ParamsValid valid = new ParamsValid();
        valid.validAppId(_appId)
                .validMoreThenZero("typeId", condition.getTypeId());
        if (!valid.isValid()) {
            return valid.showInValidMessage();
        }
        msgQueueManageService.consumerShutdown(condition.getTypeId());
        return new Protocol(0, "成功");
    }

    @RequestMapping(value = "/listener/start/beanName", method = RequestMethod.POST)
    public Protocol startListenerByBeanName(String _appId, @RequestBody MsgQueueErrorLogCondition condition) {
        ParamsValid valid = new ParamsValid();
        valid.validAppId(_appId)
                .validNotNull("beanName", condition.getBeanName());
        if (!valid.isValid()) {
            return valid.showInValidMessage();
        }
        msgQueueManageService.consumerStart(condition.getBeanName());
        return new Protocol(0, "成功");
    }

    @RequestMapping(value = "/listener/start/typeId", method = RequestMethod.POST)
    public Protocol startListenerByTypeId(String _appId, @RequestBody MsgQueueErrorLogCondition condition) {
        ParamsValid valid = new ParamsValid();
        valid.validAppId(_appId)
                .validMoreThenZero("typeId", condition.getTypeId());
        if (!valid.isValid()) {
            return valid.showInValidMessage();
        }
        msgQueueManageService.consumerStart(condition.getTypeId());
        return new Protocol(0, "成功");
    }

    @RequestMapping(value = "/repush/open", method = RequestMethod.POST)
    public Protocol rePushOpen(String _appId, @RequestBody MsgQueueErrorLogCondition condition) {
        ParamsValid valid = new ParamsValid();
        valid.validAppId(_appId)
                .validMoreThenZero("id", condition.getId());
        if (!valid.isValid()) {
            return valid.showInValidMessage();
        }
        msgQueueManageService.openRePush(condition.getId());
        return new Protocol(0, "成功");
    }
}
