package com.game.crashgamev2.controller;

import com.game.crashgamev2.dto.*;
import com.game.crashgamev2.service.AdminService;
import com.game.crashgamev2.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final TransactionService transactionService;

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDto> getStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserManagementDto>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/users/search")
    public ResponseEntity<List<UserManagementDto>> searchUsers(@RequestParam String query) {
        return ResponseEntity.ok(adminService.searchUsers(query));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<UserManagementDto> updateUserStatus(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        return ResponseEntity.ok(adminService.updateUserStatus(id, newStatus));
    }

    @GetMapping("/performance")
    public ResponseEntity<List<AdminPerformanceDto>> getAdminPerformance() {
        return ResponseEntity.ok(adminService.getAdminPerformance());
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<UserManagementDto> updateUserRole(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        String newRole = body.get("role");
        return ResponseEntity.ok(adminService.updateUserRole(id, newRole));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<AdminTransactionViewDto>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    @PutMapping("/transactions/{id}/process")
    public ResponseEntity<?> processTransaction(@PathVariable Integer id, @RequestBody Map<String, String> body, Principal principal) {
        try {
            transactionService.processTransaction(id, body.get("status"), principal.getName());
            return ResponseEntity.ok("Transaction processed successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/transactions/search")
    public ResponseEntity<List<AdminTransactionViewDto>> searchTransactions(@RequestParam String query) {
        return ResponseEntity.ok(transactionService.searchTransactions(query));
    }

    @GetMapping("/signals")
    public ResponseEntity<List<GameSignalDto>> getGameSignals() {
        return ResponseEntity.ok(adminService.getGameSignals());
    }
}