package com.example.account.service;

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

	public Optional<User> findById(Long id) {
		log.info("Get user details for id: {}", id);
		return userRepository.findById(id);
	}

	@Transactional
	public User saveUser(User user) {
		log.info("Saving new user: {}", user.getEmail());
		return userRepository.save(user);
	}

	public boolean existsByEmail(String email) {
		return userRepository.existsByEmail(email);
	}

	public boolean existsByMobile(String mobile) {
		return userRepository.existsByMobile(mobile);
	}
}
