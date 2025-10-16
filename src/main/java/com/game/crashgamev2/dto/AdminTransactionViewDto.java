package com.game.crashgamev2.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
public class AdminTransactionViewDto {
    private Integer transactionId;
    private String userPublicId;
    private String userName;
    private String type;
    private BigDecimal amount;
    private String status;
    private Timestamp createdAt;
    private String receiptUrl;
    private String withdrawalMethodDetails;
}