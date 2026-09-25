package com.example.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

	@Mock
	private JavaMailSender javaMailSender;

	@InjectMocks
	private EmailService emailService;

	private MimeMessage mimeMessage;

	@BeforeEach
	void setUp() {
		// Instantiate a real MimeMessage with an empty Jakarta Mail Session so MimeMessageHelper works cleanly
		mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
	}

	@Nested
	@DisplayName("setMailDetailsForSend(String, String)")
	class SetMailDetailsForSendTests {

		@Test
		@DisplayName("Should build mime message and delegate to JavaMailSender.send()")
		void setMailDetailsForSend_Success() throws MessagingException {
			// Given
			String payload = "<h1>Welcome User!</h1>";
			String targetEmail = "test@example.com";

			when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

			// When
			emailService.setMailDetailsForSend(payload, targetEmail);

			// Then
			verify(javaMailSender, times(1)).createMimeMessage();
			verify(javaMailSender, times(1)).send(mimeMessage);
			verifyNoMoreInteractions(javaMailSender);
		}

		@Test
		@DisplayName("Should propagate MessagingException when javaMailSender.send fails")
		void setMailDetailsForSend_MailSenderException() {
			// Given
			String payload = "<h1>Welcome User!</h1>";
			String targetEmail = "test@example.com";

			when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
			doThrow(new RuntimeException("Mail transport failed"))
					.when(javaMailSender).send(any(MimeMessage.class));

			// When & Then
			assertThrows(RuntimeException.class, () ->
					emailService.setMailDetailsForSend(payload, targetEmail)
			);

			verify(javaMailSender, times(1)).createMimeMessage();
			verify(javaMailSender, times(1)).send(mimeMessage);
		}
	}
}