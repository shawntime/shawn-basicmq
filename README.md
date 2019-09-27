#### 开发背景
* 项目中用到了rabbitmq，在引入一个新的交换机或者队列，都需要配置exchange、queue、routingKey，重复工作比较繁琐，消费端和生产端分离时需配置多份，易出错
* 发送消息和消费消息时存在丢失消息的情况，无法保障消息100%投递和100%消费
* 消费端存在重复消费

#### 功能描述

* 支持引入新的队列时零配置，自动配置消息队列、交换机，无需手动配置，避免配置出错
* 支持手动配置，兼容旧队列配置，支持自定义队列名、交换机名、交换机方式
* 支持停止消费端监听和启动消费端监听
* 保障消息100%投递，支持发送失败监听后异步重试
* 保障消息100%消费，支持消费失败后异步重试
* 提供API，控制对发送失败和消息失败的队列进行处理，诸如：修改消息体，重新消费，重新发送等
* 封装了简单定时任务处理消息的重新入队
* 解决消费端重复消费问题

#### 使用案例

> 实现了零配置，无需关心配置细节，只需要关系自己的业务逻辑即可

1）仅需要生产端

```
@Service("smsMsgQueueService")
public class SmsMsgQueueService extends AbstractMsgQueueService<SMSEntity> {

    @Override
    protected String getMessageDesc() {
        return "发送短信";
    }
}

// 调用方

@Resource(name = "smsMsgQueueService")
private IMsgQueueService smsMsgQueueService;

public void sendSms(SMSEntity smsEntity) {
    smsService.provide(JsonHelper.serialize(smsEntity));
}

```

2）生产端+消费端

> 实现protected void consumeMessage(SmsEntity smsEntity, MessageProperties messageProperties)即可

```
@Service("smsMsgQueueService")
public class SmsMsgQueueService extends AbstractMsgQueueService<SmsEntity> {
    
    @Override
    protected String getMessageDesc() {
        return "发送短信";
    }

    @Override
    protected void consumeMessage(SmsEntity smsEntity, MessageProperties messageProperties) throws Throwable {
        // 消息的具体处理类
    }
}
```
++如何不需要消息头信息，则实现++

++protected void consumeMessage(SmsEntity smsEntity) throws Throwable() {}++

++方法即可++

#### 使用方法

*  Maven dependency:

