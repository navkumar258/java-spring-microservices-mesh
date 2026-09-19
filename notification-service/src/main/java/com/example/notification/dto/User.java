package com.example.notification.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record User(
		String name,
		String email,
		Long mobile,
		LocalDate dob,
		boolean emailVerified
) {}