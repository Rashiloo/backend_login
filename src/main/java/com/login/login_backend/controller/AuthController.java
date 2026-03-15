package com.login.login_backend.controller;

import com.login.login_backend.dto.LoginRequest;
import com.login.login_backend.dto.LoginResponse;
import com.login.login_backend.model.User;
import com.login.login_backend.repository.UserRepository;
import com.login.login_backend.security.JwtUtil;
import com.login.login_backend.service.AuditService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          AuditService auditService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
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
}
