package com.game.crashgamev2.controller;

import com.game.crashgamev2.PaymentMethod;
import com.game.crashgamev2.repository.PaymentMethodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment-methods")
public class PaymentMethodController {

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    // --- Player පැත්තට අදාළ Endpoint එක ---
    @GetMapping
    public ResponseEntity<List<PaymentMethod>> getActivePaymentMethods() {
        return ResponseEntity.ok(paymentMethodRepository.findAllByActive(true));
    }

    // --- Admin පැත්තට අදාළ Endpoints ---
    @GetMapping("/all")
    public ResponseEntity<List<PaymentMethod>> getAllPaymentMethods() {
        return ResponseEntity.ok(paymentMethodRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<PaymentMethod> addPaymentMethod(@RequestBody PaymentMethod paymentMethod) {
        return ResponseEntity.ok(paymentMethodRepository.save(paymentMethod));
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<PaymentMethod> toggleStatus(@PathVariable Integer id) {
        PaymentMethod method = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment method not found"));
        method.setActive(!method.isActive());
        return ResponseEntity.ok(paymentMethodRepository.save(method));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMethod(@PathVariable Integer id) {
        paymentMethodRepository.deleteById(id);
        return ResponseEntity.ok("Method deleted successfully.");
    }
}