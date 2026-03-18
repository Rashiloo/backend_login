package com.login.login_backend.dto;

public class ForgotPasswordResponse {

    private String message;
    private String resetToken;
    private String verificationCode;
    private boolean requiresVerification;
    private int remainingAttempts;
    private long expirationTime;

    public ForgotPasswordResponse() {
    }

    public ForgotPasswordResponse(String message, String resetToken, String verificationCode) {
        this.message = message;
        this.resetToken = resetToken;
        this.verificationCode = verificationCode;
        this.requiresVerification = true;
    }

    public ForgotPasswordResponse(String message, String resetToken) {
        this.message = message;
        this.resetToken = resetToken;
        this.requiresVerification = false;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public boolean isRequiresVerification() {
        return requiresVerification;
    }

    public void setRequiresVerification(boolean requiresVerification) {
        this.requiresVerification = requiresVerification;
    }

    public int getRemainingAttempts() {
        return remainingAttempts;
    }

    public void setRemainingAttempts(int remainingAttempts) {
        this.remainingAttempts = remainingAttempts;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(long expirationTime) {
        this.expirationTime = expirationTime;
    }
}
