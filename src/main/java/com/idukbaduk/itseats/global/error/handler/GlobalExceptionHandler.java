package com.idukbaduk.itseats.global.error.handler;

import com.idukbaduk.itseats.global.error.core.BaseException;
import com.idukbaduk.itseats.global.error.core.ErrorCode;
import com.idukbaduk.itseats.global.error.core.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import com.idukbaduk.itseats.payment.error.PaymentException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        if (fieldError == null) {
            return getErrorResponse(e, GlobalErrorCode.INVALID_INPUT_VALUE);
        }

        String errorMessage = fieldError.getDefaultMessage();
        return ResponseEntity
                .status(GlobalErrorCode.INVALID_INPUT_VALUE.getStatus())
                .body(ErrorResponse.of(GlobalErrorCode.INVALID_INPUT_VALUE, errorMessage));
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException e) {
        // PaymentException이면 originalErrorBody 포함해서 내려줌
        if (e instanceof PaymentException) {
            PaymentException pe = (PaymentException) e;
            return ResponseEntity
                .status(pe.getErrorCode().getStatus())
                .body(ErrorResponse.of(pe.getErrorCode(), e.getMessage(), pe.getOriginalErrorBody()));
        }
        return getErrorResponse(e, e.getErrorCode());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        log.error("RuntimeException 발생: {}", e.getMessage(), e);
        return getErrorResponse(e, GlobalErrorCode.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Exception 발생: {}", e.getMessage(), e);
        return getErrorResponse(e, GlobalErrorCode.INTERNAL_SERVER_ERROR);
    }

    private static ResponseEntity<ErrorResponse> getErrorResponse(Exception e, GlobalErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode));
    }

    private static ResponseEntity<ErrorResponse> getErrorResponse(Exception e, ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode, e.getMessage()));
    }
}
