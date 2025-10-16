package com.game.crashgamev2.dto;
import lombok.Data;
@Data
public class ResetPasswordDto {
    private String token;
    private String newPassword;
}