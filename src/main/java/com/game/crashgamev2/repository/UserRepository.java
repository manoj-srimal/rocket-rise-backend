package com.game.crashgamev2.repository;

import com.game.crashgamev2.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findAndLockByEmail(String email);

    List<User> findByPublicUserIdContainingIgnoreCase(String publicUserId);

    @Query(value = "SELECT u.public_user_id FROM users u ORDER BY u.id DESC LIMIT 1", nativeQuery = true)
    Optional<String> findLastPublicUserId();

    @Query(value = "SELECT DATE_FORMAT(created_at, '%Y-%m') as month, COUNT(id) as count FROM users GROUP BY month ORDER BY month ASC", nativeQuery = true)
    List<Map<String, Object>> findMonthlySignups();

    @Query(value = "SELECT " +
            "u.first_name AS adminName, " +
            "COALESCE(SUM(CASE WHEN t.type = 'DEPOSIT' AND t.status = 'COMPLETED' THEN t.amount ELSE 0 END), 0) AS totalDeposits, " +
            "COALESCE(SUM(CASE WHEN t.type = 'WITHDRAWAL' AND t.status = 'COMPLETED' THEN t.amount ELSE 0 END), 0) AS totalWithdrawals " +
            "FROM users u " +
            "LEFT JOIN transactions t ON u.id = t.processed_by_admin_id " +
            "WHERE u.role IN ('ADMIN', 'SUPER_ADMIN') " +
            "GROUP BY u.id, u.first_name", nativeQuery = true)
    List<Map<String, Object>> getAdminPerformanceStats();
}