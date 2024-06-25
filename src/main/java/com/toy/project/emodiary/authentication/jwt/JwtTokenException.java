package com.toy.project.emodiary.authentication.jwt;

import io.jsonwebtoken.JwtException;

public class JwtTokenException extends JwtException {
    public JwtTokenException(String message) {
        super(message);
    }
}
