package com.example.account.integration;

import com.example.account.dto.UserCreateRequest;
import com.example.account.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class UserControllerIntegrationTest {

	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18-alpine");

	@Container
	@ServiceConnection
	static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:4.3.6-management-alpine");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	private static final String BASE_PATH = "/api/v1/users";

	@BeforeEach
	void setUp() {
		userRepository.deleteAll();
	}

	@Nested
	@DisplayName("POST " + BASE_PATH)
	class CreateUserIntegrationTests {

		@Test
		@DisplayName("Should create user in database and return 201 Created with string dob")
		void createUser_Success() throws Exception {
			// Given
			var request = new UserCreateRequest(
					"Alice Smith",
					"alice.smith@example.com",
					"+19876543210",
					LocalDate.of(1992, 8, 15)
			);

			// When & Then
			mockMvc.perform(post(BASE_PATH)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isCreated())
					.andExpect(content().contentType(MediaType.APPLICATION_JSON))
					.andExpect(jsonPath("$.id").exists())
					.andExpect(jsonPath("$.name").value("Alice Smith"))
					.andExpect(jsonPath("$.email").value("alice.smith@example.com"))
					.andExpect(jsonPath("$.mobile").value("+19876543210"))
					.andExpect(jsonPath("$.dob").value("1992-08-15")) // Verified as String
					.andExpect(jsonPath("$.emailVerified").value(false));

			// Verify persistence in actual database
			var savedUser = userRepository.findByEmail("alice.smith@example.com");
			assertThat(savedUser).isPresent();
			assertThat(savedUser.get().getName()).isEqualTo("Alice Smith");
		}

		@Test
		@DisplayName("Should return 409 Conflict when attempting to register duplicate email")
		void createUser_DuplicateEmail() throws Exception {
			// Given
			var firstRequest = new UserCreateRequest(
					"Alice Smith",
					"duplicate@example.com",
					"+19876543210",
					LocalDate.of(1992, 8, 15)
			);

			mockMvc.perform(post(BASE_PATH)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(firstRequest)))
					.andExpect(status().isCreated());

			var duplicateRequest = new UserCreateRequest(
					"Bob Smith",
					"duplicate@example.com",
					"+19999999999",
					LocalDate.of(1990, 1, 1)
			);

			// When & Then
			mockMvc.perform(post(BASE_PATH)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(duplicateRequest)))
					.andExpect(status().isConflict())
					.andExpect(jsonPath("$.title").value("Domain Rule Violation"))
					.andExpect(jsonPath("$.status").value(409))
					.andExpect(jsonPath("$.detail").value("Email already registered: duplicate@example.com"));
		}

		@Test
		@DisplayName("Should return 400 Bad Request when JSON payload validation fails")
		void createUser_ValidationError() throws Exception {
			// Given - Invalid email format
			var invalidRequest = new UserCreateRequest(
					"Alice Smith",
					"not-an-email",
					"+19876543210",
					LocalDate.of(1992, 8, 15)
			);

			// When & Then
			mockMvc.perform(post(BASE_PATH)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(invalidRequest)))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.title").value("Invalid Request Payload"))
					.andExpect(jsonPath("$.status").value(400))
					.andExpect(jsonPath("$.errors").isArray());
		}
	}

	@Nested
	@DisplayName("GET " + BASE_PATH + "/{id}")
	class GetUserDetailsIntegrationTests {

		@Test
		@DisplayName("Should return user details when record exists in database")
		void getUserDetails_Success() throws Exception {
			// Given
			var request = new UserCreateRequest(
					"Charlie Brown",
					"charlie.brown@example.com",
					"+15551234567",
					LocalDate.of(1988, 3, 10)
			);

			String responseString = mockMvc.perform(post(BASE_PATH)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isCreated())
					.andReturn().getResponse().getContentAsString();

			long createdUserId = objectMapper.readTree(responseString).get("id").asLong();

			// When & Then
			mockMvc.perform(get(BASE_PATH + "/{id}", createdUserId)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id").value(createdUserId))
					.andExpect(jsonPath("$.name").value("Charlie Brown"))
					.andExpect(jsonPath("$.email").value("charlie.brown@example.com"))
					.andExpect(jsonPath("$.dob").value("1988-03-10"));
		}

		@Test
		@DisplayName("Should return 404 Not Found ProblemDetail when user ID does not exist")
		void getUserDetails_NotFound() throws Exception {
			long nonExistentId = 999999L;

			mockMvc.perform(get(BASE_PATH + "/{id}", nonExistentId)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isNotFound())
					.andExpect(jsonPath("$.title").value("Domain Rule Violation"))
					.andExpect(jsonPath("$.status").value(404))
					.andExpect(jsonPath("$.detail").value("User not found with ID: " + nonExistentId));
		}
	}
}