package com.citacloud.app.unit.payments;

import com.citacloud.app.models.Pago;
import com.citacloud.app.repositories.PagoRepository;
import com.citacloud.app.repositories.ReembolsoPagoRepository;
import com.citacloud.app.services.PagoService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PagoServiceTest {
    @Test
    void calculatesAvailableRefundWithoutExceedingOriginalPayment() {
        UUID empresa = UUID.randomUUID();
        UUID pagoId = UUID.randomUUID();
        Pago pago = new Pago();
        pago.setMonto(new BigDecimal("5000"));

        PagoService service = new PagoService(pagos(pago), null, null, reembolsos(new BigDecimal("2000")), null,
                null, null, null, null, null, null, null, null);

        assertThat(service.totalReembolsado(empresa, pagoId)).isEqualByComparingTo("2000.00");
        assertThat(service.disponibleReembolso(empresa, pagoId)).isEqualByComparingTo("3000.00");
    }

    @Test
    void neverReturnsNegativeAvailableRefund() {
        UUID empresa = UUID.randomUUID();
        UUID pagoId = UUID.randomUUID();
        Pago pago = new Pago();
        pago.setMonto(new BigDecimal("5000"));
        PagoService service = new PagoService(pagos(pago), null, null, reembolsos(new BigDecimal("7000")), null,
                null, null, null, null, null, null, null, null);

        assertThat(service.disponibleReembolso(empresa, pagoId)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    private PagoRepository pagos(Pago pago) {
        return (PagoRepository) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{PagoRepository.class},
                (proxy, method, args) -> "findByIdAndEmpresaId".equals(method.getName()) ? Optional.of(pago) : Optional.empty());
    }

    private ReembolsoPagoRepository reembolsos(BigDecimal total) {
        return (ReembolsoPagoRepository) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{ReembolsoPagoRepository.class},
                (proxy, method, args) -> "totalPorPago".equals(method.getName()) ? total : BigDecimal.ZERO);
    }
}
