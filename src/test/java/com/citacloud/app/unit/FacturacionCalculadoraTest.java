package com.citacloud.app.unit;

import com.citacloud.app.exceptions.BusinessRuleException;
import com.citacloud.app.services.FacturaCalculadora;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class FacturacionCalculadoraTest {
    private final FacturaCalculadora calculadora = new FacturaCalculadora();

    @Test void calculaCantidadDescuentoEImpuestoSinUsarDouble() {
        FacturaCalculadora.Linea linea = calculadora.calcularLinea(new BigDecimal("2"), new BigDecimal("1500"),
                "PORCENTAJE", new BigDecimal("10"), new BigDecimal("18"));
        assertAll(
                () -> assertEquals(new BigDecimal("3000.00"), linea.subtotal()),
                () -> assertEquals(new BigDecimal("300.00"), linea.descuento()),
                () -> assertEquals(new BigDecimal("2700.00"), linea.baseImponible()),
                () -> assertEquals(new BigDecimal("486.00"), linea.impuesto()),
                () -> assertEquals(new BigDecimal("3186.00"), linea.total()));
    }

    @Test void noAsumeImpuestoCuandoLaTasaConfiguradaEsCero() {
        FacturaCalculadora.Linea linea = calculadora.calcularLinea(BigDecimal.ONE, new BigDecimal("2500"),
                "NINGUNO", BigDecimal.ZERO, BigDecimal.ZERO);
        assertEquals(new BigDecimal("0.00"), linea.impuesto());
        assertEquals(new BigDecimal("2500.00"), linea.total());
    }

    @Test void redondeaCadaLineaConDosDecimales() {
        FacturaCalculadora.Linea linea = calculadora.calcularLinea(new BigDecimal("3"), new BigDecimal("0.335"),
                "NINGUNO", BigDecimal.ZERO, new BigDecimal("18"));
        assertEquals(new BigDecimal("1.01"), linea.subtotal());
        assertEquals(new BigDecimal("0.18"), linea.impuesto());
        assertEquals(new BigDecimal("1.19"), linea.total());
    }

    @Test void totalizaLineasConLaMismaFormulaDelDocumento() {
        FacturaCalculadora.Linea una = calculadora.calcularLinea(BigDecimal.ONE, new BigDecimal("1000"), "MONTO", new BigDecimal("100"), new BigDecimal("18"));
        FacturaCalculadora.Linea dos = calculadora.calcularLinea(new BigDecimal("2"), new BigDecimal("150"), "NINGUNO", BigDecimal.ZERO, BigDecimal.ZERO);
        FacturaCalculadora.Totales total = calculadora.totalizar(List.of(una, dos));
        assertEquals(new BigDecimal("1300.00"), total.subtotal());
        assertEquals(new BigDecimal("100.00"), total.descuento());
        assertEquals(new BigDecimal("162.00"), total.impuestos());
        assertEquals(new BigDecimal("1362.00"), total.total());
    }

    @Test void rechazaCantidadPrecioYDescuentoInvalidos() {
        assertThrows(BusinessRuleException.class, () -> calculadora.calcularLinea(BigDecimal.ZERO, BigDecimal.TEN, "NINGUNO", BigDecimal.ZERO, BigDecimal.ZERO));
        assertThrows(BusinessRuleException.class, () -> calculadora.calcularLinea(BigDecimal.ONE, BigDecimal.ONE.negate(), "NINGUNO", BigDecimal.ZERO, BigDecimal.ZERO));
        assertThrows(BusinessRuleException.class, () -> calculadora.calcularLinea(BigDecimal.ONE, BigDecimal.TEN, "MONTO", new BigDecimal("11"), BigDecimal.ZERO));
        assertThrows(BusinessRuleException.class, () -> calculadora.calcularLinea(BigDecimal.ONE, BigDecimal.TEN, "PORCENTAJE", new BigDecimal("101"), BigDecimal.ZERO));
    }
}
