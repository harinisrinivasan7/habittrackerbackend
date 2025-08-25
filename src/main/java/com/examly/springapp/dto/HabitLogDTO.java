package com.examly.springapp.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;


@Getter
@Setter
@Builder
public class HabitLogDTO {
    private Integer duration; // in minutes
    private String notes;
}
