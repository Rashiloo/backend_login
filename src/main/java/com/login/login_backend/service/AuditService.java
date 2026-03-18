package com.login.login_backend.service;



import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import java.util.ArrayList;

import java.util.List;



@Service

public class AuditService {

    

    private static class AuditLog {

        private String action;

        private String email;

        private String ipAddress;

        private LocalDateTime timestamp;

        private String details;

        

        public AuditLog(String action, String email, String ipAddress, String details) {

            this.action = action;

            this.email = email;

            this.ipAddress = ipAddress;

            this.timestamp = LocalDateTime.now();

            this.details = details;

        }

        

        // Getters

        public String getAction() { return action; }

        public String getEmail() { return email; }

        public String getIpAddress() { return ipAddress; }

        public LocalDateTime getTimestamp() { return timestamp; }

        public String getDetails() { return details; }

    }

    

    private final List<AuditLog> auditLogs = new ArrayList<>();

    

    public void logLogin(String email, String ipAddress, boolean success) {

        String details = success ? "Login exitoso" : "Login fallido";

        auditLogs.add(new AuditLog("LOGIN", email, ipAddress, details));

    }

    

    public void logRegistration(String email, String ipAddress, boolean success) {

        String details = success ? "Registro exitoso" : "Registro fallido";

        auditLogs.add(new AuditLog("REGISTER", email, ipAddress, details));

    }

    

    public void logLogout(String email, String ipAddress) {

        auditLogs.add(new AuditLog("LOGOUT", email, ipAddress, "Cierre de sesión"));

    }

    

    public void logPasswordReset(String email, String ipAddress, boolean success) {

        String details = success ? "Recuperación de contraseña exitosa" : "Recuperación de contraseña fallida";

        auditLogs.add(new AuditLog("PASSWORD_RESET", email, ipAddress, details));

    }

    

    public void logPasswordChange(String email, String ipAddress, boolean success) {

        String details = success ? "Cambio de contraseña exitoso" : "Cambio de contraseña fallido";

        auditLogs.add(new AuditLog("PASSWORD_CHANGE", email, ipAddress, details));

    }

    public void logEvent(String email, String action, boolean success, String ipAddress, String message) {
        String details = success ? message : message + " (fallido)";
        auditLogs.add(new AuditLog(action, email, ipAddress, details));
    }

    public List<AuditLog> getAuditLogs() {

        return new ArrayList<>(auditLogs);

    }

}

