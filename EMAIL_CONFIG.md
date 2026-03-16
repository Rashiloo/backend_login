# 📧 Configuración del Servicio de Email

## 🎯 Configuración para Gmail (Recomendado)

### 1. Habilitar 2FA en tu cuenta Gmail
- Ve a [myaccount.google.com/security](https://myaccount.google.com/security)
- Activa la "Verificación en dos pasos"

### 2. Generar Contraseña de Aplicación
- En la misma página de seguridad, busca "Contraseñas de aplicaciones"
- Crea una nueva contraseña con nombre: "Login Backend"
- Copia la contraseña generada (ej: `abcd efgh ijkl mnop`)

### 3. Configurar Variables de Entorno

#### Opción A: Variables de Entorno (Recomendado)
```bash
# En Windows (PowerShell)
$env:EMAIL_USERNAME="tu-email@gmail.com"
$env:EMAIL_PASSWORD="abcd-efgh-ijkl-mnop"
$env:FRONTEND_URL="https://frontend-login-m0xf.onrender.com"

# En Windows (CMD)
set EMAIL_USERNAME=tu-email@gmail.com
set EMAIL_PASSWORD=abcd-efgh-ijkl-mnop
set FRONTEND_URL=https://frontend-login-m0xf.onrender.com

# En Linux/Mac
export EMAIL_USERNAME="tu-email@gmail.com"
export EMAIL_PASSWORD="abcd-efgh-ijkl-mnop"
export FRONTEND_URL="https://frontend-login-m0xf.onrender.com"
```

#### Opción B: Modificar application.properties directamente
```properties
# Reemplaza las líneas existentes
spring.mail.username=tu-email@gmail.com
spring.mail.password=abcd-efgh-ijkl-mnop
app.frontend.url=https://frontend-login-m0xf.onrender.com
```

## 🔄 Flujo Completo de Recuperación

### 1. Usuario solicita recuperación
```
POST /api/auth/forgot-password
{
  "email": "usuario@gmail.com"
}
```

### 2. Backend genera y envía email
- ✅ Genera token UUID único
- ✅ Guarda token en BD con expiración (1 hora)
- ✅ Envía email con enlace de recuperación
- ✅ Audita el evento

### 3. Usuario recibe email
El email contiene:
- ✅ Botón de "Restablecer Contraseña"
- ✅ Enlace directo: `https://frontend-login-m0xf.onrender.com/reset-password?token=UUID`
- ✅ Advertencias de seguridad
- ✅ Expiración de 1 hora

### 4. Usuario restablece contraseña
```
POST /api/auth/reset-password
{
  "token": "UUID-del-email",
  "newPassword": "nuevaClave123"
}
```

### 5. Backend procesa y actualiza
- ✅ Valida token y expiración
- ✅ Actualiza contraseña encriptada
- ✅ Limpia token de recuperación
- ✅ Audita el evento exitoso

## 🧪 Probar el Sistema

### 1. Iniciar el backend
```bash
mvn spring-boot:run
```

### 2. Probar endpoint de recuperación
```bash
curl -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email": "tu-email@gmail.com"}'
```

### 3. Revisar email
- Deberías recibir un email profesional con HTML
- El enlace contendrá el token generado
- El enlace expira en 1 hora

### 4. Probar reset de contraseña
```bash
curl -X POST http://localhost:8080/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{"token": "TOKEN_DEL_EMAIL", "newPassword": "nuevaClave123"}'
```

## 🛠️ Configuración para Producción

### Render.com
```bash
# En Render Dashboard
Environment Variables:
EMAIL_USERNAME=tu-email@gmail.com
EMAIL_PASSWORD=abcd-efgh-ijkl-mnop
FRONTEND_URL=https://tu-frontend.com
```

### Railway.app
```bash
# En Railway Variables
EMAIL_USERNAME=tu-email@gmail.com
EMAIL_PASSWORD=abcd-efgh-ijkl-mnop
FRONTEND_URL=https://tu-frontend.com
```

## 🔧 Solución de Problemas

### Error: "535-5.7.8 Username and Password not accepted"
- ✅ Verifica que usaste "Contraseña de aplicación", no tu contraseña normal
- ✅ Activa "Permitir apps menos seguras" temporalmente si es necesario

### Error: "Connection refused"
- ✅ Verifica que el puerto 587 esté abierto
- ✅ Revisa tu configuración de firewall

### Email no llega
- ✅ Revisa carpeta de spam
- ✅ Verifica dirección de email
- ✅ Revisa logs del backend

## 📋 Características Implementadas

- ✅ **Email HTML profesional** con diseño responsive
- ✅ **Token UUID seguro** con expiración
- ✅ **Manejo de errores** con limpieza de tokens
- ✅ **Auditoría completa** de eventos
- ✅ **Configuración dinámica** para diferentes entornos
- ✅ **Seguridad** con encriptación y validación
- ✅ **UX optimizada** con mensajes claros

## 🎉 ¡Listo para usar!

Una vez configurado, el sistema de recuperación de contraseña estará completamente funcional y listo para producción.
