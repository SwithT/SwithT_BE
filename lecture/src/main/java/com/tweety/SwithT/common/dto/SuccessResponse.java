package com.tweety.SwithT.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SuccessResponse {
    private int statusCode;
    private String statusMessage;
    private Object result;
    @Builder
    public SuccessResponse(HttpStatus httpStatus, String statusMessage, Object result){
        this.statusCode = httpStatus.value();
        this.statusMessage = statusMessage;
        this.result = result;
    }
}
