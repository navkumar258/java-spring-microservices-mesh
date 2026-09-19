package com.example.notification.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AppProperties.class)
public class ConsumerConfig {

	private final AppProperties appProperties;

	public ConsumerConfig(AppProperties appProperties) {
		this.appProperties = appProperties;
	}

	@Bean
	public TopicExchange eventExchange() {
		return new TopicExchange(appProperties.exchangeName());
	}

	@Bean
	public Queue queue() {
		return QueueBuilder.durable(appProperties.creation().queueName()).build();
	}

	@Bean
	public Binding binding(Queue queue, TopicExchange exchange) {
		return BindingBuilder.bind(queue)
				.to(exchange)
				.with(appProperties.creation().routingKey());
	}

	@Bean
	public JacksonJsonMessageConverter jackson2JsonMessageConverter() {
		return new JacksonJsonMessageConverter();
	}
}