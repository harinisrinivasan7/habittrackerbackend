package com.examly.springapp.controller;

import com.examly.springapp.dto.HabitLogDTO;
import com.examly.springapp.model.User;
import com.examly.springapp.model.Habit;
import com.examly.springapp.repository.UserRepository;
import com.examly.springapp.repository.HabitRepository;
import com.examly.springapp.repository.HabitLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class HabitLogControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private HabitRepository habitRepository;
    @Autowired
    private HabitLogRepository habitLogRepository;
    @Autowired
    private ObjectMapper objectMapper;
    private Long habitId;

    @BeforeEach
    void setup() {
        habitLogRepository.deleteAll();
        habitRepository.deleteAll();
        userRepository.deleteAll();
        User user = userRepository.save(User.builder().username("logtester").email("log@h.com").createdAt(java.time.LocalDateTime.now()).build());
        Habit habit = habitRepository.save(Habit.builder()
                .user(user)
                .name("Drink Water")
                .description("8 glasses")
                .frequency("Daily")
                .createdAt(java.time.LocalDateTime.now())
                .active(true)
                .build());
        habitId = habit.getHabitId();
    }

    @Test
    void controller_testHabitLogging() throws Exception {
        HabitLogDTO dto = HabitLogDTO.builder().notes("Felt refreshed").duration(10).build();
        mockMvc.perform(post("/api/habits/"+habitId+"/log")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.notes").value("Felt refreshed"));
        // Get logs
        mockMvc.perform(get("/api/habits/"+habitId+"/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].notes").value("Felt refreshed"));
    }

    @Test
    void controller_testLogHabitNotFound() throws Exception {
        HabitLogDTO dto = HabitLogDTO.builder().notes("Bad Id").build();
        mockMvc.perform(post("/api/habits/99/log")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Habit not found"));
    }

    @Test
    void controller_testGetLogsHabitNotFound() throws Exception {
        mockMvc.perform(get("/api/habits/99/logs"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Habit not found"));
    }
}
