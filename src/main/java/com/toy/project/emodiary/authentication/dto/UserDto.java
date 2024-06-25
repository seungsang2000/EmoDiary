package com.toy.project.emodiary.authentication.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserDto {
    private String email;
    private String nickname;
    private String profileImage;
    private String provider;
}
