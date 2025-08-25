package com.examly.springapp.service;

import com.examly.springapp.dto.AnalyticsResponseDTO;
import com.examly.springapp.model.Habit;
import com.examly.springapp.model.HabitLog;
import com.examly.springapp.model.User;
import com.examly.springapp.repository.HabitLogRepository;
import com.examly.springapp.repository.HabitRepository;
import com.examly.springapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class AnalyticsService {

    @Autowired
    private HabitLogRepository habitLogRepository;

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private UserRepository userRepository;

    public AnalyticsResponseDTO getAnalyticsForHabit(Long habitId) {
        Habit habit = habitRepository.findById(habitId).orElse(null);
        if (habit == null) {
            return new AnalyticsResponseDTO();
        }

        List<HabitLog> logs = habitLogRepository.findByHabitId(habitId);

        List<LocalDate> completionDates = logs.stream()
                .map(HabitLog::getCompletionDate)
                .sorted()
                .collect(Collectors.toList());

        int longestStreak = calculateLongestStreak(completionDates);
        int currentStreak = calculateCurrentStreak(completionDates);

        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        long logsInLast30Days = logs.stream()
                .filter(log -> !log.getCompletionDate().isBefore(thirtyDaysAgo))
                .count();

        long totalPossibleCompletions = 0;
        if (habit.getFrequency() == Habit.Frequency.DAILY) {
            totalPossibleCompletions = 30;
        } else if (habit.getFrequency() == Habit.Frequency.WEEKLY) {
            totalPossibleCompletions = IntStream.range(0, 30)
                    .mapToObj(i -> LocalDate.now().minusDays(i))
                    .filter(date -> date.getDayOfWeek() == DayOfWeek.SUNDAY)
                    .count();
        }

        float completionRate = totalPossibleCompletions > 0 ? ((float) logsInLast30Days / totalPossibleCompletions) * 100 : 0;

        LocalDate startOfWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        Set<DayOfWeek> completedDays = logs.stream()
                .filter(log -> !log.getCompletionDate().isBefore(startOfWeek))
                .map(log -> log.getCompletionDate().getDayOfWeek())
                .collect(Collectors.toSet());

        Map<String, Boolean> weeklyCompletion = Map.of(
                "MONDAY", completedDays.contains(DayOfWeek.MONDAY),
                "TUESDAY", completedDays.contains(DayOfWeek.TUESDAY),
                "WEDNESDAY", completedDays.contains(DayOfWeek.WEDNESDAY),
                "THURSDAY", completedDays.contains(DayOfWeek.THURSDAY),
                "FRIDAY", completedDays.contains(DayOfWeek.FRIDAY),
                "SATURDAY", completedDays.contains(DayOfWeek.SATURDAY),
                "SUNDAY", completedDays.contains(DayOfWeek.SUNDAY)
        );

        AnalyticsResponseDTO dto = new AnalyticsResponseDTO();
        dto.setLongestStreak(longestStreak);
        dto.setCurrentStreak(currentStreak);
        dto.setCompletionRate(completionRate);
        dto.setWeeklyCompletion(weeklyCompletion);

        return dto;
    }

    public Map<String, Object> getOverallAnalyticsForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Habit> userHabits = habitRepository.findByUserId(user.getId());
        List<HabitLog> allUserLogs = userHabits.stream()
                .flatMap(h -> h.getHabitLogs().stream())
                .collect(Collectors.toList());

        long totalHabits = userHabits.size();

        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        long totalCompletions = allUserLogs.stream()
                .filter(log -> !log.getCompletionDate().isBefore(thirtyDaysAgo))
                .count();

        long totalPossibleCompletions = userHabits.stream()
                .mapToLong(h -> h.getFrequency() == Habit.Frequency.DAILY ? 30 :
                        IntStream.range(0, 30).mapToObj(i -> LocalDate.now().minusDays(i))
                                .filter(date -> date.getDayOfWeek() == DayOfWeek.SUNDAY).count())
                .sum();

        float overallCompletionRate = totalPossibleCompletions > 0 ? ((float) totalCompletions / totalPossibleCompletions) * 100 : 0;

        Map<String, Object> overallStats = new HashMap<>();
        overallStats.put("totalHabits", totalHabits);
        overallStats.put("totalCompletions", totalCompletions);
        overallStats.put("overallCompletionRate", overallCompletionRate);

        return overallStats;
    }

    private int calculateLongestStreak(List<LocalDate> dates) {
        if (dates.isEmpty()) {
            return 0;
        }

        int longest = 1;
        int current = 1;
        for (int i = 1; i < dates.size(); i++) {
            if (dates.get(i).equals(dates.get(i - 1).plusDays(1))) {
                current++;
            } else {
                longest = Math.max(longest, current);
                current = 1;
            }
        }
        return Math.max(longest, current);
    }

    private int calculateCurrentStreak(List<LocalDate> dates) {
        if (dates.isEmpty()) {
            return 0;
        }

        Collections.reverse(dates);
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        if (!dates.get(0).equals(today) && !dates.get(0).equals(yesterday)) {
            return 0;
        }

        int current = 1;
        for (int i = 0; i < dates.size() - 1; i++) {
            if (dates.get(i).equals(dates.get(i + 1).plusDays(1))) {
                current++;
            } else {
                break;
            }
        }
        return current;
    }
}