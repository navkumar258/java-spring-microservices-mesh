package com.example.account.controller;

import com.example.account.config.AppProperties;
import com.example.account.dto.UserCreateRequest;
import com.example.account.dto.UserResponse;
import com.example.account.exception.GlobalExceptionHandler;
import com.example.account.exception.UserAlreadyExistsException;
import com.example.account.service.PublisherService;
import com.example.account.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private UserService userService;

	@MockitoBean
	private PublisherService publisherService;

	@MockitoBean
	private AppProperties appProperties;

	@MockitoBean
	private AppProperties.Creation creation;

	private static final String BASE_PATH = "/api/v1/users";
	private static final String ROUTING_KEY = "user.creation.key";

	@BeforeEach
	void setUp() {
		given(appProperties.creation()).willReturn(creation);
		given(creation.routingKey()).willReturn(ROUTING_KEY);
	}

	@Nested
	@DisplayName("POST " + BASE_PATH)
	class CreateUserTests {

		@Test
		@DisplayName("Should create user successfully and return 201 Created with UserResponse")
		void createUser_Success() throws Exception {
			// Given
			var request = new UserCreateRequest(
					"Jane Doe",
					"jane.doe@example.com",
					"+1234567890",
					LocalDate.of(1995, 5, 20)
			);

			var response = new UserResponse(
					1L,
					"Jane Doe",
					"jane.doe@example.com",
					"+1234567890",
					"1995-05-20",
					false
			);

			given(userService.registerUser(any(UserCreateRequest.class))).willReturn(response);
			doNothing().when(publisherService).publishUserCreateEvent(eq(ROUTING_KEY), any(UserResponse.class));
			// When & Then
			mockMvc.perform(post(BASE_PATH)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isCreated())
					.andExpect(content().contentType(MediaType.APPLICATION_JSON))
					.andExpect(jsonPath("$.id").value(1))
					.andExpect(jsonPath("$.name").value("Jane Doe"))
					.andExpect(jsonPath("$.email").value("jane.doe@example.com"))
					.andExpect(jsonPath("$.mobile").value("+1234567890"))
					.andExpect(jsonPath("$.dob").value("1995-05-20"))
					.andExpect(jsonPath("$.emailVerified").value(false));

			verify(userService).registerUser(request);
			verify(publisherService).publishUserCreateEvent(ROUTING_KEY, response);
		}

		@Test
		@DisplayName("Should return 400 Bad Request ProblemDetail when validation fails")
		void createUser_ValidationError() throws Exception {
			// Given - Invalid email and empty name
			var invalidRequest = new UserCreateRequest(
					"",
					"invalid-email-format",
					"+1234567890",
					LocalDate.of(1995, 5, 20)
			);

			// When & Then
			mockMvc.perform(post(BASE_PATH)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(invalidRequest)))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.title").value("Invalid Request Payload"))
					.andExpect(jsonPath("$.status").value(400))
					.andExpect(jsonPath("$.errors").isArray());

			verifyNoInteractions(userService);
			verifyNoInteractions(publisherService);
		}

		@Test
		@DisplayName("Should return 409 Conflict ProblemDetail when email/mobile already exists")
		void createUser_ConflictError() throws Exception {
			// Given
			var request = new UserCreateRequest(
					"Jane Doe",
					"existing@example.com",
					"+1234567890",
					LocalDate.of(1995, 5, 20)
			);

			given(userService.registerUser(any(UserCreateRequest.class)))
					.willThrow(new UserAlreadyExistsException("Email already registered: existing@example.com"));

			// When & Then
			mockMvc.perform(post(BASE_PATH)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isConflict())
					.andExpect(jsonPath("$.title").value("Domain Rule Violation"))
					.andExpect(jsonPath("$.status").value(409))
					.andExpect(jsonPath("$.detail").value("Email already registered: existing@example.com"));

			verifyNoInteractions(publisherService);
		}
	}

	@Nested
	@DisplayName("GET " + BASE_PATH + "/{id}")
	class GetUserDetailsTests {

		@Test
		@DisplayName("Should return 200 OK with UserResponse when user exists")
		void getUserDetails_Success() throws Exception {
			// Given
			long userId = 1L;
			var response = new UserResponse(
					userId,
					"John Doe",
					"john.doe@example.com",
					"+1987654321",
					"1990-01-01",
					true
			);

			given(userService.findById(userId)).willReturn(Optional.of(response));

			// When & Then
			mockMvc.perform(get(BASE_PATH + "/{id}", userId)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id").value(userId))
					.andExpect(jsonPath("$.name").value("John Doe"))
					.andExpect(jsonPath("$.email").value("john.doe@example.com"))
					.andExpect(jsonPath("$.emailVerified").value(true));

			verify(userService).findById(userId);
		}

		@Test
		@DisplayName("Should return 404 Not Found ProblemDetail when user does not exist")
		void getUserDetails_NotFound() throws Exception {
			// Given
			long nonExistentId = 999L;
			given(userService.findById(nonExistentId)).willReturn(Optional.empty());

			// When & Then
			mockMvc.perform(get(BASE_PATH + "/{id}", nonExistentId)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isNotFound())
					.andExpect(jsonPath("$.title").value("Domain Rule Violation"))
					.andExpect(jsonPath("$.status").value(404))
					.andExpect(jsonPath("$.detail").value("User not found with ID: " + nonExistentId));

			verify(userService).findById(nonExistentId);
		}
	}
}