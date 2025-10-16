package com.game.crashgamev2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class UserManagementDto {
    private Integer id;
    private String publicUserId;
    private String name;
    private String email;
    private String mobileNumber;
    private BigDecimal balance;
    private String status;
}