package com.examly.springapp.repository;

import com.examly.springapp.model.HabitLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface HabitLogRepository extends JpaRepository<HabitLog, Long> {

    List<HabitLog> findByHabitId(Long habitId);

    List<HabitLog> findByHabitIdAndCompletionDateBetween(Long habitId, LocalDate startDate, LocalDate endDate);

    Optional<HabitLog> findByHabitIdAndCompletionDate(Long habitId, LocalDate completionDate);

    @Query("SELECT hl FROM HabitLog hl WHERE hl.habitId IN " +
            "(SELECT h.id FROM Habit h WHERE h.user.id = :userId) " +
            "AND YEAR(hl.completionDate) = :year AND MONTH(hl.completionDate) = :month")
    List<HabitLog> findLogsByUserIdAndMonth(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);

    @Query("SELECT new Map(h.name as habitName, hl.completionDate as completionDate, hl.notes as notes, hl.duration as duration) " +
            "FROM HabitLog hl JOIN Habit h ON hl.habitId = h.id WHERE h.user.id = :userId ORDER BY hl.completionDate DESC")
    List<Map<String, Object>> findAllLogsForUser(@Param("userId") Long userId);

    }

