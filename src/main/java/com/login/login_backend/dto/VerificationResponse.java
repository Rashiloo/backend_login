package com.login.login_backend.dto;

public class VerificationResponse {

    private boolean verified;
    private String message;
    private String operationToken;
    private int remainingAttempts;
    private long expirationTime;

    public VerificationResponse() {
    }

    public VerificationResponse(boolean verified, String message) {
        this.verified = verified;
        this.message = message;
    }

    public VerificationResponse(boolean verified, String message, String operationToken) {
        this.verified = verified;
        this.message = message;
        this.operationToken = operationToken;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getOperationToken() {
        return operationToken;
    }

    public void setOperationToken(String operationToken) {
        this.operationToken = operationToken;
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
