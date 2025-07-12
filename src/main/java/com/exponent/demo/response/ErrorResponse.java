package com.exponent.demo.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {
    private String localized_message;
    private String message;
    private String error;
}
