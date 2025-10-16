package com.game.crashgamev2.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransactionRequestDto {
    private BigDecimal amount;
    private String paymentMethod;
    private String receiptUrl;
    private Integer withdrawalMethodId;
}