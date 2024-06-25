package com.toy.project.emodiary.authentication.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;

@Builder
public record JwtErrorResponse(String timestamp, int status, String error, String message) {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public String convertToJson() throws JsonProcessingException {
        return objectMapper.writeValueAsString(this);
    }
}
