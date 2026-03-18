package com.login.login_backend.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VerificationService {

    private final Map<String, VerificationEntry> verificationCodes = new ConcurrentHashMap<>();
    private final Map<String, OperationToken> operationTokens = new ConcurrentHashMap<>();
    
    private static final int CODE_EXPIRATION_MINUTES = 15;
    private static final int TOKEN_EXPIRATION_MINUTES = 60;
    private static final int MAX_ATTEMPTS = 3;
    
    private final Random random = new Random();
    
    /**
     * Genera un código de verificación de 6 dígitos
     */
    public String generateVerificationCode(String email) {
        String code = String.format("%06d", random.nextInt(1000000));
        
        VerificationEntry entry = new VerificationEntry(
            code,
            email,
            LocalDateTime.now(),
            0
        );
        
        verificationCodes.put(email, entry);
        
        return code;
    }
    
    /**
     * Valida un código de verificación
     */
    public boolean validateVerificationCode(String email, String code) {
        VerificationEntry entry = verificationCodes.get(email);
        
        if (entry == null) {
            return false;
        }
        
        // Verificar expiración
        if (isCodeExpired(entry)) {
            verificationCodes.remove(email);
            return false;
        }
        
        // Verificar intentos máximos
        if (entry.attempts >= MAX_ATTEMPTS) {
            verificationCodes.remove(email);
            return false;
        }
        
        // Incrementar intentos
        entry.attempts++;
        
        // Validar código
        if (entry.code.equals(code)) {
            // Generar token de operación para continuar con el proceso
            String operationToken = generateOperationToken(email);
            entry.verified = true;
            entry.operationToken = operationToken;
            return true;
        }
        
        return false;
    }
    
    /**
     * Genera un token de operación temporal
     */
    public String generateOperationToken(String email) {
        String token = java.util.UUID.randomUUID().toString();
        
        OperationToken opToken = new OperationToken(
            token,
            email,
            LocalDateTime.now()
        );
        
        operationTokens.put(token, opToken);
        
        return token;
    }
    
    /**
     * Valida un token de operación
     */
    public boolean validateOperationToken(String token) {
        OperationToken opToken = operationTokens.get(token);
        
        if (opToken == null) {
            return false;
        }
        
        if (isTokenExpired(opToken)) {
            operationTokens.remove(token);
            return false;
        }
        
        return true;
    }
    
    /**
     * Obtiene el email asociado a un token de operación
     */
    public String getEmailFromOperationToken(String token) {
        OperationToken opToken = operationTokens.get(token);
        return opToken != null ? opToken.email : null;
    }
    
    /**
     * Limpia el código de verificación después de uso exitoso
     */
    public void clearVerificationCode(String email) {
        verificationCodes.remove(email);
    }
    
    /**
     * Limpia el token de operación después de uso
     */
    public void clearOperationToken(String token) {
        operationTokens.remove(token);
    }
    
    /**
     * Obtiene intentos restantes
     */
    public int getRemainingAttempts(String email) {
        VerificationEntry entry = verificationCodes.get(email);
        if (entry == null) {
            return 0;
        }
        return Math.max(0, MAX_ATTEMPTS - entry.attempts);
    }
    
    /**
     * Obtiene el token de operación generado después de verificación exitosa
     */
    public String getOperationToken(String email) {
        VerificationEntry entry = verificationCodes.get(email);
        return entry != null ? entry.operationToken : null;
    }
    
    private boolean isCodeExpired(VerificationEntry entry) {
        return ChronoUnit.MINUTES.between(entry.createdAt, LocalDateTime.now()) > CODE_EXPIRATION_MINUTES;
    }
    
    private boolean isTokenExpired(OperationToken token) {
        return ChronoUnit.MINUTES.between(token.createdAt, LocalDateTime.now()) > TOKEN_EXPIRATION_MINUTES;
    }
    
    private static class VerificationEntry {
        String code;
        String email;
        LocalDateTime createdAt;
        int attempts;
        boolean verified;
        String operationToken;
        
        VerificationEntry(String code, String email, LocalDateTime createdAt, int attempts) {
            this.code = code;
            this.email = email;
            this.createdAt = createdAt;
            this.attempts = attempts;
            this.verified = false;
        }
    }
    
    private static class OperationToken {
        String token;
        String email;
        LocalDateTime createdAt;
        
        OperationToken(String token, String email, LocalDateTime createdAt) {
            this.token = token;
            this.email = email;
            this.createdAt = createdAt;
        }
    }
}
