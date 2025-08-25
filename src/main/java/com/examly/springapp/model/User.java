package com.examly.springapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String password;
    private Boolean active = true;
    @Enumerated(EnumType.STRING)
    private Role role;
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // New fields for user profile
    private LocalDate dateOfBirth;
    private Double height; // in cm
    private Double weight; // in kg
    private Double bmi;
    private String goals;

    public enum Role {
        USER, ADMIN
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (role == null) {
            role = Role.USER;
        }
    }


    public Long getUserId() {
        return this.id;
    }
}