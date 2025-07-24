package com.example.demo.bean;


import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.jms.JMSException;

@Configuration
public class AmqBean {

    @Value("${spring.artemis.broker-url}")
    String BROKER_URL;

    @Value("${spring.artemis.user}")
    String BROKER_USERNAME;

    @Value("${spring.artemis.password}")
    String BROKER_PASSWORD;

    @Bean
    public ActiveMQConnectionFactory connectionFactory() throws JMSException {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
        //connectionFactory.setTrustAllPackages(true);
        connectionFactory.setBrokerURL(BROKER_URL);
        connectionFactory.setUser(BROKER_USERNAME);
        connectionFactory.setPassword(BROKER_PASSWORD);
        return connectionFactory;
    }
}
