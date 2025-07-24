package com.example.demo.controller;


import com.example.demo.model.Person;
import lombok.extern.slf4j.Slf4j;
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

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@Slf4j
public class MsgController {

    private AtomicInteger msgId = new AtomicInteger();

    @Autowired
    private JmsTemplate jmsTemplate;

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

    //TODO: Property based filtering
    @JmsListener(destination = "spring-queue", selector = "Priority = 'Q1'")
    public void receiveMessage(Person person){
        persons.add(person);
        log.info("Message Received: {}", person);
    }
}
