package com.game.crashgamev2.controller;

import com.game.crashgamev2.dto.TransactionDto;
import com.game.crashgamev2.dto.TransactionRequestDto;
import com.game.crashgamev2.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/my-history")
    public ResponseEntity<List<TransactionDto>> getMyTransactionHistory(Principal principal) {
        return ResponseEntity.ok(transactionService.getMyTransactions(principal.getName()));
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> requestDeposit(Principal principal, @RequestBody TransactionRequestDto request) {
        try {
            transactionService.createDepositRequest(principal.getName(), request.getAmount(), request.getReceiptUrl());
            return ResponseEntity.ok("Deposit request submitted successfully. Please wait for admin approval.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> requestWithdrawal(Principal principal, @RequestBody TransactionRequestDto request) {
        System.out.println("[CONTROLLER DEBUG] Received Withdrawal Method ID: " + request.getWithdrawalMethodId());
        try {
            transactionService.createWithdrawalRequest(principal.getName(), request.getAmount(), request.getWithdrawalMethodId());
            return ResponseEntity.ok("Withdrawal request submitted successfully. It will be processed soon.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}