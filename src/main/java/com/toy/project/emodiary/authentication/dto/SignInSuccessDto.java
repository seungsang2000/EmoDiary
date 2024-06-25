package com.toy.project.emodiary.authentication.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignInSuccessDto {
    private UserDto user;
    private TokenDto token;
}
