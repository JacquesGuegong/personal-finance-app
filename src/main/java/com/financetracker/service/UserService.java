package com.financetracker.service;

import com.financetracker.entity.User;
import com.financetracker.exception.ResourceNotFoundException;
import com.financetracker.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor injection: Spring sees exactly one constructor and wires
    // the beans automatically. No @Autowired needed on the constructor in Spring 5+.
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional  // writes to DB → must be transactional
    public User register(String email, String rawPassword) {
        if (userRepository.existsByEmail(email)) {
            // 409 Conflict — a global @ControllerAdvice will map this later
            throw new IllegalStateException("Email already registered: " + email);
        }
        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword)) // BCrypt: slow + salted
                .build();
        return userRepository.save(user);
    }

    // Read-only — no @Transactional needed; JPA repositories handle their own transactions
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}
