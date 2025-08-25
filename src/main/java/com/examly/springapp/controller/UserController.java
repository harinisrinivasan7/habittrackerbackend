package com.examly.springapp.controller;

import com.examly.springapp.config.JwtUtil;
import com.examly.springapp.dto.AuthenticationRequest;
import com.examly.springapp.dto.AuthenticationResponse;
import com.examly.springapp.model.User;
import com.examly.springapp.service.MyUserDetailsService;
import com.examly.springapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:8081")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private MyUserDetailsService userDetailsService;
    @Autowired
    private JwtUtil jwtUtil;


    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        User newUser = userService.createUser(user);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }
    
    @PostMapping("/authenticate")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authRequest) throws Exception {
        System.out.println("--- Attempting Authentication ---");
        System.out.println("Email: " + authRequest.email());
        System.out.println("Password: " + authRequest.password());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.email(), authRequest.password())
            );
        } catch (BadCredentialsException e) {
            System.out.println("Authentication failed: Bad credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect email or password");
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.email());
        final String jwt = jwtUtil.generateToken(userDetails);

        System.out.println("Authentication successful. JWT created.");
        return ResponseEntity.ok(new AuthenticationResponse(jwt));
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getUserProfile(Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userService.findUserByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateUserProfile(@RequestBody User updatedDetails, Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userService.findUserByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        User updatedUser = userService.updateUserProfile(user.getId(), updatedDetails);
        return ResponseEntity.ok(updatedUser);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        User user = userService.updateUser(id, updatedUser);
        return ResponseEntity.ok(user);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id, Authentication authentication) {
        // FIX: Pass the Authentication object to the service method
        userService.deleteUser(id, authentication);
        return ResponseEntity.ok("User deleted successfully");
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<String> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok("User account deactivated");
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/{id}/activate")
    public ResponseEntity<String> activateUser(@PathVariable Long id) {
        userService.activateUser(id);
        return ResponseEntity.ok("User account activated");
    }
}