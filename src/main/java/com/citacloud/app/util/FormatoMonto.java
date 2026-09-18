package com.citacloud.app.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Conversión única de importes escritos por el usuario y su presentación monetaria. */
public final class FormatoMonto {
    private FormatoMonto() {
    }

    public static BigDecimal parse(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new NumberFormatException("El monto es obligatorio.");
        }
        return new BigDecimal(valor.replace(",", "").trim());
    }

    public static String format(BigDecimal valor) {
        return new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US))
                .format(valor == null ? BigDecimal.ZERO : valor);
    }
}
