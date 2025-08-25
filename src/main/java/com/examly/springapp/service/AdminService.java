package com.examly.springapp.service;

import com.examly.springapp.repository.HabitLogRepository;
import com.examly.springapp.repository.HabitRepository;
import com.examly.springapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;


import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Service
public class AdminService {

    private final DataSource dataSource;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private HabitLogRepository habitLogRepository;


    @Autowired
    public AdminService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Map<String, Object> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();

        Runtime runtime = Runtime.getRuntime();
        health.put("totalMemoryMB", runtime.totalMemory() / (1024 * 1024));
        health.put("freeMemoryMB", runtime.freeMemory() / (1024 * 1024));
        health.put("maxMemoryMB", runtime.maxMemory() / (1024 * 1024));

        long uptimeMillis = ManagementFactory.getRuntimeMXBean().getUptime();
        health.put("uptimeMinutes", uptimeMillis / (1000 * 60));


        try (Connection conn = dataSource.getConnection()) {
            health.put("database", "UP");
        } catch (SQLException e) {
            health.put("database", "DOWN");
        }

        health.put("timestamp", LocalDateTime.now());

        return health;
    }

    public Map<String, Object> getOverallPlatformActivity() {
        Map<String, Object> activity = new HashMap<>();

        long totalUsers = userRepository.count();
        activity.put("totalUsers", totalUsers);

        long totalHabits = habitRepository.count();
        activity.put("totalHabits", totalHabits);

        long totalHabitLogs = habitLogRepository.count();
        activity.put("totalHabitLogs", totalHabitLogs);

        return activity;
    }


    }
