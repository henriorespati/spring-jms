# Sample SpringBoot with AMQ Broker
It is a simple Springboot based application with the integration of AMQ Broker over OpenWire protocol. This application also has the feature of consuming the messages based on the set property e.g. a person with age greater than 60. 
## Configuring the application 
Configure the following properties as per the AMQ Broker setup. 
````properties
spring.activemq.broker-url=tcp://localhost:61616
spring.activemq.user=admin
spring.activemq.password=secret
spring.activemq.queue=spring-queue
````
## Building the application
````shell
mvn clean package
````

## Run the application
````shell
./mvnw spring-boot:run
````

## Test the Application
- Send a message over a curl command
    ````shell
    curl --location 'http://localhost:8080/send-message' --header 'Content-Type: application/json' --data '{"name":"Sid", "age": 62 }'
    ````

- Check the message using get api
    ````shell
    curl --location 'http://localhost:8080/get-message' --header 'Content-Type: application/json' | jq
    [
      {
        "name": "Sid",
        "age": 62
      }
    ]
    ````
  **Note:** The get api will return the result of the person whose age is above 60. 

