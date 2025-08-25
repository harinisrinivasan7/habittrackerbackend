package com.examly.springapp.repository;

import com.examly.springapp.model.Habit;
import com.examly.springapp.model.HabitLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HabitRepository extends JpaRepository<Habit, Long> {

    List<Habit> findByUserId(Long userId);

    List<Habit> findByStatus(Habit.Status status);

    List<Habit> findByNotificationsEnabledTrue();

    List<Habit> findByNotificationsEnabledTrueAndUser_Id(Long userId);

    @Query("SELECT h FROM Habit h LEFT JOIN FETCH h.habitLogs WHERE h.user.id = :userId")
    List<Habit> findByUserIdWithLogs(@Param("userId") Long userId);

    @Query("SELECT hl FROM HabitLog hl WHERE hl.habitId IN " +
            "(SELECT h.id FROM Habit h WHERE h.user.id = :userId) " +
            "AND YEAR(hl.completionDate) = :year AND MONTH(hl.completionDate) = :month")
    List<HabitLog> findLogsByUserIdAndMonth(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);
}