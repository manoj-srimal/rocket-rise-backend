package com.game.crashgamev2.dto;

import lombok.Data;

@Data
public class SignUpRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String mobileNumber;
    private String password;
}