package com.examly.springapp.controller;

import com.examly.springapp.dto.UserDTO;
import com.examly.springapp.model.User;
import com.examly.springapp.service.UserService;
import com.examly.springapp.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ADMIN')")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> usersPage = userService.getAllUsers(pageable);

        Page<UserDTO> dtoPage = usersPage.map(UserDTO::fromEntity);

        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        return ResponseEntity.ok(adminService.getSystemHealth());
    }

    @GetMapping("/activity")
    public ResponseEntity<Map<String, Object>> getOverallPlatformActivity() {
        return ResponseEntity.ok(adminService.getOverallPlatformActivity());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(UserDTO.fromEntity(user));
    }

    @PostMapping("/users/create")
    public ResponseEntity<User> createUser(@RequestBody User newUser) {
        User createdUser = userService.createUser(newUser);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO updatedUserDTO) {
        User userDetails = new User();
        userDetails.setName(updatedUserDTO.getName());
        userDetails.setEmail(updatedUserDTO.getEmail());
        userDetails.setRole(updatedUserDTO.getRole());
        userDetails.setActive(updatedUserDTO.getActive());

        User updatedUser = userService.updateUser(id, userDetails);
        return ResponseEntity.ok(UserDTO.fromEntity(updatedUser));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id, Authentication authentication) {
        userService.deleteUser(id, authentication);
        return ResponseEntity.ok("User deleted successfully");
    }

    @PostMapping("/users/{id}/deactivate")
    public ResponseEntity<String> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok("User account deactivated");
    }

    @GetMapping("/user-growth")
    public Map<String, Object> getWeeklyUserGrowth() {
        Map<String, Object> userGrowthData = new HashMap<>();

        userGrowthData.put("labels", List.of("Week 4", "Week 3", "Week 2", "Week 1"));
        userGrowthData.put("data", List.of(10L, 15L, 20L, 25L));
        return userGrowthData;
    }

    @GetMapping("/top-habits")
    public Map<String, Object> getTopPerformingHabits() {
        Map<String, Object> topHabitsData = new HashMap<>();

        topHabitsData.put("labels", List.of("Habit A", "Habit B", "Habit C"));
        topHabitsData.put("data", List.of(90.5, 85.0, 78.2));
        return topHabitsData;
    }

    @PostMapping("/users/{id}/activate")
    public ResponseEntity<String> activateUser(@PathVariable Long id) {
        userService.activateUser(id);
        return ResponseEntity.ok("User account activated");
    }
}