package com.example.account.dto;

import com.example.account.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserCreateRequest(
		@NotBlank(message = "Name is required")
		String name,

		@NotBlank(message = "Email is required")
		@Email(message = "Invalid email format")
		String email,

		@NotBlank(message = "Mobile number is required")
		@Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid E.164 mobile phone format")
		String mobile
) {
	public User toEntity() {
		User user = new User();
		user.setName(this.name);
		user.setEmail(this.email);
		user.setMobile(this.mobile);
		return user;
	}
}