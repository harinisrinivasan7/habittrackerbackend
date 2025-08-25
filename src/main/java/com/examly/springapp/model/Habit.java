package com.examly.springapp.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.time.LocalTime;

@Entity
@Table(name = "habits")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Habit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    @Enumerated(EnumType.STRING)
    private Frequency frequency;
    @Enumerated(EnumType.STRING)
    private Status status;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private Boolean notificationsEnabled = false;
    private LocalTime notificationTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "habitId", fetch = FetchType.LAZY)
    private Set<HabitLog> habitLogs;


    public enum Frequency {
        DAILY, WEEKLY;

        @JsonCreator
        public static Frequency fromString(String value) {
            return value == null ? null : Frequency.valueOf(value.toUpperCase());
        }
    }

    public enum Status {
        ACTIVE, ARCHIVED
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = Status.ACTIVE;
        }
    }
}