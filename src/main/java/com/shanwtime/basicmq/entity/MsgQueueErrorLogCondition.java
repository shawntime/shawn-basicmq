package com.shanwtime.basicmq.entity;

/**
 * Created by shma on 2019/3/6.
 */
public class MsgQueueErrorLogCondition {

    private Integer id;

    private String ids;

    private Integer typeId;

    private String typeIds;

    private Integer isRePush;

    private String jsonBody;

    private String beanName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIds() {
        return ids;
    }

    public void setIds(String ids) {
        this.ids = ids;
    }

    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public String getTypeIds() {
        return typeIds;
    }

    public void setTypeIds(String typeIds) {
        this.typeIds = typeIds;
    }

    public Integer getIsRePush() {
        return isRePush;
    }

    public void setIsRePush(Integer isRePush) {
        this.isRePush = isRePush;
    }

    public String getJsonBody() {
        return jsonBody;
    }

    public void setJsonBody(String jsonBody) {
        this.jsonBody = jsonBody;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }
}
