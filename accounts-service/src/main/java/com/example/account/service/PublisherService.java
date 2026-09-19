package com.example.account.service;

import com.example.account.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class PublisherService {

	private static final Logger log = LoggerFactory.getLogger(PublisherService.class);

	private final RabbitTemplate rabbitTemplate;
	private final TopicExchange exchange;

	public PublisherService(RabbitTemplate rabbitTemplate, TopicExchange exchange) {
		this.rabbitTemplate = rabbitTemplate;
		this.exchange = exchange;
	}

	public void publishUserCreateEvent(String routingKey, User message) {
		log.info("Publishing user creation message: {}", message);
		rabbitTemplate.convertAndSend(exchange.getName(), routingKey, message);
	}
}
