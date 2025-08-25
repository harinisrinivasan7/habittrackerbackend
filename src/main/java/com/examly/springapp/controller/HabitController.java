package com.examly.springapp.controller;

import com.examly.springapp.dto.HabitLogDTO;
import com.examly.springapp.model.Habit;
import com.examly.springapp.model.User;
import com.examly.springapp.service.HabitService;
import com.examly.springapp.service.UserService;
import com.examly.springapp.service.HabitLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;

@RestController
@RequestMapping("/api/habits")
@CrossOrigin(origins = "*")
public class HabitController {

    @Autowired
    private HabitService habitService;

    @Autowired
    private UserService userService;

    @Autowired
    private HabitLogService habitLogService;

    @GetMapping
    public ResponseEntity<List<Habit>> getAllHabits(Authentication authentication) {
        User user = userService.findUserByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return ResponseEntity.ok(habitService.getHabitsForUser(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Habit> getHabitById(@PathVariable Long id) {
        return habitService.getHabitById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Habit> createHabit(@RequestBody Habit habit, Authentication authentication) {
        User user = userService.findUserByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        habit.setStatus(Habit.Status.ACTIVE);

        Habit createdHabit = habitService.createHabitForUser(habit, user);
        return new ResponseEntity<>(createdHabit, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Habit> updateHabit(@PathVariable Long id, @RequestBody Habit habitDetails) {
        return habitService.updateHabit(id, habitDetails)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHabit(@PathVariable Long id) {
        if (habitService.existsById(id)) {
            habitService.deleteHabit(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> logHabitCompletion(@PathVariable Long id, @RequestBody HabitLogDTO habitLogDTO) {
        try {
            habitLogService.logHabitCompletion(id, habitLogDTO);
            return ResponseEntity.ok(Map.of("message", "Habit logged successfully for today."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Habit already logged for today."));
        }
    }

    @GetMapping("/daily")
    public ResponseEntity<List<Map<String, Object>>> getDailyHabits(Authentication authentication) {
        User user = userService.findUserByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<Habit> habits = habitService.getHabitsForUser(user);

        List<Map<String, Object>> dailyHabits = habits.stream()
                .map(habit -> {
                    boolean isCompleted = habitLogService.isHabitCompletedToday(habit.getId());
                    Map<String, Object> habitData = new HashMap<>();
                    habitData.put("id", habit.getId());
                    habitData.put("name", habit.getName());
                    habitData.put("isCompleted", isCompleted);
                    habitData.put("status", habit.getStatus().name());
                    return habitData;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(dailyHabits);
    }
}