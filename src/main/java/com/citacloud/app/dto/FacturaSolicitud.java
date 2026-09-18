package com.citacloud.app.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record FacturaSolicitud(
        UUID sucursalId,
        UUID pacienteId,
        UUID medicoId,
        LocalDate fecha,
        String tipoComprobante,
        String origenTipo,
        UUID origenId,
        String observacion,
        String claveIdempotencia,
        List<FacturaLineaSolicitud> lineas
) { }
