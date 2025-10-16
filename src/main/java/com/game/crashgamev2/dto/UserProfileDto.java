package com.game.crashgamev2.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UserProfileDto {
    private String publicUserId;
    private String firstName;
    private String lastName;
    private String email;
    private BigDecimal balance;
}