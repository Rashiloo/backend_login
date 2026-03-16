package com.login.login_backend.controller;

import com.login.login_backend.dto.LoginRequest;
import com.login.login_backend.dto.LoginResponse;
import com.login.login_backend.dto.ForgotPasswordRequest;
import com.login.login_backend.dto.ResetPasswordRequest;
import com.login.login_backend.dto.ChangePasswordRequest;
import com.login.login_backend.model.User;
import com.login.login_backend.repository.UserRepository;
import com.login.login_backend.security.JwtUtil;
import com.login.login_backend.service.AuditService;
import com.login.login_backend.service.EmailService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
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

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          AuditService auditService,
                          EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
        this.emailService = emailService;
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
    public String forgotPassword(@RequestBody ForgotPasswordRequest request, HttpServletRequest httpRequest) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email no encontrado"));

        // Generar token único
        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        // Enviar email con el token
        try {
            // emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
            auditService.logPasswordReset(request.getEmail(), getClientIpAddress(httpRequest), false);
            return "Se ha enviado un email con instrucciones para recuperar tu contraseña";
        } catch (Exception e) {
            // Si falla el envío de email, limpiar el token y notificar el error
            user.setResetToken(null);
            user.setResetTokenExpiry(null);
            userRepository.save(user);
            
            auditService.logPasswordReset(request.getEmail(), getClientIpAddress(httpRequest), false);
            throw new RuntimeException("Error al enviar el email de recuperación. Por favor, intenta más tarde.");
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
