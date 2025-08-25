package com.examly.springapp.controller;

import com.examly.springapp.dto.UserDTO;
import com.examly.springapp.model.User;
import com.examly.springapp.repository.UserRepository;
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
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    @Test
    void controller_testCreateUser() throws Exception {
        UserDTO dto = UserDTO.builder()
            .username("uniqueuser")
            .email("unique@example.com")
            .build();
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.username").value("uniqueuser"));

        // Duplicate username/email
        UserDTO dup = UserDTO.builder().username("uniqueuser").email("unique@example.com").build();
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dup)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username or email already exists"));
    }

    @Test
    void controller_testGetUserNotFound() throws Exception {
        mockMvc.perform(get("/api/users/9999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void controller_testInvalidUserData() throws Exception {
        // no username/email
        UserDTO dto = UserDTO.builder().build();
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid user data"));
    }
}
