package com.citacloud.app.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record FacturaLineaSolicitud(
        String tipoItem,
        UUID itemId,
        BigDecimal cantidad,
        BigDecimal precioUnitario,
        String tipoDescuento,
        BigDecimal descuento
) { }
