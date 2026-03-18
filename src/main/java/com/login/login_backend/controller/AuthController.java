package com.login.login_backend.controller;



import com.login.login_backend.dto.LoginRequest;
import com.login.login_backend.dto.LoginResponse;
import com.login.login_backend.dto.ForgotPasswordRequest;
import com.login.login_backend.dto.ForgotPasswordResponse;
import com.login.login_backend.dto.ResetPasswordRequest;
import com.login.login_backend.dto.ResetPasswordWithVerificationRequest;
import com.login.login_backend.dto.VerificationRequest;
import com.login.login_backend.dto.VerificationResponse;
import com.login.login_backend.dto.ChangePasswordRequest;
import com.login.login_backend.model.User;
import com.login.login_backend.repository.UserRepository;
import com.login.login_backend.security.JwtUtil;
import com.login.login_backend.service.AuditService;
import com.login.login_backend.service.EmailService;
import com.login.login_backend.service.VerificationService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.web.bind.annotation.*;



import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;



@RestController

@RequestMapping("/api/auth")

public class AuthController {



    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuditService auditService;

    private final EmailService emailService;

    private final VerificationService verificationService;



    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          AuditService auditService,
                          EmailService emailService,
                          VerificationService verificationService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
        this.emailService = emailService;
        this.verificationService = verificationService;
    }



    @PostMapping("/login")

    public LoginResponse login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {

        String ipAddress = getClientIpAddress(httpRequest);

        

        try {

            Authentication authentication = authenticationManager.authenticate(

                    new UsernamePasswordAuthenticationToken(

                            request.getEmail(), request.getPassword())

            );

            String token = jwtUtil.generateToken(authentication.getName());

            

            // Auditoría: Login exitoso

            auditService.logLogin(request.getEmail(), ipAddress, true);

            

            return new LoginResponse(token);

        } catch (Exception e) {

            // Auditoría: Login fallido

            auditService.logLogin(request.getEmail(), ipAddress, false);

            throw e;

        }

    }



    @PostMapping("/register")

    public User register(@RequestBody User user, HttpServletRequest httpRequest) {

        String ipAddress = getClientIpAddress(httpRequest);

        

        // Validar email sin revelar si existe

        if (!user.getEmail().contains("@") || !user.getEmail().contains(".")) {

            auditService.logRegistration(user.getEmail(), ipAddress, false);

            throw new RuntimeException("Email no válido");

        }

        

        // Validar contraseña

        if (user.getPassword().length() < 8) {

            auditService.logRegistration(user.getEmail(), ipAddress, false);

            throw new RuntimeException("La contraseña debe tener al menos 8 caracteres");

        }

        

        // Intentar guardar usuario - si el email ya existe, fallará silenciosamente

        try {

            User newUser = User.builder()

                    .firstName(user.getFirstName())

                    .lastName(user.getLastName())

                    .email(user.getEmail())

                    .password(passwordEncoder.encode(user.getPassword()))

                    .role("ROLE_USER")

                    .enabled(true)

                    .build();

            

            User savedUser = userRepository.save(newUser);

            

            // Auditoría: Registro exitoso

            auditService.logRegistration(user.getEmail(), ipAddress, true);

            

            return savedUser;

        } catch (Exception e) {

            // Auditoría: Registro fallido

            auditService.logRegistration(user.getEmail(), ipAddress, false);

            // Mensaje genérico que no revela si el email ya existe

            throw new RuntimeException("No se pudo procesar el registro. Intente con otro email.");

        }

    }



    private String getClientIpAddress(HttpServletRequest request) {

        String xForwardedFor = request.getHeader("X-Forwarded-For");

        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {

            return xForwardedFor.split(",")[0].trim();

        }

        

        String xRealIp = request.getHeader("X-Real-IP");

        if (xRealIp != null && !xRealIp.isEmpty()) {

            return xRealIp;

        }

        

        return request.getRemoteAddr();

    }



    @GetMapping("/me")

    public User me(@RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);

        String email = jwtUtil.getUsernameFromToken(token);

        return userRepository.findByEmail(email)

                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    }



    @PostMapping("/forgot-password")
    public ForgotPasswordResponse forgotPassword(@RequestBody ForgotPasswordRequest request, HttpServletRequest httpRequest) {
        System.out.println("🔥 FORGOT-PASSWORD RECIBIDO: " + request.getEmail());
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email no encontrado"));

        // Generar token único para reset
        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        // Generar código de verificación (RF-15)
        String verificationCode = verificationService.generateVerificationCode(user.getEmail());

        // Enviar email con código de verificación (asíncrono para no bloquear)
        new Thread(() -> {
            try {
                emailService.sendVerificationCode(user.getEmail(), verificationCode);
                System.out.println("✅ Email enviado exitosamente a " + user.getEmail());
            } catch (Exception e) {
                System.err.println("❌ Error enviando email: " + e.getMessage());
            }
        }).start();
        
        auditService.logPasswordReset(request.getEmail(), getClientIpAddress(httpRequest), false);
        
        ForgotPasswordResponse response = new ForgotPasswordResponse();
        response.setMessage("Se ha enviado un código de verificación a tu email. Por favor, verifica tu identidad para continuar.");
        response.setResetToken(resetToken);
        response.setVerificationCode(verificationCode);
        response.setRequiresVerification(true);
        response.setRemainingAttempts(3);
        
        return response;
    }

    @PostMapping("/verify-identity")
    public VerificationResponse verifyIdentity(@RequestBody VerificationRequest request, HttpServletRequest httpRequest) {
        // Validar código de verificación
        boolean isValid = verificationService.validateVerificationCode(request.getEmail(), request.getVerificationCode());
        
        if (!isValid) {
            int remainingAttempts = verificationService.getRemainingAttempts(request.getEmail());
            
            VerificationResponse response = new VerificationResponse();
            response.setVerified(false);
            response.setMessage("Código de verificación inválido o expirado.");
            response.setRemainingAttempts(remainingAttempts);
            
            return response;
        }
        
        // Obtener token de operación generado
        String operationToken = verificationService.getOperationToken(request.getEmail());
        
        auditService.logEvent(request.getEmail(), "IDENTITY_VERIFICATION", true, getClientIpAddress(httpRequest), "Verificación exitosa");
        
        VerificationResponse response = new VerificationResponse();
        response.setVerified(true);
        response.setMessage("Identidad verificada exitosamente.");
        response.setOperationToken(operationToken);
        
        return response;
    }

    @PostMapping("/reset-password-with-verification")
    public ResponseEntity<?> resetPasswordWithVerification(@RequestBody ResetPasswordWithVerificationRequest request, HttpServletRequest httpRequest) {
        System.out.println("🔥 RESET-WITH-VERIFICATION RECIBIDO:");
        System.out.println("  Email: " + request.getEmail());
        System.out.println("  ResetToken: " + request.getResetToken());
        System.out.println("  OperationToken: " + request.getOperationToken());
        System.out.println("  VerificationCode: " + request.getVerificationCode());
        System.out.println("  NewPassword: " + request.getNewPassword());
        
        try {
            // Validar token de operación
            System.out.println("🔍 Validando operationToken: " + request.getOperationToken());
            if (!verificationService.validateOperationToken(request.getOperationToken())) {
                System.out.println("❌ Token de operación inválido o expirado");
                return ResponseEntity.badRequest().body(Map.of("error", "Token de operación inválido o expirado"));
            }
            System.out.println("✅ Token de operación válido");
            
            // Validar que el token pertenece al email
            System.out.println("🔍 Obteniendo email del token...");
            String tokenEmail = verificationService.getEmailFromOperationToken(request.getOperationToken());
            System.out.println("🔍 Email del token: " + tokenEmail);
            System.out.println("🔍 Email de la request: " + request.getEmail());
            
            if (!tokenEmail.equals(request.getEmail())) {
                System.out.println("❌ Token de operación no válido para este email");
                return ResponseEntity.badRequest().body(Map.of("error", "Token de operación no válido para este email"));
            }
            System.out.println("✅ Email validado correctamente");
            
            // Buscar usuario por reset token
            System.out.println("🔍 Buscando usuario con resetToken: " + request.getResetToken());
            User user = userRepository.findByResetToken(request.getResetToken())
                    .orElseThrow(() -> new RuntimeException("Token de reset inválido o expirado"));
            System.out.println("✅ Usuario encontrado: " + user.getEmail());

            if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
                System.out.println("❌ Token de reset ha expirado");
                return ResponseEntity.badRequest().body(Map.of("error", "Token de reset ha expirado"));
            }
            System.out.println("✅ Token de reset válido");
            
            // Actualizar contraseña
            System.out.println("🔍 Actualizando contraseña...");
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            user.setResetToken(null);
            user.setResetTokenExpiry(null);
            user.setPasswordChangedAt(LocalDateTime.now());
            userRepository.save(user);
            System.out.println("✅ Contraseña actualizada en BD");

            // Limpiar tokens
            System.out.println("🔍 Limpiando tokens...");
            verificationService.clearOperationToken(request.getOperationToken());
            verificationService.clearVerificationCode(request.getEmail());
            System.out.println("✅ Tokens limpiados");

            auditService.logPasswordReset(user.getEmail(), getClientIpAddress(httpRequest), true);

            System.out.println("✅ Contraseña actualizada exitosamente para: " + user.getEmail());
            return ResponseEntity.ok(Map.of("message", "Contraseña actualizada exitosamente"));
        } catch (Exception e) {
            System.err.println("❌ Error actualizando contraseña: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Error al actualizar la contraseña: " + e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestBody ResetPasswordRequest request, HttpServletRequest httpRequest) {

        User user = userRepository.findByResetToken(request.getToken())

                .orElseThrow(() -> new RuntimeException("Token inválido o expirado"));



        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {

            throw new RuntimeException("Token ha expirado");

        }



        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        user.setResetToken(null);

        user.setResetTokenExpiry(null);

        user.setPasswordChangedAt(LocalDateTime.now());

        userRepository.save(user);



        auditService.logPasswordReset(user.getEmail(), getClientIpAddress(httpRequest), true);



        return "Contraseña actualizada exitosamente";

    }



    @PostMapping("/change-password")

    public String changePassword(@RequestBody ChangePasswordRequest request, 

                                   @RequestHeader("Authorization") String token, HttpServletRequest httpRequest) {

        String email = jwtUtil.getUsernameFromToken(token);

        User user = userRepository.findByEmail(email)

                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));



        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {

            throw new RuntimeException("Contraseña actual incorrecta");

        }



        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        user.setPasswordChangedAt(LocalDateTime.now());

        userRepository.save(user);



        auditService.logPasswordChange(user.getEmail(), getClientIpAddress(httpRequest), true);



        return "Contraseña cambiada exitosamente";

    }

}

