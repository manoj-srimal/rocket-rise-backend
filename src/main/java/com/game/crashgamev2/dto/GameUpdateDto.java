package com.game.crashgamev2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameUpdateDto {
    private String status;
    private double multiplier;
    private int countdown;
}