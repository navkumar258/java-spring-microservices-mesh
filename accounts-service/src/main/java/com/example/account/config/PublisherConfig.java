package com.example.account.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AppProperties.class)
public class PublisherConfig {

	private final AppProperties appProperties;

	public PublisherConfig(AppProperties appProperties) {
		this.appProperties = appProperties;
	}

	@Bean
	public TopicExchange eventExchange() {
		return new TopicExchange(appProperties.exchangeName());
	}

	@Bean
	public JacksonJsonMessageConverter producerJacksonJsonMessageConverter() {
		return new JacksonJsonMessageConverter();
	}

	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
	                                     JacksonJsonMessageConverter messageConverter) {
		var rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(messageConverter);
		return rabbitTemplate;
	}
}
