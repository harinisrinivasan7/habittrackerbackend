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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AnalyticsControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private HabitRepository habitRepository;
    @Autowired
    private ObjectMapper objectMapper;
    private Long habitId;
    private Long userId;

    @BeforeEach
    void setup() throws Exception {
        habitRepository.deleteAll();
        userRepository.deleteAll();
        User user = userRepository.save(User.builder().username("anatest").email("anatest@h.com").createdAt(java.time.LocalDateTime.now()).build());
        userId = user.getUserId();
        HabitDTO habitDTO = HabitDTO.builder()
                .userId(userId)
                .name("Read Book")
                .description("Read 20 pages")
                .frequency("Daily")
                .build();
        String resp = mockMvc.perform(post("/api/habits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(habitDTO)))
                .andReturn().getResponse().getContentAsString();
        Number habitIdNumber = com.jayway.jsonpath.JsonPath.read(resp, "$.habitId");
        habitId = habitIdNumber.longValue();
    }
    @Test
    void controller_testMonthlyAnalytics() throws Exception {
        // log once = 1 completion for one habit
        mockMvc.perform(post("/api/habits/"+habitId+"/log").contentType(MediaType.APPLICATION_JSON).content("{}"));
        int month = java.time.LocalDate.now().getMonthValue();
        int year = java.time.LocalDate.now().getYear();
        mockMvc.perform(get("/api/analytics/users/"+userId+"/monthly?month="+month+"&year="+year))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalHabits").value(1))
                .andExpect(jsonPath("$.totalCompletions").value(1));
    }
    @Test
void controller_testMonthlyAnalyticsWithNoCompletions() throws Exception {
    int month = java.time.LocalDate.now().getMonthValue();
    int year = java.time.LocalDate.now().getYear();
    mockMvc.perform(get("/api/analytics/users/" + userId + "/monthly?month=" + month + "&year=" + year))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalHabits").value(1))
            .andExpect(jsonPath("$.totalCompletions").value(0));
}

@Test
void controller_testMonthlyAnalyticsMultipleCompletions() throws Exception {
    // Log the same habit 3 times
    for (int i = 0; i < 3; i++) {
        mockMvc.perform(post("/api/habits/" + habitId + "/log")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"));
    }
    int month = java.time.LocalDate.now().getMonthValue();
    int year = java.time.LocalDate.now().getYear();
    mockMvc.perform(get("/api/analytics/users/" + userId + "/monthly?month=" + month + "&year=" + year))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalHabits").value(1))
            .andExpect(jsonPath("$.totalCompletions").value(3));
}

}
