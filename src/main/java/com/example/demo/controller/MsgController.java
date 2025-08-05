package com.example.demo.controller;


import com.example.demo.model.Person;
import lombok.extern.slf4j.Slf4j;

import org.apache.activemq.artemis.api.core.QueueConfiguration;
import org.apache.activemq.artemis.api.core.RoutingType;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSession.QueueQuery;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.messaging.MessageHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.PostConstruct;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Session;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class MsgController {

    private AtomicInteger msgId = new AtomicInteger();

    @Autowired
    private JmsTemplate jmsTemplate;

    @Value("${spring.artemis.broker-url}")
    String BROKER_URL;

    @Value("${spring.artemis.user}")
    String BROKER_USERNAME;

    @Value("${spring.artemis.password}")
    String BROKER_PASSWORD;

    @Value("${spring.artemis.queue}")
    private String queue;

    private List<Person> persons = new ArrayList<>();

    @GetMapping(value = "/get-message", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Person> getMessage(){
        return persons;
    }

    @PostMapping(value = "/send-message", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Person setMessage(@RequestBody Person person){
        log.info("Input value: {}", person);
        //Setting priority based on age
        if(person.getAge()>60) {
            jmsTemplate.convertAndSend(queue, person, new MessagePostProcessor() {
                @Override
                public Message postProcessMessage(Message message) throws JMSException {
                    message.setStringProperty("msgId", String.valueOf(msgId.getAndIncrement()));
                    return message;
                }
            });
        }else {
            jmsTemplate.convertAndSend(queue, person);
        }
        return person;
    }

    // Create queue when application starts
    @PostConstruct
    public void init() {
        List<String> brokerUrls = extractBrokerUrls(BROKER_URL);
        for (String url : brokerUrls) {
            createQueue(url, queue, true);
        }
    }

    private void createQueue(String brokerUrl, String queueName, boolean durable) {
        try (
                ServerLocator locator = ActiveMQClient.createServerLocator(brokerUrl);
                ClientSessionFactory factory = locator.createSessionFactory();
                ClientSession session = factory.createSession(BROKER_USERNAME, BROKER_PASSWORD, false, true, true, false, 0)) {
            SimpleString address = SimpleString.toSimpleString(queueName);
            SimpleString name = SimpleString.toSimpleString(queueName);

            QueueQuery query = session.queueQuery(name);
            if (!query.isExists()) {
                QueueConfiguration queueConfig = new QueueConfiguration(name)
                        .setAddress(address)
                        .setDurable(durable)
                        .setRoutingType(RoutingType.ANYCAST); 

                session.createQueue(queueConfig);
                log.info("Queue '" + queueName + "' created.");
            } else {
                log.info("Queue '" + queueName + "' already exists.");
            }

        } catch (Exception e) {
            log.error("Failed to create queue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static List<String> extractBrokerUrls(String brokerUri) {
        int start = brokerUri.indexOf('(');
        int end = brokerUri.indexOf(')');

        if (start >= 0 && end > start) {
            // Extract broker URLs
            String uris = brokerUri.substring(start + 1, end);

            // Extract query parameters (after '?')
            int queryStart = brokerUri.indexOf('?', end);
            final String query = (queryStart > 0) ? brokerUri.substring(queryStart) : "";

            // Combine each URL with query
            return Arrays.stream(uris.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(url -> url + query)
                    .collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException("Invalid brokerUri format: missing ( )");
        }
    }

    //TODO: Property based filtering
/*    @JmsListener(destination = "test.destination::test.queue")
    public void receiveMessage(Person person){
        persons.add(person);
        log.info("Message Received: {}", person);
    }*/
}
