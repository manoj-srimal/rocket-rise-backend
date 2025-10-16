package com.game.crashgamev2;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "user_withdrawal_methods")
public class UserWithdrawalMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore // API එකෙන් user details ආපහු යවන එක නවත්වනවා
    private User user;

    @Column(nullable = false)
    private String methodType; // e.g., "BANK", "CRYPTO"

    @Column(nullable = false)
    private String methodName; // e.g., "My BOC Savings"

    @Column(name = "account_holder_name", nullable = false)
    private String accountHolderName;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "branch_name")
    private String branchName;

    @Column(name = "is_active")
    private boolean isActive = true;
}