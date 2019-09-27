package com.shanwtime.basicmq.dynamic;

import java.util.List;
import javax.annotation.Resource;

import com.shanwtime.basicmq.service.impl.AbstractMsgQueueService;
import com.shanwtime.basicmq.utils.SpringUtils;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author mashaohua
 */
@Component
public class MessageQueueBeanRegister implements BeanDefinitionRegistryPostProcessor {

    private ApplicationContext applicationContext;

    private BeanDefinitionRegistry beanDefinitionRegistry;

    @Resource
    private List<AbstractMsgQueueService> msgQueueServiceList;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry)
            throws BeansException {

        this.beanDefinitionRegistry = beanDefinitionRegistry;

        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) SpringUtils.getBeanFactory();
        ConnectionFactory connectionFactory = (ConnectionFactory) beanFactory.getBean("basicConnectionFactory");
        RabbitAdmin rabbitAdmin = (RabbitAdmin) beanFactory.getBean("basicRabbitAdmin");

        msgQueueServiceList.forEach(abstractMsgQueueService -> {

        });


    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory)
            throws BeansException {

    }
}
