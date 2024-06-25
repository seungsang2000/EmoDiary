package com.toy.project.emodiary.authentication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class UserEditDto {
    @NotBlank(message = "닉네임을 입력해주세요.")
    @Pattern(regexp = "^(?=.*[a-z0-9가-힣])[a-z0-9가-힣]{2,16}$" , message = "닉네임은 특수문자를 포함하지 않은 2~16자리로 입력해주세요.")
    private String nickname;
    private String imageUrl;
    private Boolean isImageChange;
}
