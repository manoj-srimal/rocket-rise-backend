package com.game.crashgamev2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminPerformanceDto {
    private String adminName;
    private BigDecimal totalDeposits;
    private BigDecimal totalWithdrawals;
    private BigDecimal profit;
}