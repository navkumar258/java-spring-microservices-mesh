package com.example.account.service;

import com.example.account.dto.UserCreateRequest;
import com.example.account.dto.UserResponse;
import com.example.account.exception.UserAlreadyExistsException;
import com.example.account.model.User;
import com.example.account.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserService userService;

	@Nested
	@DisplayName("registerUser(UserCreateRequest)")
	class RegisterUserTests {

		@Test
		@DisplayName("Should successfully register a new user when email and mobile are unique")
		void registerUser_Success() {
			// Given
			var request = new UserCreateRequest(
					"Jane Doe",
					"jane.doe@example.com",
					"+1234567890",
					LocalDate.of(1995, 5, 20)
			);

			var savedUser = new User(
					1L,
					"Jane Doe",
					"jane.doe@example.com",
					"+1234567890",
					LocalDate.of(1995, 5, 20),
					false
			);

			given(userRepository.existsByEmail(request.email())).willReturn(false);
			given(userRepository.existsByMobile(request.mobile())).willReturn(false);
			given(userRepository.save(any(User.class))).willReturn(savedUser);

			// When
			UserResponse response = userService.registerUser(request);

			// Then
			assertThat(response).isNotNull();
			assertThat(response.id()).isEqualTo(1L);
			assertThat(response.name()).isEqualTo("Jane Doe");
			assertThat(response.email()).isEqualTo("jane.doe@example.com");
			assertThat(response.mobile()).isEqualTo("+1234567890");
			assertThat(response.dob()).isEqualTo("1995-05-20");
			assertThat(response.emailVerified()).isFalse();

			verify(userRepository).existsByEmail(request.email());
			verify(userRepository).existsByMobile(request.mobile());
			verify(userRepository).save(any(User.class));
		}

		@Test
		@DisplayName("Should throw UserAlreadyExistsException when email is already registered")
		void registerUser_EmailAlreadyExists() {
			// Given
			var request = new UserCreateRequest(
					"Jane Doe",
					"existing@example.com",
					"+1234567890",
					LocalDate.of(1995, 5, 20)
			);

			given(userRepository.existsByEmail(request.email())).willReturn(true);

			// When & Then
			assertThatThrownBy(() -> userService.registerUser(request))
					.isInstanceOf(UserAlreadyExistsException.class)
					.hasMessage("Email already registered: existing@example.com");

			verify(userRepository).existsByEmail(request.email());
			verify(userRepository, never()).existsByMobile(any());
			verify(userRepository, never()).save(any());
		}

		@Test
		@DisplayName("Should throw UserAlreadyExistsException when mobile number is already registered")
		void registerUser_MobileAlreadyExists() {
			// Given
			var request = new UserCreateRequest(
					"Jane Doe",
					"jane.doe@example.com",
					"+1234567890",
					LocalDate.of(1995, 5, 20)
			);

			given(userRepository.existsByEmail(request.email())).willReturn(false);
			given(userRepository.existsByMobile(request.mobile())).willReturn(true);

			// When & Then
			assertThatThrownBy(() -> userService.registerUser(request))
					.isInstanceOf(UserAlreadyExistsException.class)
					.hasMessage("Mobile number already registered: +1234567890");

			verify(userRepository).existsByEmail(request.email());
			verify(userRepository).existsByMobile(request.mobile());
			verify(userRepository, never()).save(any());
		}
	}

	@Nested
	@DisplayName("findById(Long)")
	class FindByIdTests {

		@Test
		@DisplayName("Should return Optional containing UserResponse when user exists")
		void findById_Success() {
			// Given
			Long userId = 1L;
			var user = new User(
					userId,
					"John Doe",
					"john.doe@example.com",
					"+1987654321",
					LocalDate.of(1990, 1, 1),
					true
			);

			given(userRepository.findById(userId)).willReturn(Optional.of(user));

			// When
			Optional<UserResponse> result = userService.findById(userId);

			// Then
			assertThat(result).isPresent();
			assertThat(result.get().id()).isEqualTo(userId);
			assertThat(result.get().email()).isEqualTo("john.doe@example.com");
			assertThat(result.get().emailVerified()).isTrue();

			verify(userRepository).findById(userId);
		}

		@Test
		@DisplayName("Should return empty Optional when user does not exist")
		void findById_NotFound() {
			// Given
			Long userId = 999L;
			given(userRepository.findById(userId)).willReturn(Optional.empty());

			// When
			Optional<UserResponse> result = userService.findById(userId);

			// Then
			assertThat(result).isEmpty();

			verify(userRepository).findById(userId);
		}
	}
}