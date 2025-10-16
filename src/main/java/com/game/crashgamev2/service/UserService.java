package com.game.crashgamev2.service;

import com.game.crashgamev2.User;
import com.game.crashgamev2.dto.ChangePasswordRequestDto;
import com.game.crashgamev2.dto.UserProfileDto;
import com.game.crashgamev2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserProfileDto getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        UserProfileDto dto = new UserProfileDto();
        dto.setPublicUserId(user.getPublicUserId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setBalance(user.getBalance());

        return dto;
    }

    @Autowired private PasswordEncoder passwordEncoder;

    public void changePassword(String email, ChangePasswordRequestDto request) {
        User user = userRepository.findAndLockByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found."));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new IllegalStateException("Incorrect old password.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}