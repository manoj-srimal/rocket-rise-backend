package com.game.crashgamev2.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class LiveBetDto {
    private String username;
    private BigDecimal betAmount;
    private String status;
    private BigDecimal cashOutAt;
    private BigDecimal winnings;
}