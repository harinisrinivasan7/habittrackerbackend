package com.examly.springapp.dto;

import com.examly.springapp.model.Habit;

public class HabitDTO {

    private Long id;
    private String name;
    private String description;
    private Habit.Frequency frequency;
    private Habit.Status status;
    private Long userId;
    private boolean active;

    public static HabitDTOBuilder builder() {
        return new HabitDTOBuilder();
    }

    public static class HabitDTOBuilder {

        private Long id;
        private String name;
        private String description;
        private Habit.Frequency frequency;
        private Habit.Status status;
        private Long userId;
        private boolean active;

        public HabitDTOBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public HabitDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public HabitDTOBuilder description(String description) {
            this.description = description;
            return this;
        }

        public HabitDTOBuilder frequency(String frequency) {
            this.frequency = Habit.Frequency.valueOf(frequency.toUpperCase());
            return this;
        }

        public HabitDTOBuilder status(Habit.Status status) {
            this.status = status;
            return this;
        }

        public HabitDTOBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public HabitDTOBuilder active(boolean active) {
            this.active = active;
            return this;
        }

        public HabitDTO build() {
            HabitDTO dto = new HabitDTO();
            dto.setId(id);
            dto.setName(name);
            dto.setDescription(description);
            dto.setFrequency(frequency);
            dto.setStatus(status);
            dto.setUserId(userId);
            dto.setActive(active);
            return dto;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Habit.Frequency getFrequency() {
        return frequency;
    }

    public void setFrequency(Habit.Frequency frequency) {
        this.frequency = frequency;
    }

    public Habit.Status getStatus() {
        return status;
    }

    public void setStatus(Habit.Status status) {
        this.status = status;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
