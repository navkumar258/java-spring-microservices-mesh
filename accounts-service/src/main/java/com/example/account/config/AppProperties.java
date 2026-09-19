package com.example.account.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rabbitmq.user")
public record AppProperties(
		String exchangeName,
		Creation creation
) {
	public record Creation(
			String routingKey
	) {}
}