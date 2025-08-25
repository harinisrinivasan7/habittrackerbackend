package com.examly.springapp.dto;

import java.util.Map;

public class AnalyticsResponseDTO {

    private float completionRate;
    private int longestStreak;
    private int currentStreak;
    private Map<String, Boolean> weeklyCompletion;

    public float getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(float completionRate) {
        this.completionRate = completionRate;
    }

    public int getLongestStreak() {
        return longestStreak;
    }

    public void setLongestStreak(int longestStreak) {
        this.longestStreak = longestStreak;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public Map<String, Boolean> getWeeklyCompletion() {
        return weeklyCompletion;
    }

    public void setWeeklyCompletion(Map<String, Boolean> weeklyCompletion) {
        this.weeklyCompletion = weeklyCompletion;
    }
}
