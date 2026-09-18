# Integración de WhatsApp

**Estado:** IMPLEMENTADA PARCIALMENTE

`WhatsappProvider` desacopla Cita Cloud del proveedor HTTP. La configuración incluye URL, token y referencia de plantilla. La referencia identifica la plantilla aprobada en WhatsApp Business y debe corresponder con idioma y variables enviadas.

Flujo: outbox -> worker -> plantilla -> API del proveedor -> entrega. Se registran estado, intento y error sanitizado. No se almacenan tokens en Git ni se incluyen datos clínicos innecesarios. Webhooks de confirmación completa no están documentados como implementados actualmente.
