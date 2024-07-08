package com.toy.project.emodiary.authentication.controller;


import com.toy.project.emodiary.authentication.dto.*;
import com.toy.project.emodiary.authentication.service.AuthService;
import com.toy.project.emodiary.common.dto.MessageDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup") // 회원가입
    public ResponseEntity<MessageDto> signUp(@RequestBody @Valid SignUpDto dto) {
        return authService.signUp(dto);
    }

    @PostMapping("/signin") // 로그인
    public ResponseEntity<SignInSuccessDto> signIn(@RequestBody @Valid SignInDto dto) {
        return authService.signIn(dto);
    }

    @PostMapping("/logout") // 로그아웃
    public ResponseEntity<MessageDto> logOut() {
        return authService.logOut();
    }

    @GetMapping("/users") // 로그인 유저 정보
    public ResponseEntity<UserDto> info() {
        return authService.userInfo();
    }

    @DeleteMapping("/users") // 회원탈퇴
    public ResponseEntity<MessageDto> withdrawal() {
        return authService.withdrawal();
    }

    @PutMapping("/users") // 회원 정보 수정
    public ResponseEntity<UserDto> editUser(@RequestBody @Valid UserEditDto dto) {
        return authService.editUser(dto);
    }

    @PostMapping("/token/access") // Access Token 재발급
    public ResponseEntity<TokenDto> reIssueAccessToken(
            @RequestHeader(value = "AccessToken") String accessToken,
            @RequestHeader(value = "RefreshToken") String refreshToken
    ) {
        return authService.reIssueAccessToken(accessToken, refreshToken);
    }

    @PostMapping("/token/refresh") // Refresh Token 재발급
    public ResponseEntity<TokenDto> reIssueRefreshToken() {
        return authService.reIssueRefreshToken();
    }

}
