package com.game.crashgamev2.controller;


import com.game.crashgamev2.dto.ForgotPasswordRequestDto;
import com.game.crashgamev2.dto.ResetPasswordDto;
import com.game.crashgamev2.dto.SignInRequest;
import com.game.crashgamev2.dto.SignUpRequest;
import com.game.crashgamev2.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignUpRequest signUpRequest) {
        try {
            authService.registerUser(signUpRequest);
            return ResponseEntity.ok("User registered successfully!");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody SignInRequest signInRequest) {
        try {
            String token = authService.signIn(signInRequest);
            return ResponseEntity.ok(java.util.Map.of("token", token));
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid email or password");
        }
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
        String result = authService.verifyUser(token);
        if ("valid".equals(result)) {
            return ResponseEntity.ok("Email verified successfully. You can now log in.");
        } else if ("expired".equals(result)) {
            return ResponseEntity.status(400).body("Verification link has expired. Please register again.");
        } else {
            return ResponseEntity.status(400).body("Invalid verification token.");
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequestDto request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok("If an account with that email exists, a password reset code has been sent.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDto request) {
        try {
            authService.resetPassword(request);
            return ResponseEntity.ok("Password has been reset successfully.");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}