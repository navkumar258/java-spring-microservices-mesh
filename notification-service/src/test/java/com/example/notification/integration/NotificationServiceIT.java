package com.example.notification.integration;

import com.example.notification.dto.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import tools.jackson.databind.JsonNode;

import java.time.Duration;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@ActiveProfiles("test")
class NotificationServiceIT {

	@Container
	@ServiceConnection
	static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:4.3.6-management-alpine");

	// Mailpit SMTP Container for catching outgoing emails without mocking JavaMailSender
	@Container
	static GenericContainer<?> mailpit = new GenericContainer<>("axllent/mailpit:v1.31.2")
			.withExposedPorts(1025, 8025) // 1025: SMTP, 8025: Web UI / API
			.waitingFor(Wait.forHttp("/").forPort(8025));

	@DynamicPropertySource
	static void configureMailProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.mail.host", mailpit::getHost);
		registry.add("spring.mail.port", () -> mailpit.getMappedPort(1025));
		registry.add("spring.mail.protocol", () -> "smtp");
	}

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Value("${rabbitmq.user.creation.queue-name}")
	private String queueName;

	@BeforeEach
	void setUp() {
		// Ensure queue is purged before running test
		rabbitTemplate.execute(channel -> {
			channel.queuePurge(queueName);
			return null;
		});
	}

	@Test
	@DisplayName("Should receive user from RabbitMQ and dispatch real MIME email to SMTP server")
	void endToEnd_MessageToEmailFlow() {
		// Given
		var testUser = new User( "Integration User", "abcd@mail.com", 123456789L, LocalDate.EPOCH, false);

		// When - Publish real message directly to the RabbitMQ queue
		rabbitTemplate.convertAndSend(queueName, testUser);

		// Then - Use Awaitility to wait for asynchronous consumer processing & email arrival
		await().atMost(Duration.ofSeconds(10))
				.pollInterval(Duration.ofMillis(500))
				.untilAsserted(() -> {
					int emailCount = fetchEmailCountFromMailpit();
					assertThat(emailCount).isEqualTo(1);
				});
	}

	private int fetchEmailCountFromMailpit() {
		String apiUrl = "http://" + mailpit.getHost() + ":" + mailpit.getMappedPort(8025) + "/api/v1/messages";
		RestTemplate restTemplate = new RestTemplate();
		JsonNode response = restTemplate.getForObject(apiUrl, JsonNode.class);
		return response != null && response.has("total") ? response.get("total").asInt() : 0;
	}
}