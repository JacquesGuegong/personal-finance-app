package com.financetracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfig {

    // Defines PasswordEncoder as a Spring bean so any service can inject it.
    // BCrypt is the correct choice: it's slow by design (resistant to brute force)
    // and automatically salts each hash, so identical passwords produce different hashes.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
