package com.login.login_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ForgotPasswordRequest {
    private String email;
}
