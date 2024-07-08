package com.toy.project.emodiary.authentication.service;

import com.toy.project.emodiary.authentication.dto.*;
import com.toy.project.emodiary.authentication.entity.Authority;
import com.toy.project.emodiary.authentication.entity.Users;
import com.toy.project.emodiary.authentication.jwt.JwtTokenProvider;
import com.toy.project.emodiary.authentication.repository.UserRepository;
import com.toy.project.emodiary.authentication.util.RedisUtil;
import com.toy.project.emodiary.authentication.util.SecurityUtil;
import com.toy.project.emodiary.common.dto.MessageDto;
import com.toy.project.emodiary.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.toy.project.emodiary.common.exception.ErrorCode;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthService {
    @Value("${jwt.token.refresh-expiration-time}")
    private long refreshTokenExpiration;

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;

    private final SecurityUtil securityUtil;
    private final RedisUtil redisUtil;

    private static final String AUTHORITIES_KEY = "auth";
    private static final String REFRESH_TOKEN_SUFFIX = "(refreshToken)"; // Redis Key 중복 방지를 위한 접미사

    // 회원가입
    @Transactional
    public ResponseEntity<MessageDto> signUp(SignUpDto dto) {
        // 이메일 중복 검사
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        // ROLE_USER 권한 부여
        Authority authority = new Authority();
        authority.setAuthorityName("ROLE_USER");

        Users users = new Users();
        users.setEmail(dto.getEmail());
        users.setPassword(bCryptPasswordEncoder.encode(dto.getPassword()));
        users.setNickname(dto.getNickname());
        users.setAuthorities(Collections.singleton(authority));
        userRepository.save(users);

        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageDto("회원가입에 성공했습니다."));
    }

    // 로그인 (이메일, 비밀번호)
    @Transactional
    public ResponseEntity<SignInSuccessDto> signIn(SignInDto dto) {
        try {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword());

            //authentication 객제 검증
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

            // Access Token, Refresh Token 생성
            String accessToken = jwtTokenProvider.generateAccessToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken();

            // Redis에 Refresh Token 저장
            redisUtil.setData(authentication.getName() + REFRESH_TOKEN_SUFFIX, refreshToken, refreshTokenExpiration / 1000);

            Users user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_NOT_FOUND));

            return ResponseEntity.status(HttpStatus.OK).body(new SignInSuccessDto(
                    new UserDto(user.getEmail(), user.getNickname(), user.getImageUrl(), user.getProvider()),
                    new TokenDto(accessToken, refreshToken)
            ));
        } catch (BadCredentialsException e) {
            throw new CustomException(ErrorCode.BAD_CREDENTIALS);
        }
    }

    // 로그아웃
    @Transactional
    public ResponseEntity<MessageDto> logOut() {
        try {
            Users currentUser = securityUtil.getCurrentUser();

            // Redis에 저장된 Refresh Token 삭제
            redisUtil.deleteData(currentUser.getEmail() + REFRESH_TOKEN_SUFFIX);

            return ResponseEntity.status(HttpStatus.OK).body(new MessageDto("로그아웃 완료"));
        } catch (Exception e) {
            throw new CustomException(ErrorCode.LOGOUT_FAILURE);
        }
    }

    // 유저 정보
    public ResponseEntity<UserDto> userInfo() {
        Users currentUser = securityUtil.getCurrentUser();

        return ResponseEntity.status(HttpStatus.OK).body(new UserDto(
                currentUser.getEmail(),
                currentUser.getNickname(),
                currentUser.getImageUrl(),
                currentUser.getProvider()));
    }

    // Access Token 재발급
    public ResponseEntity<TokenDto> reIssueAccessToken(String accessToken, String refreshToken) {
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        } else {
            throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
        }

        String email = jwtTokenProvider.getClaims(accessToken).getSubject();
        String authorities = jwtTokenProvider.getClaims(accessToken).get(AUTHORITIES_KEY).toString(); // 권한 정보

        String storedRefreshToken = redisUtil.getData(email + REFRESH_TOKEN_SUFFIX); // Redis에 저장된 Refresh Token을 가져옴

        // Refresh Token 유효성 검사
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken) || !jwtTokenProvider.validateToken(storedRefreshToken)) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        String newAccessToken = jwtTokenProvider.reissueAccessToken(email, authorities); // Access Token 재발급

        return ResponseEntity.status(HttpStatus.OK).body(new TokenDto(newAccessToken, null));
    }

    // Refresh Token 재발급
    public ResponseEntity<TokenDto> reIssueRefreshToken() {
        Users currentUser = securityUtil.getCurrentUser();

        String storedRefreshToken = redisUtil.getData(currentUser.getEmail() + REFRESH_TOKEN_SUFFIX); // Redis에 저장된 Refresh Token을 가져옴

        // Refresh Token 유효성 검사
        if (storedRefreshToken == null) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        String newRefreshToken = jwtTokenProvider.generateRefreshToken(); // Refresh Token 재발급
        redisUtil.setData(currentUser.getEmail() + REFRESH_TOKEN_SUFFIX, newRefreshToken, refreshTokenExpiration / 1000); // Redis에 새로운 Refresh Token 저장

        return ResponseEntity.status(HttpStatus.OK).body(new TokenDto(null, newRefreshToken));
    }

    // 회원탈퇴
    @Transactional
    public ResponseEntity<MessageDto> withdrawal() {
        Users currentUser = securityUtil.getCurrentUser();

        redisUtil.deleteData(currentUser.getEmail() + REFRESH_TOKEN_SUFFIX);


        userRepository.delete(currentUser);


        return ResponseEntity.status(HttpStatus.OK).body(new MessageDto("회원 탈퇴가 완료되었습니다."));
    }

    // 회원 정보 수정
    @Transactional
    public ResponseEntity<UserDto> editUser(UserEditDto dto) {
        Users currentUser = securityUtil.getCurrentUser();

        if (dto.getIsImageChange()) {
            // 기존 프로필 이미지 삭제
//            if (dto.getImageUrl() != null && s3Util.isFileExists(currentUser.getImageUrl())) {
//                s3Util.deleteFile(currentUser.getImageUrl());
//            }

            currentUser.setImageUrl(dto.getImageUrl());
        }

        currentUser.setNickname(dto.getNickname());
        userRepository.save(currentUser);

        return ResponseEntity.status(HttpStatus.OK).body(new UserDto(
                currentUser.getEmail(),
                currentUser.getNickname(),
                currentUser.getImageUrl(),
                currentUser.getProvider()));
    }

    // 구글 로그인 및 카카오 로그인 보류
}
