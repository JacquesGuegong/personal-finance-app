package com.financetracker.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")                          // "user" is a reserved word in PostgreSQL
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // JPA 3.1 UUID strategy — Hibernate generates the UUID in Java
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;                // Hibernate maps camelCase → password_hash via Spring's naming strategy

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist                                 // runs automatically just before INSERT — sets the timestamp once
    private void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
