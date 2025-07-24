package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringJmsApplication {
	static {
		System.setProperty("org.apache.activemq.ssl.trustStore","/opt/master-broker/etc/wildfly.jks");
		System.setProperty("org.apache.activemq.ssl.trustStorePassword","jboss@123");
	}
	public static void main(String[] args) {
		SpringApplication.run(SpringJmsApplication.class, args);
	}

}
