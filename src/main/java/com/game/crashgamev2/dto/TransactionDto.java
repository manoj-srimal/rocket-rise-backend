package com.game.crashgamev2.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
public class TransactionDto {
    private Integer id;
    private String type;
    private BigDecimal amount;
    private String status;
    private Timestamp createdAt;
}