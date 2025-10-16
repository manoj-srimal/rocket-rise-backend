package com.game.crashgamev2.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
@Data
@AllArgsConstructor
public class BetConfirmationDto {
    private Integer betId;
    private BigDecimal betAmount;
    private String panelId;
}