package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringJmsApplication {
	// static {
	// 	System.setProperty("org.apache.activemq.ssl.trustStore","/Users/sidde/Downloads/Software/apache-artemis-2.33.0.redhat-00016/master-broker/etc/wildfly.jks");
	// 	System.setProperty("org.apache.activemq.ssl.trustStorePassword","jboss@123");
	// 	System.setProperty("org.apache.activemq.ssl.keyStore","/Users/sidde/Downloads/Software/apache-artemis-2.33.0.redhat-00016/master-broker/etc/wildfly.jks");
	// 	System.setProperty("org.apache.activemq.ssl.keyStorePassword","jboss@123");
	// }
	public static void main(String[] args) {
		SpringApplication.run(SpringJmsApplication.class, args);
	}
}
