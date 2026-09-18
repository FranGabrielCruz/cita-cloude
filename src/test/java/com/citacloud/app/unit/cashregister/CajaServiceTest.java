package com.citacloud.app.unit.cashregister;

import com.citacloud.app.models.MovimientoCaja;
import com.citacloud.app.models.SesionCaja;
import com.citacloud.app.services.CajaService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CajaServiceTest {
    @Test
    void translatesMovementTypesForTheReport() {
        assertThat(CajaService.tipoMovimientoEspanol("MANUAL_INCOME")).isEqualTo("Ingreso manual");
        assertThat(CajaService.tipoMovimientoEspanol("MANUAL_EXPENSE")).isEqualTo("Egreso manual");
        assertThat(CajaService.tipoMovimientoEspanol("PAYMENT")).isEqualTo("Cobro");
        assertThat(CajaService.tipoMovimientoEspanol("REFUND")).isEqualTo("Devolución");
        assertThat(CajaService.tipoMovimientoEspanol("OTHER_TYPE")).doesNotContain("_");
    }

    @Test
    void calculatesExpectedCashFromOpeningIncomeAndRefund() throws Exception {
        SesionCaja sesion = new SesionCaja();
        sesion.setFondoInicial(new BigDecimal("10000"));
        CajaService.Resumen resumen = resumen(sesion, movimiento("IN", "EFECTIVO", "5000"), movimiento("OUT", "EFECTIVO", "2000"));

        assertThat(resumen.ingresos()).isEqualByComparingTo("5000.00");
        assertThat(resumen.egresos()).isEqualByComparingTo("2000.00");
        assertThat(resumen.efectivoEsperado()).isEqualByComparingTo("13000.00");
    }

    private CajaService.Resumen resumen(SesionCaja sesion, MovimientoCaja... movimientos) throws Exception {
        CajaService service = new CajaService(null, null, null, null, null, null, null, null, null, null);
        Method method = CajaService.class.getDeclaredMethod("resumen", SesionCaja.class, List.class);
        method.setAccessible(true);
        return (CajaService.Resumen) method.invoke(service, sesion, List.of(movimientos));
    }

    private MovimientoCaja movimiento(String direccion, String metodo, String monto) {
        MovimientoCaja movimiento = new MovimientoCaja();
        movimiento.setEstado("ACTIVE");
        movimiento.setDireccion(direccion);
        movimiento.setMetodoPago(metodo);
        movimiento.setMonto(new BigDecimal(monto));
        return movimiento;
    }
}
