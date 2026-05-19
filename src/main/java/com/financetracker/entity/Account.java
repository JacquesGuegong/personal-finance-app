package com.financetracker.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    // LAZY: do NOT load the User row from DB when you load an Account.
    // Load it only if code actually calls account.getUser().
    // EAGER (the default for @ManyToOne) would join users on every account query — wasteful.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false) // creates the FK column "user_id" in the accounts table
    private User user;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)  // store "CHECKING" in the DB, not 0 — safe if enum order ever changes
    @Column(nullable = false)
    private AccountType type;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;
}
