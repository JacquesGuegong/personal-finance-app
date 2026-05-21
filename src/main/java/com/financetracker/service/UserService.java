package com.financetracker.service;

import com.financetracker.entity.User;
import com.financetracker.exception.ResourceNotFoundException;
import com.financetracker.exception.UnauthorizedException;
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

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    // Always say "Invalid credentials" for both wrong email AND wrong password —
    // never distinguish between them so callers can't enumerate valid emails.
    public User authenticate(String email, String rawPassword) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || !passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        return user;
    }
}
