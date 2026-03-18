package com.login.login_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class VerificationRequest {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    private String email;

    @NotBlank(message = "El código de verificación es obligatorio")
    @Pattern(regexp = "\\d{6}", message = "El código debe tener 6 dígitos numéricos")
    private String verificationCode;

    @NotBlank(message = "El tipo de acción es obligatorio")
    private String actionType;

    // Token temporal para la operación (opcional, para operaciones que requieren token previo)
    private String operationToken;

    public VerificationRequest() {
    }

    public VerificationRequest(String email, String verificationCode, String actionType) {
        this.email = email;
        this.verificationCode = verificationCode;
        this.actionType = actionType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getOperationToken() {
        return operationToken;
    }

    public void setOperationToken(String operationToken) {
        this.operationToken = operationToken;
    }
}
