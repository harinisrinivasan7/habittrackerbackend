package com.examly.springapp.controller;

import com.examly.springapp.dto.HabitDTO;
import com.examly.springapp.model.User;
import com.examly.springapp.repository.UserRepository;
import com.examly.springapp.repository.HabitRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class HabitControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private HabitRepository habitRepository;
    @Autowired
    private ObjectMapper objectMapper;

    private Long userId;

    @BeforeEach
    void setup() {
        habitRepository.deleteAll();
        userRepository.deleteAll();
        User saved = userRepository.save(
                User.builder().username("habittester").email("tester@h.com").createdAt(java.time.LocalDateTime.now()).build()
        );
        userId = saved.getUserId();
    }

    @Test
    void controller_testCreateAndGetHabit() throws Exception {
        HabitDTO dto = HabitDTO.builder()
                .userId(userId)
                .name("Meditation")
                .description("10 min mindfulness")
                .frequency("Daily")
                .build();
        String content = objectMapper.writeValueAsString(dto);
        // create
        mockMvc.perform(post("/api/habits")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.habitId").exists())
                .andExpect(jsonPath("$.name").value("Meditation"));     

        // get
        mockMvc.perform(get("/api/habits/user/"+userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Meditation"));
    }

    @Test
    void controller_testUpdateHabit() throws Exception {
        HabitDTO dto = HabitDTO.builder()
                .userId(userId)
                .name("Exercise")
                .description("30 min cardio")
                .frequency("Daily")
                .build();
        String h = mockMvc.perform(post("/api/habits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andReturn().getResponse().getContentAsString();
        // Fix: use Number and convert to longValue
        Number habitIdNumber = com.jayway.jsonpath.JsonPath.read(h, "$.habitId");
        Long habitId = habitIdNumber.longValue();

        HabitDTO up = HabitDTO.builder()
                .name("Updated Exercise")
                .description("45 min cardio")
                .frequency("Weekly")
                .active(true)
                .build();
        mockMvc.perform(put("/api/habits/"+habitId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(up)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated Exercise"));
    }

    @Test
    void controller_testCreateHabitUserNotFound() throws Exception {
        HabitDTO dto = HabitDTO.builder()
                .userId(9999L)
                .name("Yoga")
                .frequency("Daily")
                .build();
        mockMvc.perform(post("/api/habits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }
}