```
<dependency>
    <groupId>com.shanwtime</groupId>
    <artifactId>basicmq</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

* 包扫描配置类添加【com.shawntime.basic.dao】

```
static final String SCAN_PACKAGE = "com.shawntime.provider.dao.mapper." + DB_TGA + ",com.shawntime.basic.dao";
Resource[] locationResources = resolver.getResources(mapperLocations);
Resource[] mqResources = resolver.getResources("classpath:mapper/basicmq/MessageQueueErrorLogMapper.xml");
List<Resource> resources = new ArrayList<>(locationResources.length + mqResources.length);
resources.addAll(Arrays.asList(locationResources));
resources.addAll(Arrays.asList(mqResources));
sqlSessionFactoryBean.setMapperLocations(resources.stream().toArray(Resource[]::new));
```

* 加包扫描
```
@ComponentScan(basePackages = {"com.shawntime.basic")
```

* yml文件配置rabbitmq链接参数

```
spring:
  rabbitmq:
    base:
      host: 127.0.0.1
      port: 5672
      username: admin
      password: 123456
      vhost: test
      isOpenListener: true
```

> isOpenListener: 是否开启消费端，默认关闭

* 如何需要开启组件自带的定时任务，则需要配置application.properties

```
openScheduledTask=true
```

##### 旧项目的修改
> 已经引入basicmq项目的实现类，需要重写以下两个方法
> isAutoRegistry：是否自动注册
> isConfirmCallBack：是否确认交换机接收消息

```
@Service
public class DingTalkService extends AbstractMsgQueueService<DingMessage> {

    @Resource(name = "dingTalkTemplate")
    private AmqpTemplate dingTalkTemplate;

    @Value("${spring.rabbitmq.tmApi.queue.dingTalk}")
    private String dingTalkQueue;

    @Value(" ${spring.rabbitmq.tmApi.exchange.dingTalk}")
    private String dingTalkExchange;

    @Override
    protected String getMessageDesc() {
        return "钉钉消息";
    }

    @Override
    protected void provideMessage(String msgBodyJson) throws Throwable {
        dingTalkTemplate.convertAndSend(dingTalkQueue, msgBodyJson);
    }

    @Override
    protected boolean isAutoRegistry() {
        return false;
    }

    @Override
    protected boolean isConfirmCallBack() {
        return false;
    }
}
```

#### 代码分析

##### 生产端消息100%投递

```
@Override
public void provide(String msgBodyJson, boolean isAsync, Map headMap) {
    try {
        logger.info("provide -> {}", msgBodyJson);
        String correlationDataId = "";
        if (isConfirmCallBack()) {
            MessageData data = getMessageData(msgBodyJson);
            redisClient.hset(Constant.queue_key, data.getId(), JsonHelper.serialize(data), -1);
            correlationDataId = data.getId();
        }
        provideMessage(msgBodyJson, correlationDataId, headMap);
    } catch (Throwable e) {
        exceptionHandle(new MsgQueueBody(BasicOperatorEnum.PROVIDER, msgBodyJson), e, isAsync);
    }
}

private MessageData getMessageData(String msgBodyJson) {
    String id = UUID.randomUUID().toString();
    MessageData data = new MessageData();
    data.setCurrTime(System.currentTimeMillis());
    data.setId(id);
    data.setJsonData(msgBodyJson);
    data.setTypeDesc(getMessageDesc());
    data.setTypeId(getMessageType());
    data.setOriginalId(0);
    data.setBeanName(getSpringBeanName());
    return data;
}

protected void provideMessage(String msgBodyJson,
                                  String correlationDataId,
                                  Map<String, Object> headMap) throws Throwable {
    if (!isAutoRegistry()) {
        provideMessage(msgBodyJson, correlationDataId);
        return;
    }
    String exchangeName = getExchangeName();
    String queueName = getQueueName();
    MessagePostProcessor messagePostProcessor = message -> {
        MessageProperties messageProperties = message.getMessageProperties();
        if (MapExtensions.isNotEmpty(headMap)) {
            headMap.forEach((key, value) -> {
                messageProperties.setHeader(key, value);
            });
        }
        messageProperties.setCorrelationId(correlationDataId.getBytes());
        return message;
    };
    amqpProducer.publishMsg(exchangeName, queueName, msgBodyJson,
            new CorrelationData(correlationDataId), messagePostProcessor);
}
```

##### 发送端消息确认
```
@Component("confirmCallBackListener")
public class ConfirmCallBackListener implements RabbitTemplate.ConfirmCallback {

    @Resource
    private RedisClient redisClient;

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        String id = correlationData.getId();
        if (StringUtils.isEmpty(id)) {
            return;
        }
        if (ack) {
            redisClient.hdel(Constant.queue_key, id);
        }
    }

}
```

##### 发送失败的消息job异步重试
```
@Override
public void retry() {
    Map<String, String> keyMap = redisClient.hgetAll(Constant.queue_key);
    if (MapExtensions.isEmpty(keyMap)) {
        return;
    }
    keyMap.forEach((id, value) -> {
        MessageData data = JsonHelper.deSerialize(value, MessageData.class);
        if (System.currentTimeMillis() - data.getCurrTime() > 5 * 60 * 1000) {
            MessageQueueErrorRecord log = new MessageQueueErrorRecord();
            log.setBeanName(data.getBeanName());
            log.setErrorDesc("");
            log.setIsRePush(0);
            log.setMsgBody(data.getJsonData());
            log.setTypeDesc(data.getTypeDesc());
            log.setOperatorId(BasicOperatorEnum.PROVIDER.getCode());
            log.setTypeId(data.getTypeId());
            log.setOriginalId(data.getOriginalId());
            msgQueueErrorLogService.save(log);
            redisClient.hdel(Constant.queue_key, id);
        }
    });
}
```

##### 消费端100%消费
> 处理失败消息统一入库
```
@Override
public void consume(String msgBodyJson, int originalId, MessageProperties messageProperties) {
    try {
        logger.info("consume -> {}", msgBodyJson);
        T obj = JsonHelper.deSerialize(msgBodyJson,
                (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
        if (messageProperties == null || messageProperties.getCorrelationId() == null) {
            consumeMessage(obj, messageProperties);
            return;
        }
        String correlationId = new String(messageProperties.getCorrelationId(), Charset.defaultCharset());
        if (StringUtils.isEmpty(correlationId)) {
            consumeMessage(obj, messageProperties);
            return;
        }
        if (exchangeType() == ExchangeType.TOPIC || exchangeType() == ExchangeType.FANOUT) {
            consumeMessage(obj, messageProperties);
            return;
        }
        String lockKey = "lock." + correlationId;
        boolean isLock = RedisLockUtil.lock(lockKey, correlationId, 60);
        if (isLock) {
            try {
                consumeMessage(obj, messageProperties);
            } finally {
                RedisLockUtil.unLock(lockKey, correlationId);
            }
        } else {
            new RuntimeException("重复消费");
        }
    } catch (Throwable e) {
        exceptionHandle(new MsgQueueBody(BasicOperatorEnum.CONSUMER, msgBodyJson), e, false, originalId);
    }
}

private void exceptionHandle(MsgQueueBody msg, Throwable throwable, boolean isAsync, int originalId) {
    logger.error(getMessageDesc() + "|" + msg.getMsgQueueBody(), throwable);
    MessageQueueErrorRecord log = new MessageQueueErrorRecord();
    log.setMsgBody(msg.getMsgQueueBody());
    log.setErrorDesc(DBStringUtil.subString(throwable.getMessage(), 1500));
    log.setOperatorId(msg.getBasicOperatorEnum().getCode());
    String beanName = getSpringBeanName();
    log.setBeanName(beanName);
    log.setTypeId(getMessageType());
    log.setTypeDesc(StringUtils.defaultString(getMessageDesc(), ""));
    log.setOriginalId(originalId);
    logger.info("dbLog -> {}", JsonHelper.serialize(log));
    if (isAsync) {
        rabbitProductExecutor.submit(() -> saveLog(log));
    } else {
        saveLog(log);
    }
}
```

##### 异常消息重试
```
private void rePush(MessageQueueErrorRecord record) {
    int id = record.getOriginalId();
    if (!IntegerExtensions.isMoreThanZero(record.getOriginalId())) {
        id = record.getId();
    }
    if (!check(id)) {
        return;
    }
    AbstractMsgQueueService messageQueueService =
            (AbstractMsgQueueService) msgQueueFactory.getMsgQueueService(record);
    messageQueueService.consume(record.getMsgBody(), id);
    record.setIsRePush(1);
    msgQueueErrorLogService.update(record);
}

private boolean check(int id) {
    String key = getRedisKey(id);
    Long increment = redisClient.increment(key, 1L, 24 * 60 * 60);
    return increment == null || increment.longValue() <= 3;
}

private String getRedisKey(int id) {
    return "message.queue.consume.limit"
            + "." + LocalDateUtil.format(new Date(), "yyyyMMdd")
            + "#" + id;
}
```

##### 配置自动注入监听
```
@PostConstruct
public void autoRegistry() {
    if (!isAutoRegistry()) {
        return;
    }
    DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) SpringUtils.getBeanFactory();
    ConnectionFactory connectionFactory = (ConnectionFactory) beanFactory.getBean("basicConnectionFactory");
    RabbitAdmin rabbitAdmin = (RabbitAdmin) beanFactory.getBean("basicRabbitAdmin");
    DynamicConsumer consumer = null;
    try {
        DynamicConsumerContainerFactory fac = DynamicConsumerContainerFactory.builder()
                .exchangeType(exchangeType())
                .exchangeName(getExchangeName())
                .queue(getQueueName())
                .autoDeleted(false)
                .autoAck(true)
                .durable(true)
                .routingKey(getRoutingKey())
                .rabbitAdmin(rabbitAdmin)
                .connectionFactory(connectionFactory).build();
        consumer = new DynamicConsumer(fac, this);
    } catch (Exception e) {
        logger.error("系统异常", e);
    }
    customizeDynamicConsumerContainer.put(getQueueName(), consumer);
    if (isOpenListener()) {
        consumer.start();
    }
}
```

##### AbstractMsgQueueService 抽象类使用

###### 发送消息

```
/**
* msgBodyJson：json序列化的消息体
* isAsync：发送失败是否异步入库
* headMap：发送消息时携带的消息头
*/
public void provide(String msgBodyJson)
public void provide(String msgBodyJson, Map headMap)
public void provide(String msgBodyJson, boolean isAsync, Map headMap)
```

##### 消费消息

```
/**
* msgBodyJson：接收到的消息体
* originalId：重试代入的原始消息主键id，用于控制消息每天重试次数
* messageProperties：消息头信息
*/
public void consume(String msgBodyJson)
public void consume(String msgBodyJson, int originalId)
public void consume(String msgBodyJson, MessageProperties messageProperties)
public void consume(String msgBodyJson, int originalId, MessageProperties messageProperties)
```

##### 查找实体bean
> 如果定义了messageType则按照messageType查找，如果没有定义则按照service的bean名称查找
```
protected int getMessageType()
private String getSpringBeanName()
```

##### 自定义exchange、queue、exchange type、routingKey
> 重写以下方法
```
protected String getQueueName()
protected String getDirectExchangeName()
protected String getTopicExchangeName()
protected String getFanoutExchangeName()
protected ExchangeType exchangeType()
protected String getRoutingKey()
```

#### api接口介绍

* mq/repush/ids：根据多个主键id重新推送失败消息
* mq/repush/id：根据主键id重新推送失败消息
* mq/repush/typeIds：按照多个类型id重推某一类型下所有失败的消息
* mq/repush/typeId：按照类型id重推某一类型下所有失败的消息
* mq/modify/status/id：根据主键id修改消息体重试状态
* mq/modify/status/ids：根据多个主键id修改消息重试试状态
* mq/modify/status/typeId：根据类型修改消息重试状态
* mq/add/typeId：根据类型id添加消息
* mq/add/beanName：根据beanName添加消息
* mq/retry：生产端失败消息重试
* mq/listener/close/typeId：根据类型id关闭该类型的消息监听
* mq/listener/close/beanName：根据beanName关闭该beanName的消息监听
* mq/listener/start/typeId：根据类型id启动该类型的消息监听
* mq/listener/start/beanName：根据beanName启动该beanName的消息监听

##### 创建数据库

* SQL Server

```sql
CREATE TABLE [dbo].[MessageQueueErrorRecord] (
  [id] int  IDENTITY(1,1) NOT NULL,
  [operator_id] tinyint  NOT NULL,
  [type_id] int  NOT NULL,
  [type_desc] varchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [bean_name] varchar(200) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [msg_body] varchar(8000) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [error_desc] varchar(2000) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [original_id] int DEFAULT ((0)) NOT NULL,
  [is_re_push] tinyint DEFAULT ((0)) NOT NULL,
  [created_stime] datetime DEFAULT (getdate()) NOT NULL,
  [modified_stime] datetime DEFAULT (getdate()) NOT NULL,
  [is_del] int DEFAULT ((0)) NOT NULL
)
GO

ALTER TABLE [dbo].[MessageQueueErrorRecord] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'主键id',
'SCHEMA', N'dbo',
'TABLE', N'MessageQueueErrorRecord',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'操作类型，1:消费端，2：生产端',
'SCHEMA', N'dbo',
'TABLE', N'MessageQueueErrorRecord',
'COLUMN', N'operator_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'消息类型id，1001-"消费端：原始订单入库"，2001-"生产端：原始订单入库"，1002-"消费端：百万代理人订单推送"，2002-"生产端：百万代理人订单推送"，1003-"消费端：发送短信"，2003-"生产端：添加短信信息"，1004-"消费端：订单导出"，2004-"生产端：订单导出"',
'SCHEMA', N'dbo',
'TABLE', N'MessageQueueErrorRecord',
'COLUMN', N'type_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'消息类型描述',
'SCHEMA', N'dbo',
'TABLE', N'MessageQueueErrorRecord',
'COLUMN', N'type_desc'
GO

EXEC sp_addextendedproperty
'MS_Description', N'消息对应spring bean名称',
'SCHEMA', N'dbo',
'TABLE', N'MessageQueueErrorRecord',
'COLUMN', N'bean_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'消息体',
'SCHEMA', N'dbo',
'TABLE', N'MessageQueueErrorRecord',
'COLUMN', N'msg_body'
GO

EXEC sp_addextendedproperty
'MS_Description', N'错误描述',
'SCHEMA', N'dbo',
'TABLE', N'MessageQueueErrorRecord',
'COLUMN', N'error_desc'
GO

EXEC sp_addextendedproperty
'MS_Description', N'原始id',
'SCHEMA', N'dbo',
'TABLE', N'MessageQueueErrorRecord',
'COLUMN', N'original_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'是否重新推送，0：没有，1：有',
'SCHEMA', N'dbo',
'TABLE', N'MessageQueueErrorRecord',
'COLUMN', N'is_re_push'
GO

EXEC sp_addextendedproperty
'MS_Description', N'创建时间',
'SCHEMA', N'dbo',
'TABLE', N'MessageQueueErrorRecord',
'COLUMN', N'created_stime'
GO

EXEC sp_addextendedproperty
'MS_Description', N'修改时间',
'SCHEMA', N'dbo',
'TABLE', N'MessageQueueErrorRecord',
'COLUMN', N'modified_stime'
GO

EXEC sp_addextendedproperty
'MS_Description', N'是否删除',
'SCHEMA', N'dbo',
'TABLE', N'MessageQueueErrorRecord',
'COLUMN', N'is_del'
GO

EXEC sp_addextendedproperty
'MS_Description', N'消息队列错误记录表',
'SCHEMA', N'dbo',
'TABLE', N'MessageQueueErrorRecord'
GO


-- ----------------------------
-- Primary Key structure for table MessageQueueErrorRecord
-- ----------------------------
ALTER TABLE [dbo].[MessageQueueErrorRecord] ADD CONSTRAINT [PK_MessageQueueErrorRecord] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [DFG]
GO
```

* MYSQL
```sql
    
```

##### 参数资料

rabbitMQ动态队列实现参考：https://blog.csdn.net/kingvin_xm/article/details/86712613



