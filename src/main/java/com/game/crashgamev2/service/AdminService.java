package com.game.crashgamev2.service;

import com.game.crashgamev2.Game;
import com.game.crashgamev2.Transaction;
import com.game.crashgamev2.User;
import com.game.crashgamev2.dto.AdminPerformanceDto;
import com.game.crashgamev2.dto.AdminStatsDto;
import com.game.crashgamev2.dto.GameSignalDto;
import com.game.crashgamev2.dto.UserManagementDto;
import com.game.crashgamev2.repository.GameRepository;
import com.game.crashgamev2.repository.TransactionRepository;
import com.game.crashgamev2.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final GameRepository gameRepository;

    public AdminStatsDto getDashboardStats() {
        return AdminStatsDto.builder()
                .totalUsers(userRepository.count())
                .totalDeposits(transactionRepository.sumAmountByType(Transaction.TransactionType.DEPOSIT))
                .totalWithdrawals(transactionRepository.sumAmountByType(Transaction.TransactionType.WITHDRAWAL))
                .pendingTransactions(transactionRepository.countByStatus(Transaction.TransactionStatus.PENDING))
                .monthlySignups(userRepository.findMonthlySignups())
                .build();
    }

    public List<UserManagementDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToUserDto)
                .collect(Collectors.toList());
    }

    public List<UserManagementDto> searchUsers(String query) {
        return userRepository.findByPublicUserIdContainingIgnoreCase(query).stream()
                .map(this::convertToUserDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserManagementDto updateUserStatus(Integer userId, String newStatus) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(User.Status.valueOf(newStatus.toUpperCase()));
        User updatedUser = userRepository.save(user);
        return convertToUserDto(updatedUser);
    }

    private UserManagementDto convertToUserDto(User user) {
        return new UserManagementDto(
                user.getId(),
                user.getPublicUserId(),
                user.getFirstName() + " " + user.getLastName(),
                user.getEmail(),
                user.getMobileNumber(),
                user.getBalance(),
                user.getStatus().name()
        );
    }

    public List<AdminPerformanceDto> getAdminPerformance() {
        List<Map<String, Object>> results = userRepository.getAdminPerformanceStats();
        List<AdminPerformanceDto> performanceList = new ArrayList<>();

        for (Map<String, Object> result : results) {
            String adminName = (String) result.get("adminName");
            BigDecimal totalDeposits = (BigDecimal) result.get("totalDeposits");
            BigDecimal totalWithdrawals = (BigDecimal) result.get("totalWithdrawals");
            BigDecimal profit = totalDeposits.subtract(totalWithdrawals);

            performanceList.add(new AdminPerformanceDto(adminName, totalDeposits, totalWithdrawals, profit));
        }
        return performanceList;
    }

    @Transactional
    public UserManagementDto updateUserRole(Integer userId, String newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // String එක, අදාළ Enum value එකට හරවනවා
        User.Role roleToSet = User.Role.valueOf(newRole.toUpperCase());

        user.setRole(roleToSet);
        User updatedUser = userRepository.save(user);
        return convertToUserDto(updatedUser);
    }

    public List<GameSignalDto> getGameSignals() {
        return gameRepository.findTop10ByStatusOrderByIdAsc(Game.Status.PENDING)
                .stream()
                .map(game -> new GameSignalDto(game.getId(), game.getCrashPoint()))
                .collect(Collectors.toList());
    }
}