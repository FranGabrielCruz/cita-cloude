package com.citacloud.app.dto;

import java.time.LocalDate;
import java.util.UUID;

public record FacturaFiltro(
        String termino,
        LocalDate desde,
        LocalDate hasta,
        String estado,
        UUID sucursalId,
        UUID pacienteId,
        UUID medicoId
) { }
