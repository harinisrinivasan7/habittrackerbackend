package com.examly.springapp.service;

import com.examly.springapp.model.Habit;
import com.examly.springapp.model.User;
import com.examly.springapp.repository.HabitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HabitService {

    @Autowired
    private HabitRepository habitRepository;

    public List<Habit> getHabitsForUser(User user) {
        return habitRepository.findByUserId(user.getId());
    }

    public Habit createHabitForUser(Habit habit, User user) {
        habit.setUser(user);
        return habitRepository.save(habit);
    }

    public Optional<Habit> getHabitById(Long id) {
        return habitRepository.findById(id);
    }

    public Optional<Habit> updateHabit(Long id, Habit habitDetails) {
        return habitRepository.findById(id)
                .map(existingHabit -> {
                    existingHabit.setName(habitDetails.getName());
                    existingHabit.setDescription(habitDetails.getDescription());
                    existingHabit.setFrequency(habitDetails.getFrequency());
                    existingHabit.setNotificationsEnabled(habitDetails.getNotificationsEnabled());
                    existingHabit.setNotificationTime(habitDetails.getNotificationTime());
                    return habitRepository.save(existingHabit);
                });
    }

    public boolean deleteHabit(Long id) {
        if (habitRepository.existsById(id)) {
            habitRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean existsById(Long id) {
        return habitRepository.existsById(id);
    }
}