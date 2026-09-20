package com.example.account.controller;

import com.example.account.config.AppProperties;
import com.example.account.dto.UserCreateRequest;
import com.example.account.dto.UserResponse;
import com.example.account.exception.UserNotFoundException;
import com.example.account.service.PublisherService;
import com.example.account.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

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
		UserResponse response = userService.registerUser(request);

		// Publish message event using appProperties routing key
		publisherService.publishUserCreateEvent(appProperties.creation().routingKey(), response);

		return response;
	}

	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public UserResponse getUserDetails(@PathVariable("id") long userId) {
		return userService.findById(userId)
				.orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
	}
}