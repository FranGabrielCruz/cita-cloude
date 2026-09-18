# ADR-002 — Outbox para notificaciones de citas

**Estado:** Aceptado  
**Fecha:** 08/09/2026

## Contexto

SMTP y WhatsApp pueden fallar o responder lentamente; una cita no debe perderse ni depender de ellos.

## Decisión

Guardar una orden de entrega en `outbox_notificaciones` dentro del flujo de negocio y procesarla mediante un worker con estados, intentos y entrega registrada.

## Alternativas consideradas

- Envío sincrónico: simple, pero acopla la cita al proveedor.
- Broker externo: robusto, pero innecesario para el volumen y madurez actuales.

## Consecuencias

Se obtiene retry y diagnóstico; existe consistencia eventual y se debe monitorear cola, worker y duplicados.
