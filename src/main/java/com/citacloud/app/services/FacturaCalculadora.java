package com.citacloud.app.services;

import com.citacloud.app.exceptions.BusinessRuleException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

/** Fuente unica de redondeo y calculos para UI, persistencia, PDF y e-CF. */
@Component
public class FacturaCalculadora {
    public record Linea(BigDecimal subtotal, BigDecimal descuento, BigDecimal baseImponible,
                        BigDecimal impuesto, BigDecimal total) { }
    public record Totales(BigDecimal subtotal, BigDecimal descuento, BigDecimal impuestos, BigDecimal total) { }

    public Linea calcularLinea(BigDecimal cantidad, BigDecimal precioUnitario, String tipoDescuento,
                               BigDecimal valorDescuento, BigDecimal tasaImpuesto) {
        if (cantidad == null || cantidad.signum() <= 0) {
            throw new BusinessRuleException("INVALID_INVOICE_AMOUNT", "La cantidad debe ser mayor que cero.");
        }
        if (precioUnitario == null || precioUnitario.signum() < 0) {
            throw new BusinessRuleException("INVALID_INVOICE_AMOUNT", "El precio unitario no puede ser negativo.");
        }
        BigDecimal subtotal = dinero(cantidad.multiply(precioUnitario));
        BigDecimal valor = dinero(valorDescuento);
        BigDecimal descuento = switch (normalizarTipo(tipoDescuento)) {
            case "PORCENTAJE" -> {
                if (valor.compareTo(BigDecimal.valueOf(100)) > 0) {
                    throw new BusinessRuleException("INVALID_INVOICE_AMOUNT", "El descuento porcentual no puede superar 100%.");
                }
                yield dinero(subtotal.multiply(valor).divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP));
            }
            case "MONTO" -> valor;
            default -> BigDecimal.ZERO.setScale(2);
        };
        if (descuento.signum() < 0 || descuento.compareTo(subtotal) > 0) {
            throw new BusinessRuleException("INVALID_INVOICE_AMOUNT", "El descuento no puede superar el importe de la línea.");
        }
        BigDecimal tasa = tasaImpuesto == null ? BigDecimal.ZERO : tasaImpuesto;
        if (tasa.signum() < 0 || tasa.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BusinessRuleException("INVALID_INVOICE_AMOUNT", "La tasa de impuesto configurada no es válida.");
        }
        BigDecimal base = dinero(subtotal.subtract(descuento));
        BigDecimal impuesto = dinero(base.multiply(tasa).divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP));
        return new Linea(subtotal, descuento, base, impuesto, dinero(base.add(impuesto)));
    }

    public Totales totalizar(Collection<Linea> lineas) {
        if (lineas == null || lineas.isEmpty()) {
            return new Totales(cero(), cero(), cero(), cero());
        }
        BigDecimal subtotal = lineas.stream().map(Linea::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal descuento = lineas.stream().map(Linea::descuento).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal impuestos = lineas.stream().map(Linea::impuesto).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal total = lineas.stream().map(Linea::total).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Totales(dinero(subtotal), dinero(descuento), dinero(impuestos), dinero(total));
    }

    public BigDecimal dinero(BigDecimal valor) {
        return (valor == null ? BigDecimal.ZERO : valor).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal cero() { return BigDecimal.ZERO.setScale(2); }
    private String normalizarTipo(String tipo) {
        if (tipo == null || tipo.isBlank() || "NINGUNO".equalsIgnoreCase(tipo)) return "NINGUNO";
        String normalizado = tipo.trim().toUpperCase();
        if (!"PORCENTAJE".equals(normalizado) && !"MONTO".equals(normalizado)) {
            throw new BusinessRuleException("INVALID_INVOICE_AMOUNT", "El tipo de descuento no es válido.");
        }
        return normalizado;
    }
}
