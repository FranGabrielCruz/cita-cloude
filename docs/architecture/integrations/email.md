# Integración de correo

**Estado:** IMPLEMENTADA

El canal usa `EmailProvider` con implementación SMTP. Host, puerto, autenticación, TLS, usuario y dirección remitente provienen de configuración técnica. El destinatario ve `Nombre de la clínica <no-reply@dominio>`, donde el nombre se obtiene del tenant y la dirección de `NOTIFICATIONS_EMAIL_FROM`. Las credenciales nunca se persisten en la configuración funcional.

Flujo: outbox -> worker -> plantilla -> SMTP -> entrega. El worker registra éxito/error sanitizado, duración e intentos. Los timeouts y retry deben permanecer acotados. En desarrollo puede usarse un buzón de pruebas; producción exige TLS y credenciales rotables.
