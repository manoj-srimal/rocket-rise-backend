package com.game.crashgamev2.controller;

import com.game.crashgamev2.User;
import com.game.crashgamev2.UserWithdrawalMethod;
import com.game.crashgamev2.repository.UserRepository;
import com.game.crashgamev2.repository.UserWithdrawalMethodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/withdrawal-methods")
public class WithdrawalMethodController {

    @Autowired
    private UserWithdrawalMethodRepository methodRepository;
    @Autowired private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<UserWithdrawalMethod>> getMyMethods(Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return ResponseEntity.ok(methodRepository.findAllByUser_Id(user.getId()));
    }

    @PostMapping
    public ResponseEntity<UserWithdrawalMethod> addMethod(Principal principal, @RequestBody UserWithdrawalMethod method) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        method.setUser(user);
        return ResponseEntity.ok(methodRepository.save(method));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMethod(Principal principal, @PathVariable Integer id) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        UserWithdrawalMethod method = methodRepository.findById(id).orElseThrow(() -> new RuntimeException("Method not found"));

        if (!method.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("You are not authorized to delete this method.");
        }
        methodRepository.deleteById(id);
        return ResponseEntity.ok("Method deleted successfully.");
    }
}