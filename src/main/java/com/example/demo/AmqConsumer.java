package com.example.demo;

import com.example.demo.model.Person;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AmqConsumer implements MessageListener {
    @Override
    public void onMessage(Message message) {
        log.info("start receive msg");
        if (message == null) {
            log.info("message is null");
            return;
        }
        try {
            Object body = message.getBody(Object.class);
            if (body instanceof Person){
                Person person = (Person) body;
                log.info("Received message: {}", person);
            }
        }catch (Exception e) {
            log.info("handler error", e);
        }
    }
}
