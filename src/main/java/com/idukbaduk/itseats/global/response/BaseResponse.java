package com.idukbaduk.itseats.global.response;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
@SuperBuilder
public class BaseResponse {

    private final Integer httpStatus;
    private final String message;
    private final Object data;

    public static ResponseEntity<BaseResponse> toResponseEntity(Response response) {
        return ResponseEntity.status(response.getHttpStatus())
                .body(BaseResponse.builder()
                        .httpStatus(response.getHttpStatus().value())
                        .message(response.getMessage())
                        .build());
    }

    public static ResponseEntity<BaseResponse> toResponseEntity(HttpStatus httpStatus, String message) {
       return ResponseEntity.status(httpStatus)
                .body(BaseResponse.builder()
                        .httpStatus(httpStatus.value())
                        .message(message)
                        .build());
    }

    public static ResponseEntity<BaseResponse> toResponseEntity(Response response, Object data) {
        return ResponseEntity.status(response.getHttpStatus())
                .body(BaseResponse.builder()
                        .httpStatus(response.getHttpStatus().value())
                        .message(response.getMessage())
                        .data(data)
                        .build());
    }

    public static ResponseEntity<BaseResponse> toResponseEntity(HttpStatus httpStatus, String message, Object data) {
        return ResponseEntity.status(httpStatus)
                .body(BaseResponse.builder()
                        .httpStatus(httpStatus.value())
                        .message(message)
                        .data(data)
                        .build());
    }
}
