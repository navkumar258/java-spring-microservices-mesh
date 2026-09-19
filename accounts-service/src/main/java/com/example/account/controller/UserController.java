package com.example.account.controller;

import com.example.account.config.AppProperties;
import com.example.account.model.User;
import com.example.account.service.PublisherService;
import com.example.account.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
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

	// Strongly-typed immutable API response record
	public record ApiResponse<T>(String status, T message) {
		public static <T> ApiResponse<T> success(T data) {
			return new ApiResponse<>("success", data);
		}

		public static ApiResponse<String> error(String errorMessage) {
			return new ApiResponse<>("error", errorMessage);
		}
	}

	@PostMapping(path = "/users", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponse<?>> createUser(@RequestBody User user) {
		log.info("Create user request - {}", user);

		if (userService.existsByEmail(user.getEmail())) {
			return ResponseEntity.ok(ApiResponse.error("user email already exists, please change and try again"));
		}

		if (userService.existsByMobile(user.getMobile())) {
			return ResponseEntity.ok(ApiResponse.error("user mobile already exists, please change and try again"));
		}

		user.setEmailVerified(false);
		var savedUser = userService.saveUser(user);

		publisherService.publishUserCreateEvent(appProperties.creation().routingKey(), savedUser);

		return ResponseEntity.ok(ApiResponse.success("User created successfully!!!"));
	}

	@GetMapping(path = "/users/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Object getUserDetails(@PathVariable(name = "id") long userId) {
		return userService.findById(userId)
				.map(user -> ResponseEntity.ok(ApiResponse.success(user)))
				.orElseGet(() -> ResponseEntity.notFound().build());
	}
}
