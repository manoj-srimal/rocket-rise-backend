package com.game.crashgamev2.repository;

import com.game.crashgamev2.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    List<Transaction> findAllByUser_EmailOrderByCreatedAtDesc(String email);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = :type")
    BigDecimal sumAmountByType(Transaction.TransactionType type);

    long countByStatus(Transaction.TransactionStatus status);

    List<Transaction> findAllByOrderByCreatedAtDesc();

    List<Transaction> findByUser_PublicUserIdContainingIgnoreCaseOrderByCreatedAtDesc(String publicUserId);
}