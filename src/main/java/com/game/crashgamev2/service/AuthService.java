package com.game.crashgamev2.service;


import com.game.crashgamev2.PasswordResetToken;
import com.game.crashgamev2.User;
import com.game.crashgamev2.VerificationToken;
import com.game.crashgamev2.dto.ForgotPasswordRequestDto;
import com.game.crashgamev2.dto.ResetPasswordDto;
import com.game.crashgamev2.dto.SignInRequest;
import com.game.crashgamev2.dto.SignUpRequest;
import com.game.crashgamev2.repository.PasswordResetTokenRepository;
import com.game.crashgamev2.repository.UserRepository;
import com.game.crashgamev2.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordResetTokenRepository passwordTokenRepository;

    @Transactional
    public void registerUser(SignUpRequest signUpRequest) {
        if (userRepository.findByEmail(signUpRequest.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already in use.");
        }
        User user = new User();
        user.setPublicUserId(generateNextPublicUserId());
        user.setFirstName(signUpRequest.getFirstName());
        user.setLastName(signUpRequest.getLastName());
        user.setEmail(signUpRequest.getEmail());
        user.setMobileNumber(signUpRequest.getMobileNumber());
        user.setPasswordHash(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setRole(User.Role.USER);
        user.setStatus(User.Status.PENDING_VERIFICATION); // <-- නිවැරදි කළ status එක
        user.setBalance(java.math.BigDecimal.ZERO);
        userRepository.save(user);

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, user);
        tokenRepository.save(verificationToken);

        // පරණ email logic එක වෙනුවට, EmailService එක call කරනවා
        emailService.sendVerificationEmail(user, token);
    }

    public String verifyUser(String token) {
        VerificationToken verificationToken = tokenRepository.findByToken(token);
        if (verificationToken == null) {
            return "invalidToken";
        }

        User user = verificationToken.getUser();
        if (verificationToken.getExpiryDate().before(new Date())) {
            tokenRepository.delete(verificationToken); // Expired token එක delete කරනවා
            return "expired";
        }

        user.setStatus(User.Status.ACTIVE);
        userRepository.save(user);

        tokenRepository.delete(verificationToken); // Verify කළාට පස්සේ token එක delete කරනවා
        return "valid";
    }

    // AuthService.java file එකේ signIn method එකට මේක දාන්න

    public String signIn(SignInRequest signInRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(signInRequest.getEmail(), signInRequest.getPassword())
        );

        User user = userRepository.findByEmail(signInRequest.getEmail())
                .orElseThrow(() -> new IllegalStateException("User not found after authentication."));

        if (user.getStatus() != User.Status.ACTIVE) {
            throw new IllegalStateException("Please verify your email before logging in.");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return jwtService.generateToken(userDetails);
    }

    private String generateNextPublicUserId() {
        String prefix = "CRG";
        String lastId = userRepository.findLastPublicUserId().orElse(null);
        if (lastId == null) {
            return prefix + String.format("%06d", 1);
        } else {
            int lastNumber = Integer.parseInt(lastId.substring(prefix.length()));
            int nextNumber = lastNumber + 1;
            return prefix + String.format("%06d", nextNumber);
        }
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequestDto request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String token = String.format("%06d", new Random().nextInt(999999)); // ඉලක්කම් 6ක code එකක්
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setToken(token);
            resetToken.setUser(user);
            resetToken.setExpiryDate(new Date(System.currentTimeMillis() + 15 * 60 * 1000)); // විනාඩි 15
            passwordTokenRepository.save(resetToken);
            emailService.sendPasswordResetEmail(user, token);
        });

    }

    @Transactional
    public void resetPassword(ResetPasswordDto request) {
        PasswordResetToken resetToken = passwordTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new IllegalStateException("Invalid token."));

        if (resetToken.getExpiryDate().before(new Date())) {
            passwordTokenRepository.delete(resetToken);
            throw new IllegalStateException("Token has expired.");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        passwordTokenRepository.delete(resetToken);
    }
}