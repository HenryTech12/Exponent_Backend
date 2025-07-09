package com.exponent.demo.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@JsonSerialize
public class AuthResponse {

    private String username;
    private String jwtToken;
}
