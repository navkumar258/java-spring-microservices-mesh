package com.example.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class EmailService {

	private final JavaMailSender javaMailSender;

	public EmailService(JavaMailSender javaMailSender) {
		this.javaMailSender = javaMailSender;
	}

	public void setMailDetailsForSend(final String payload, final String email) throws MessagingException {
		final MimeMessage mail = javaMailSender.createMimeMessage();
		final MimeMessageHelper helper = new MimeMessageHelper(
				mail,
				MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
				StandardCharsets.UTF_8.name()
		);

		helper.setTo(email);
		helper.setSubject("Notification");
		helper.setText(payload, true);

		javaMailSender.send(mail);
	}
}