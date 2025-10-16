package com.game.crashgamev2.controller;

import com.game.crashgamev2.dto.ChangePasswordRequestDto;
import com.game.crashgamev2.dto.UserProfileDto;
import com.game.crashgamev2.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:3000") // CORS for this controller
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getCurrentUser(Principal principal) {
        String email = principal.getName();
        UserProfileDto userProfile = userService.getUserProfile(email);
        return ResponseEntity.ok(userProfile);
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(Principal principal, @RequestBody ChangePasswordRequestDto request) {
        try {
            userService.changePassword(principal.getName(), request);
            return ResponseEntity.ok("Password changed successfully.");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
