package com.examly.springapp.service;

import com.examly.springapp.model.User;
import com.examly.springapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createUser(User user) {
        String rawPassword = user.getPassword();
        validatePassword(rawPassword);
        String hashedPassword = passwordEncoder.encode(rawPassword);
        user.setPassword(hashedPassword);
        user.setActive(true);
        return userRepository.save(user);
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long.");
        }
        Pattern pattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$");
        Matcher matcher = pattern.matcher(password);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Password must contain at least one digit, one lowercase, one uppercase letter, and one special character.");
        }
    }

    public User updateUser(Long id, User updatedUser) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        existingUser.setName(updatedUser.getName());
        existingUser.setEmail(updatedUser.getEmail());
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
            validatePassword(updatedUser.getPassword());
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }
        existingUser.setRole(updatedUser.getRole());
        existingUser.setActive(updatedUser.getActive());
        return userRepository.save(existingUser);
    }

    @Transactional
    public User updateUserProfile(Long userId, User updatedDetails) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(updatedDetails.getName());
        user.setDateOfBirth(updatedDetails.getDateOfBirth());
        user.setHeight(updatedDetails.getHeight());
        user.setWeight(updatedDetails.getWeight());
        user.setBmi(updatedDetails.getBmi());
        user.setGoals(updatedDetails.getGoals());

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id, Authentication authentication) {
        User userToDelete = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (authentication != null && userToDelete.getEmail().equals(authentication.getName())) {
            throw new IllegalArgumentException("Admins cannot delete their own account.");
        }

        userRepository.deleteById(id);
    }

    public Page<User> getAllUsers(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentAdminEmail = authentication.getName();

        // Fetch paginated users
        Page<User> userPage = userRepository.findAll(pageable);

        List<User> filteredUsers = userPage.getContent().stream()
                .filter(user -> !user.getEmail().equals(currentAdminEmail))
                .toList();

        return new PageImpl<>(filteredUsers, pageable, userPage.getTotalElements() - 1);
    }

    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setActive(false);
        userRepository.save(user);
    }

    public void activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setActive(true);
        userRepository.save(user);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}