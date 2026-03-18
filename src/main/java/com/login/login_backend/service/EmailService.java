package com.login.login_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    /**
     * Envía email de recuperación de contraseña
     */
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        try {
            String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Recuperación de Contraseña - Login System");
            
            // HTML content para el email
            String htmlContent = buildPasswordResetEmailHtml(resetLink, toEmail);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            
        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar email de recuperación: " + e.getMessage());
        }
    }

    /**
     * Construye el contenido HTML del email de recuperación
     */
    private String buildPasswordResetEmailHtml(String resetLink, String userEmail) {
        String htmlTemplate = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Recuperación de Contraseña</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                        background-color: #f4f4f4;
                    }
                    .container {
                        background-color: white;
                        padding: 30px;
                        border-radius: 10px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                    }
                    .header {
                        text-align: center;
                        padding-bottom: 20px;
                        border-bottom: 2px solid #667eea;
                    }
                    .header h1 {
                        color: #667eea;
                        margin: 0;
                        font-size: 28px;
                    }
                    .content {
                        padding: 20px 0;
                    }
                    .button {
                        display: inline-block;
                        padding: 12px 30px;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        text-decoration: none;
                        border-radius: 5px;
                        font-weight: bold;
                        margin: 20px 0;
                    }
                    .button:hover {
                        opacity: 0.9;
                    }
                    .footer {
                        text-align: center;
                        padding-top: 20px;
                        border-top: 1px solid #eee;
                        font-size: 12px;
                        color: #666;
                    }
                    .warning {
                        background-color: #fff3cd;
                        border: 1px solid #ffeaa7;
                        padding: 15px;
                        border-radius: 5px;
                        margin: 20px 0;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔐 Recuperación de Contraseña</h1>
                    </div>
                    
                    <div class="content">
                        <p>Hola,</p>
                        <p>Recibimos una solicitud para restablecer la contraseña de tu cuenta asociada a este email: <strong>%s</strong></p>
                        
                        <p>Para continuar con el proceso de recuperación, haz clic en el siguiente botón:</p>
                        
                        <div style="text-align: center;">
                            <a href="%s" class="button">Restablecer Contraseña</a>
                        </div>
                        
                        <div class="warning">
                            <strong>⚠️ Importante:</strong><br>
                            • Este enlace expirará en 1 hora por seguridad.<br>
                            • Si no solicitaste este cambio, ignora este email.<br>
                            • Nunca compartas este enlace con nadie.
                        </div>
                        
                        <p>Si el botón no funciona, copia y pega este enlace en tu navegador:</p>
                        <p style="word-break: break-all; background-color: #f8f9fa; padding: 10px; border-radius: 5px;">
                            %s
                        </p>
                    </div>
                    
                    <div class="footer">
                        <p>Este es un mensaje automático de Login System. Por favor, no respondas a este email.</p>
                        <p>Si necesitas ayuda, contacta a nuestro equipo de soporte.</p>
                    </div>
                </div>
            </body>
            </html>
            """;
        
        return String.format(htmlTemplate, userEmail, resetLink, resetLink);
    }

    /**
     * Envía email de bienvenida (opcional, para futuras mejoras)
     */
    public void sendWelcomeEmail(String toEmail, String firstName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("¡Bienvenido a Login System!");
            message.setText(String.format(
                "Hola %s,\n\n" +
                "¡Bienvenido a Login System! Tu cuenta ha sido creada exitosamente.\n\n" +
                "Ahora puedes iniciar sesión con tus credenciales.\n\n" +
                "Saludos,\n" +
                "El equipo de Login System",
                firstName
            ));
            
            mailSender.send(message);
            
        } catch (Exception e) {
            throw new RuntimeException("Error al enviar email de bienvenida: " + e.getMessage());
        }
    }

    /**
     * Envía email con código de verificación (RF-15)
     */
    public void sendVerificationCode(String toEmail, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Tu código de verificación - Login System");
            
            String html = "<h3>Tu código de seguridad es: <b>" + code + "</b></h3>" +
                         "<p>Este código expira en 15 minutos.</p>" +
                         "<p>Si no solicitaste este código, ignora este mensaje.</p>";
            
            helper.setText(html, true);
            mailSender.send(message);
            
        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar el correo: " + e.getMessage());
        }
    }

    /**
     * Envía email de recuperación con código de verificación (RF-15)
     */
    public void sendPasswordResetEmailWithVerification(String toEmail, String resetToken, String verificationCode) {
        try {
            String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Recuperación de Contraseña - Verificación de Identidad");
            
            String htmlContent = buildPasswordResetWithVerificationHtml(resetLink, toEmail, verificationCode);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            
        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar email de recuperación con verificación: " + e.getMessage());
        }
    }

    private String buildPasswordResetWithVerificationHtml(String resetLink, String userEmail, String verificationCode) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Recuperación de Contraseña - Verificación</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                        background-color: #f4f4f4;
                    }
                    .container {
                        background-color: white;
                        padding: 30px;
                        border-radius: 10px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                    }
                    .header {
                        text-align: center;
                        padding-bottom: 20px;
                        border-bottom: 2px solid #667eea;
                    }
                    .header h1 {
                        color: #667eea;
                        margin: 0;
                        font-size: 24px;
                    }
                    .verification-box {
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        font-size: 36px;
                        font-weight: bold;
                        text-align: center;
                        padding: 25px;
                        border-radius: 10px;
                        letter-spacing: 10px;
                        margin: 25px 0;
                        text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
                    }
                    .warning {
                        background-color: #fff3cd;
                        border: 1px solid #ffeaa7;
                        padding: 15px;
                        border-radius: 5px;
                        margin: 20px 0;
                    }
                    .button {
                        display: inline-block;
                        padding: 12px 30px;
                        background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
                        color: white;
                        text-decoration: none;
                        border-radius: 5px;
                        font-weight: bold;
                        margin: 10px 0;
                    }
                    .footer {
                        text-align: center;
                        padding-top: 20px;
                        border-top: 1px solid #eee;
                        font-size: 12px;
                        color: #666;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔐 Verificación de Identidad</h1>
                    </div>
                    
                    <div class="content">
                        <p>Hola,</p>
                        <p>Recibimos una solicitud para restablecer la contraseña de: <strong>%s</strong></p>
                        
                        <p><strong>Paso 1:</strong> Usa este código de verificación de 6 dígitos:</p>
                        <div class="verification-box">%s</div>
                        
                        <p><strong>Paso 2:</strong> Una vez verificado, usa este enlace para cambiar tu contraseña:</p>
                        <div style="text-align: center;">
                            <a href="%s" class="button">Restablecer Contraseña</a>
                        </div>
                        
                        <div class="warning">
                            <strong>⚠️ Importante:</strong><br>
                            • Este código expira en <strong>15 minutos</strong>.<br>
                            • El enlace expira en <strong>1 hora</strong>.<br>
                            • Si no solicitaste este cambio, ignora este email.<br>
                            • Nunca compartas este código con nadie.
                        </div>
                        
                        <p>Si el botón no funciona, copia y pega este enlace:</p>
                        <p style="word-break: break-all; background-color: #f8f9fa; padding: 10px; border-radius: 5px;">
                            %s
                        </p>
                    </div>
                    
                    <div class="footer">
                        <p>Este es un mensaje automático de Login System.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(userEmail, verificationCode, resetLink, resetLink);
    }
}

