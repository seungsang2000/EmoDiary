package com.toy.project.emodiary.authentication.util;

import com.toy.project.emodiary.authentication.entity.Users;
import com.toy.project.emodiary.authentication.repository.UserRepository;
import com.toy.project.emodiary.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import com.toy.project.emodiary.common.exception.ErrorCode;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SecurityUtil {
    private final UserRepository userRepository;

    public Users getCurrentUser() {
        // 현재 사용자의 이메일을 가져옴
        Optional<Users> user = getCurrentUsername().flatMap(userRepository::findByEmail);

        // 사용자가 없는 경우
        if (user.isEmpty()) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        return user.get();
    }

    private Optional<String> getCurrentUsername() {
        // SecurityContext에서 Authentication 객체를 꺼내온다.
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증 정보가 없는 경우
        if (authentication == null) {
            return Optional.empty();
        }

        String username = null;
        if (authentication.getPrincipal() instanceof UserDetails springSecurityUser) { // 인증된 주체의 정보가 UserDetails 인스턴스인 경우
            username = springSecurityUser.getUsername();
        } else if (authentication.getPrincipal() instanceof String) { // 인증된 주체의 정보가 문자열인 경우
            username = (String) authentication.getPrincipal();
        }

        return Optional.ofNullable(username);
    }
}
