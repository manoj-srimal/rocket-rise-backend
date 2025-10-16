package com.game.crashgamev2.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class AdminStatsDto {
    private long totalUsers;
    private BigDecimal totalDeposits;
    private BigDecimal totalWithdrawals;
    private long pendingTransactions;
    private List<Map<String, Object>> monthlySignups;
}