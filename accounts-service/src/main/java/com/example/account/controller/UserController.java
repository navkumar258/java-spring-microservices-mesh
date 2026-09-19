package com.example.account.controller;

import com.example.account.config.AppProperties;
import com.example.account.dto.UserCreateRequest;
import com.example.account.dto.UserResponse;
import com.example.account.exception.UserAlreadyExistsException;
import com.example.account.exception.UserNotFoundException;
import com.example.account.model.User;
import com.example.account.service.PublisherService;
import com.example.account.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

	private static final Logger log = LoggerFactory.getLogger(UserController.class);

	private final UserService userService;
	private final PublisherService publisherService;
	private final AppProperties appProperties;

	public UserController(UserService userService, PublisherService publisherService, AppProperties appProperties) {
		this.userService = userService;
		this.publisherService = publisherService;
		this.appProperties = appProperties;
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public UserResponse createUser(@Valid @RequestBody UserCreateRequest request) {
		log.info("Request received to create user with email: {}", request.email());

		if (userService.existsByEmail(request.email())) {
			throw new UserAlreadyExistsException("Email already registered: " + request.email());
		}

		if (userService.existsByMobile(request.mobile())) {
			throw new UserAlreadyExistsException("Mobile number already registered: " + request.mobile());
		}

		User user = request.toEntity();
		user.setEmailVerified(false);

		User savedUser = userService.saveUser(user);
		publisherService.publishUserCreateEvent(appProperties.creation().routingKey(), savedUser);

		return UserResponse.fromEntity(savedUser);
	}

	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public UserResponse getUserDetails(@PathVariable("id") long userId) {
		return userService.findById(userId)
				.map(UserResponse::fromEntity)
				.orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
	}
}