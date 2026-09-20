package com.example.account.service;

import com.example.account.dto.UserCreateRequest;
import com.example.account.dto.UserResponse;
import com.example.account.exception.UserAlreadyExistsException;
import com.example.account.model.User;
import com.example.account.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserService {

	private static final Logger log = LoggerFactory.getLogger(UserService.class);

	private final UserRepository userRepository;

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Transactional
	public UserResponse registerUser(UserCreateRequest request) {
		log.info("Registering new user with email: {}", request.email());

		if (userRepository.existsByEmail(request.email())) {
			throw new UserAlreadyExistsException("Email already registered: " + request.email());
		}

		if (userRepository.existsByMobile(request.mobile())) {
			throw new UserAlreadyExistsException("Mobile number already registered: " + request.mobile());
		}

		User user = request.toEntity();
		user.setEmailVerified(false);

		User savedUser = userRepository.save(user);
		log.info("User registered successfully with ID: {}", savedUser.getId());

		return UserResponse.fromEntity(savedUser);
	}

	public Optional<UserResponse> findById(Long id) {
		log.debug("Fetching user details for ID: {}", id);
		return userRepository.findById(id)
				.map(UserResponse::fromEntity);
	}
}