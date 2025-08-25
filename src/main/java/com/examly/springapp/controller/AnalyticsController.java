package com.examly.springapp.controller;

import com.examly.springapp.dto.AnalyticsResponseDTO;
import com.examly.springapp.model.Habit;
import com.examly.springapp.model.User;
import com.examly.springapp.repository.HabitRepository;
import com.examly.springapp.service.AnalyticsService;
import com.examly.springapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private UserService userService;

    @GetMapping("/my")
    public ResponseEntity<AnalyticsResponseDTO> getMyAnalytics(@RequestParam Long habitId) {
        AnalyticsResponseDTO analytics = analyticsService.getAnalyticsForHabit(habitId);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/performance")
    public ResponseEntity<List<Map<String, Object>>> getHabitPerformance(@RequestParam(required = false) String date, Authentication authentication) {
        User user = userService.findUserByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<Habit> habits = habitRepository.findByUserIdWithLogs(user.getUserId());

        LocalDate selectedDate = (date != null) ? LocalDate.parse(date) : LocalDate.now();
        LocalDate thirtyDaysBeforeSelectedDate = selectedDate.minusDays(30);

        List<Map<String, Object>> performanceData = habits.stream()
                .map(habit -> {
                    long completedDays = habit.getHabitLogs().stream()
                            .filter(log -> log.getCompletionDate().isEqual(selectedDate))
                            .count();

                    long totalPossibleDays = 30;
                    double completionPercentage = totalPossibleDays > 0 ? ((double) completedDays / totalPossibleDays) * 100 : 0;

                    return Map.of(
                            "name", (Object) habit.getName(),
                            "value", (Object) (long) completedDays,
                            "completionPercentage", (Object) (long) completionPercentage
                    );
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(performanceData);
    }
}
