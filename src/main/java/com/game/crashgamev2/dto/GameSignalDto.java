package com.game.crashgamev2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class GameSignalDto {
    private Integer gameId;
    private BigDecimal crashPoint;
}