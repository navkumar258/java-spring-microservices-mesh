# java-spring-microservices-mesh

Java-Spring Cloud based event driven microservices based on [this](https://github.com/sbruksha/event-driven-microservices-platform) repository and [this](https://www.linkedin.com/pulse/event-driven-microservices-architecture-using-spring-cloud-bruksha/) article.

## Available services(each has it's own branch):
  1. **accounts-service:** User registration, authentication and management micro-service.Also, a producer for RabbitMQ user exchange
  2. **appointment-service:** Create and manage appointments. Producer for the appointment exchange
  3. **eureka-server:** Service discovery and registration micro-service
  4. **gateway-service:** service acting as a application-gateway i.e. front door of all user requests with dynamic routing, monitoring, resilience and security
  5. **notification-service:** Consumer of RabbitMQ queues for sending email as well as sms notifications on corresponding events 
  6. **store-service:** Used to create, retrieve and update store related info
  
**Note:** This is a work-in-progress and only available as a reference for creating highly scalable and fault-tolerant micro-services using java and spring cloud integration
