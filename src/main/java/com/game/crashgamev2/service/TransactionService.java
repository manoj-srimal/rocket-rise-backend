package com.game.crashgamev2.service;

import com.game.crashgamev2.Transaction;
import com.game.crashgamev2.User;
import com.game.crashgamev2.UserWithdrawalMethod;
import com.game.crashgamev2.dto.AdminTransactionViewDto;
import com.game.crashgamev2.dto.TransactionDto;
import com.game.crashgamev2.repository.TransactionRepository;
import com.game.crashgamev2.repository.UserRepository;
import com.game.crashgamev2.repository.UserWithdrawalMethodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    @Autowired private TransactionRepository transactionRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private UserWithdrawalMethodRepository withdrawalMethodRepository;
    @Autowired private SimpMessagingTemplate messagingTemplate;

    // --- Player පැත්තට අදාළ Methods ---
    public List<TransactionDto> getMyTransactions(String email) {
        return transactionRepository.findAllByUser_EmailOrderByCreatedAtDesc(email)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void createDepositRequest(String email, BigDecimal amount, String receiptUrl) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalStateException("User not found."));
        Transaction deposit = new Transaction();
        deposit.setUser(user);
        deposit.setAmount(amount);
        deposit.setType(Transaction.TransactionType.DEPOSIT);
        deposit.setStatus(Transaction.TransactionStatus.PENDING);
        deposit.setReceiptUrl(receiptUrl);
        transactionRepository.save(deposit);
    }

    @Transactional
    public void createWithdrawalRequest(String email, BigDecimal amount, Integer withdrawalMethodId) {
        User user = userRepository.findAndLockByEmail(email).orElseThrow(() -> new IllegalStateException("User not found."));
        if (user.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance for withdrawal.");
        }
        UserWithdrawalMethod method = withdrawalMethodRepository.findById(withdrawalMethodId).orElseThrow(() -> new IllegalStateException("Withdrawal method not found."));
        if (!method.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("Invalid withdrawal method selected.");
        }
        user.setBalance(user.getBalance().subtract(amount));
        userRepository.save(user);
        Transaction withdrawal = new Transaction();
        withdrawal.setUser(user);
        withdrawal.setAmount(amount);
        withdrawal.setType(Transaction.TransactionType.WITHDRAWAL);
        withdrawal.setStatus(Transaction.TransactionStatus.PENDING);
        withdrawal.setWithdrawalMethod(method);
        transactionRepository.save(withdrawal);
    }

    // --- Admin පැත්තට අදාළ අලුත් Methods ---
    public List<AdminTransactionViewDto> getAllTransactions() {
        return transactionRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::convertToAdminDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void processTransaction(Integer transactionId, String newStatus, String adminEmail) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalStateException("Transaction not found."));

        if (transaction.getStatus() != Transaction.TransactionStatus.PENDING) {
            throw new IllegalStateException("This transaction has already been processed.");
        }

        User adminUser = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new IllegalStateException("Admin user not found."));

        User targetUser = transaction.getUser();
        Transaction.TransactionStatus statusToSet = Transaction.TransactionStatus.valueOf(newStatus.toUpperCase());

        if (statusToSet == Transaction.TransactionStatus.COMPLETED) {
            if (transaction.getType() == Transaction.TransactionType.DEPOSIT) {
                targetUser.setBalance(targetUser.getBalance().add(transaction.getAmount()));
                userRepository.save(targetUser);
            }
        } else if (statusToSet == Transaction.TransactionStatus.REJECTED) {
            if (transaction.getType() == Transaction.TransactionType.WITHDRAWAL) {
                targetUser.setBalance(targetUser.getBalance().add(transaction.getAmount()));
                userRepository.save(targetUser);
            }
        }

        transaction.setStatus(statusToSet);
        transaction.setProcessedByAdminId(adminUser.getId());
        transactionRepository.save(transaction);

        // User ට notification එකක් යවනවා
        String userNotification = "Your " + transaction.getType().name().toLowerCase() +
                " of $" + transaction.getAmount() + " has been " + statusToSet.name().toLowerCase() + ".";
        messagingTemplate.convertAndSendToUser(targetUser.getEmail(), "/queue/notifications", userNotification);

        // Balance එක update වෙලා නම්, ඒකත් යවනවා
        if (statusToSet != Transaction.TransactionStatus.PENDING) {
            messagingTemplate.convertAndSendToUser(targetUser.getEmail(), "/queue/balance", targetUser.getBalance());
        }
    }

    // --- Helper Methods ---
    private AdminTransactionViewDto convertToAdminDto(Transaction transaction) {
        AdminTransactionViewDto dto = new AdminTransactionViewDto();
        dto.setTransactionId(transaction.getId());
        dto.setUserPublicId(transaction.getUser().getPublicUserId());
        dto.setUserName(transaction.getUser().getFirstName());
        dto.setType(transaction.getType().name());
        dto.setAmount(transaction.getAmount());
        dto.setStatus(transaction.getStatus().name());
        dto.setCreatedAt(transaction.getCreatedAt());
        dto.setReceiptUrl(transaction.getReceiptUrl());

        if (transaction.getWithdrawalMethod() != null) {
            UserWithdrawalMethod method = transaction.getWithdrawalMethod();
            String details = String.format("Bank: %s\nAcc Name: %s\nAcc No: %s",
                    method.getBankName(), method.getAccountHolderName(), method.getAccountNumber());
            dto.setWithdrawalMethodDetails(details);
        }
        return dto;
    }

    private TransactionDto convertToDto(Transaction transaction) {
        TransactionDto dto = new TransactionDto();
        dto.setId(transaction.getId());
        dto.setType(transaction.getType().name());
        dto.setAmount(transaction.getAmount());
        dto.setStatus(transaction.getStatus().name());
        dto.setCreatedAt(transaction.getCreatedAt());
        return dto;
    }

    public List<AdminTransactionViewDto> searchTransactions(String userPublicId) {
        return transactionRepository.findByUser_PublicUserIdContainingIgnoreCaseOrderByCreatedAtDesc(userPublicId)
                .stream()
                .map(this::convertToAdminDto)
                .collect(Collectors.toList());
    }
}