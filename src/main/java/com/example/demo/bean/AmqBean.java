package com.example.demo.bean;


import com.example.demo.AmqConsumer;
import jakarta.jms.ConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.jms.JMSException;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.MessageListenerContainer;

@Configuration
public class AmqBean {

    @Value("${spring.artemis.broker-1-url}")
    String BROKER_ONE_URL;
    @Value("${spring.artemis.broker-2-url}")
    String BROKER_TWO_URL;

    @Value("${spring.artemis.user}")
    String BROKER_USERNAME;

    @Value("${spring.artemis.password}")
    String BROKER_PASSWORD;

    @Value("${spring.artemis.queue}")
    private String queue;

    @Bean
    public ActiveMQConnectionFactory connectionFactory() throws JMSException {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
        //connectionFactory.setTrustAllPackages(true);
        connectionFactory.setBrokerURL(BROKER_ONE_URL);
        connectionFactory.setUser(BROKER_USERNAME);
        connectionFactory.setPassword(BROKER_PASSWORD);
        return connectionFactory;
    }

    @Bean
    public ActiveMQConnectionFactory listenerConnectionFactory() throws JMSException {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
        //connectionFactory.setTrustAllPackages(true);
        connectionFactory.setBrokerURL(BROKER_TWO_URL);
        connectionFactory.setUser(BROKER_USERNAME);
        connectionFactory.setPassword(BROKER_PASSWORD);
        return connectionFactory;
    }

    @Bean
    public JmsTemplate jmsTemplate() throws JMSException{
        JmsTemplate template = new JmsTemplate();
        template.setConnectionFactory(connectionFactory());
        return template;
    }

    /*@Bean
    public JmsListenerContainerFactory jmsListenerContainerFactory(@Qualifier("connectionFactory") ConnectionFactory factory) throws JMSException {
        DefaultJmsListenerContainerFactory containerFactory = new DefaultJmsListenerContainerFactory();
        containerFactory.setConnectionFactory(factory);
        containerFactory.setConcurrency("1-5");
        return containerFactory;
    }*/

    @Bean
    public MessageListenerContainer artemisApexTestContainer(ActiveMQConnectionFactory connectionFactory
            , AmqConsumer amqConsumer) throws JMSException {
        DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
        container.setConnectionFactory(listenerConnectionFactory());
        container.setConcurrentConsumers(10);
        //container.setConcurrency("3-10");
        container.setDestinationName(queue);
        //container.setAutoStartup(true);
        container.setMessageListener(amqConsumer);
        return container;
    }
}
