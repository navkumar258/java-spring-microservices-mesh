package com.example.notification.service;

import com.example.notification.dto.User;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsumerServiceTest {

	@Mock
	private EmailService emailService;

	@InjectMocks
	private ConsumerService consumerService;

	@Nested
	@DisplayName("receive(User)")
	class ReceiveTests {

		@Test
		@DisplayName("Should invoke email service to send welcome email when message is received")
		void receive_Success() throws MessagingException {
			// Given
			var user = new User("Alice Smith", "alice@example.com", 123456789L, LocalDate.EPOCH, false);
			doNothing().when(emailService).setMailDetailsForSend(anyString(), anyString());

			// When
			consumerService.receive(user);

			// Then
			verify(emailService, times(1)).setMailDetailsForSend(user.toString(), "abcd@mail.com");
			verifyNoMoreInteractions(emailService);
		}

		@Test
		@DisplayName("Should catch MessagingException and log error without throwing exception")
		void receive_MessagingExceptionHandled() throws MessagingException {
			// Given
			var user = new User("Bob Jones", "bob@example.com", 123456789L, LocalDate.EPOCH, false);
			doThrow(new MessagingException("SMTP Server unavailable"))
					.when(emailService)
					.setMailDetailsForSend(anyString(), anyString());

			// When & Then - Should not throw MessagingException out of receive method
			consumerService.receive(user);

			// Verify the method attempted to send the email
			verify(emailService, times(1)).setMailDetailsForSend(user.toString(), "abcd@mail.com");
			verifyNoMoreInteractions(emailService);
		}
	}
}