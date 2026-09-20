package com.example.account.service;

import com.example.account.dto.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PublisherServiceTest {

	@Mock
	private RabbitTemplate rabbitTemplate;

	@Mock
	private TopicExchange exchange;

	@InjectMocks
	private PublisherService publisherService;

	@Test
	@DisplayName("Should convert and send UserResponse message to RabbitMQ topic exchange")
	void publishUserCreateEvent_Success() {
		// Given
		String exchangeName = "account.exchange";
		String routingKey = "user.created.v1";

		var userResponse = new UserResponse(
				101L,
				"Jane Doe",
				"jane.doe@example.com",
				"+1234567890",
				"1995-5-20",
				false
		);

		given(exchange.getName()).willReturn(exchangeName);

		// When
		publisherService.publishUserCreateEvent(routingKey, userResponse);

		// Then
		verify(exchange).getName();
		verify(rabbitTemplate).convertAndSend(exchangeName, routingKey, userResponse);
	}
}