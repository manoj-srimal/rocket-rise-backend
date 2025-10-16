package com.game.crashgamev2.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PlaceBetDto {
    private BigDecimal betAmount;
    private String panelId;
    private BigDecimal autoCashOutAt;
}