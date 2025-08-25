package com.examly.springapp.controller;

import com.examly.springapp.model.HabitLog;
import com.examly.springapp.model.User;
import com.examly.springapp.repository.HabitLogRepository;
import com.examly.springapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/habit-logs")
@CrossOrigin(origins = "*")
public class HabitLogController {

    @Autowired
    private HabitLogRepository habitLogRepository;

    @Autowired
    private UserService userService;

    @GetMapping("/{habitId}")
    public ResponseEntity<List<HabitLog>> getHabitLogs(@PathVariable Long habitId) {
        List<HabitLog> logs = habitLogRepository.findByHabitId(habitId);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getAllUserLogs(Authentication authentication) {
        User user = userService.findUserByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<Map<String, Object>> logs = habitLogRepository.findAllLogsForUser(user.getId());
        return ResponseEntity.ok(logs);
    }
}