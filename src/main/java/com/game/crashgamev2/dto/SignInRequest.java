package com.game.crashgamev2.dto;

import lombok.Data;

@Data
public class SignInRequest {
    private String email;
    private String password;
}