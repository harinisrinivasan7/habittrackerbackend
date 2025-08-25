package com.examly.springapp.service;

import com.examly.springapp.model.Habit;
import com.examly.springapp.repository.HabitLogRepository;
import com.examly.springapp.repository.HabitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private HabitLogRepository habitLogRepository;

    public List<Habit> getOverdueHabits(Long userId) {
        LocalTime now = LocalTime.now();
        List<Habit> habitsToNotify = habitRepository.findByNotificationsEnabledTrueAndUser_Id(userId);

        return habitsToNotify.stream()
                .filter(habit -> habit.getNotificationTime() != null && now.isAfter(habit.getNotificationTime()))
                .filter(habit -> !habitLogRepository.findByHabitIdAndCompletionDate(habit.getId(), LocalDate.now()).isPresent())
                .collect(Collectors.toList());
    }
}