package com.example.account.dto;

import com.example.account.model.User;

public record UserResponse(
		Long id,
		String name,
		String email,
		String mobile,
		boolean emailVerified
) {
	public static UserResponse fromEntity(User user) {
		return new UserResponse(
				user.getId(),
				user.getName(),
				user.getEmail(),
				user.getMobile(),
				user.isEmailVerified()
		);
	}
}
