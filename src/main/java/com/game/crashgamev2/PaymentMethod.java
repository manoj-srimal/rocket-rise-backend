package com.game.crashgamev2;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "payment_methods")
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MethodType type;

    @Column(nullable = false)
    private String name; // e.g., "Commercial Bank", "Binance USDT"

    @Column(name = "account_name", nullable = false)
    private String accountName; // e.g., "Crash Game Pvt Ltd"

    @Column(name = "account_number", nullable = false)
    private String accountNumber; // e.g., "100012345678" or crypto address

    @Column
    private String branch; // e.g., "Colombo Main Branch" (Bank වලට විතරක්)

    @Column
    private String description; // e.g., "Please use your User ID as the reference"

    @Column(nullable = false)
    private boolean active = true;


    public enum MethodType {
        BANK,
        CRYPTO,
        E_WALLET
    }
}