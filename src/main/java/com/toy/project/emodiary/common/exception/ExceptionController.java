package com.toy.project.emodiary.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

import static com.toy.project.emodiary.common.exception.ErrorCode.INVALID_FIELD;

@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
        ObjectError objectError = Objects.requireNonNull(exception.getBindingResult().getAllErrors().stream().findFirst().orElse(null));
        return ErrorResponse.responseEntity(INVALID_FIELD, objectError.getDefaultMessage());
    }

    @ExceptionHandler(value = CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        return ErrorResponse.responseEntity(e.getErrorCode());
    }
}
