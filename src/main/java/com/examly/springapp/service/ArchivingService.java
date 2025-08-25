package com.examly.springapp.service;

import com.examly.springapp.model.Habit;
import com.examly.springapp.model.HabitLog;
import com.examly.springapp.repository.HabitLogRepository;
import com.examly.springapp.repository.HabitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ArchivingService {

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private HabitLogRepository habitLogRepository;

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void archiveInactiveHabits() {
        System.out.println("Running scheduled task to archive inactive habits...");

        LocalDate threeMonthsAgo = LocalDate.now().minusMonths(3);

        List<Habit> activeHabits = habitRepository.findByStatus(Habit.Status.ACTIVE);

        for (Habit habit : activeHabits) {
            List<HabitLog> recentLogs = habitLogRepository.findByHabitIdAndCompletionDateBetween(
                    habit.getId(), threeMonthsAgo, LocalDate.now()
            );

            if (recentLogs.isEmpty()) {
                habit.setStatus(Habit.Status.ARCHIVED);
                habitRepository.save(habit);
                System.out.println("Archived habit: " + habit.getName());
            }
        }
        System.out.println("Finished archiving task.");
    }
}