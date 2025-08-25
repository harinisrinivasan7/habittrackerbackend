package com.examly.springapp.service;

import com.examly.springapp.dto.HabitLogDTO;
import com.examly.springapp.model.Habit;
import com.examly.springapp.model.HabitLog;
import com.examly.springapp.repository.HabitLogRepository;
import com.examly.springapp.repository.HabitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class HabitLogService {

    @Autowired
    private HabitLogRepository habitLogRepository;

    @Autowired
    private HabitRepository habitRepository;

    @Transactional
    public void logHabitCompletion(Long habitId, HabitLogDTO habitLog){
        if (isHabitCompletedToday(habitId)) {
            throw new IllegalStateException("Habit already logged for today.");
        }

        HabitLog newLog = new HabitLog();
        newLog.setHabitId(habitId);
        newLog.setCompletionDate(LocalDate.now());
        newLog.setNotes(habitLog.getNotes());
        newLog.setDuration(habitLog.getDuration());
        habitLogRepository.save(newLog);

        habitRepository.findById(habitId).ifPresent(habit -> {
            if (habit.getStatus() == Habit.Status.ARCHIVED) {
                habit.setStatus(Habit.Status.ACTIVE);
                habitRepository.save(habit);
            }
        });
    }

    public boolean isHabitCompletedToday(Long habitId) {
        Optional<HabitLog> log = habitLogRepository.findByHabitIdAndCompletionDate(habitId, LocalDate.now());
        return log.isPresent();
    }
}