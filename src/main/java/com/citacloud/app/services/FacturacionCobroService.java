package com.citacloud.app.services;

import com.citacloud.app.models.CargoFinanciero;
import com.citacloud.app.models.Factura;
import com.citacloud.app.models.Pago;
import com.citacloud.app.repositories.CargoFinancieroRepository;
import com.citacloud.app.exceptions.ApplicationException;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class FacturacionCobroService {
    public record Resultado(Factura factura, Pago pago) { }

    private final FacturacionService facturacion;
    private final PagoService pagos;
    private final CargoFinancieroRepository cargos;
    private final MetricasPagos metricas;

    public FacturacionCobroService(FacturacionService facturacion, PagoService pagos,
                                   CargoFinancieroRepository cargos, MetricasPagos metricas) {
        this.facturacion = facturacion;
        this.pagos = pagos;
        this.cargos = cargos;
        this.metricas = metricas;
    }

    @Transactional
    public Resultado emitirYCobrar(UUID empresaId, UUID facturaId, String metodo,
                                   BigDecimal efectivoRecibido, String referencia) {
        return emitirYCobrar(empresaId, facturaId, null, metodo, efectivoRecibido, referencia);
    }

    @Transactional
    public Resultado emitirYCobrar(UUID empresaId, UUID facturaId, UUID cajaId, String metodo,
                                   BigDecimal efectivoRecibido, String referencia) {
        Timer.Sample muestra = metricas.iniciar();
        try {
            Factura factura = facturacion.emitir(empresaId, facturaId, cajaId);
            CargoFinanciero cargo = cargos.findByEmpresaIdAndFacturaId(empresaId, facturaId)
                    .orElseThrow(() -> new IllegalArgumentException("No se generó la cuenta por cobrar de la factura."));
            Pago pago = pagos.registrar(empresaId, new PagoService.Solicitud(
                    factura.getPaciente().getId(), factura.getSucursal().getId(), metodo, factura.getTotal(),
                    efectivoRecibido, referencia, null, "Cobro al emitir la factura " + factura.getNumero(),
                    "FACTURA-EMISION-" + facturaId, List.of(new PagoService.Aplicacion(cargo.getId(), factura.getTotal()))),
                    factura.getSesionCaja() == null ? null : factura.getSesionCaja().getId());
            metricas.aprobado(muestra);
            return new Resultado(factura, pago);
        } catch (ApplicationException | IllegalArgumentException exception) {
            metricas.rechazado(muestra);
            throw exception;
        } catch (RuntimeException exception) {
            metricas.error(muestra);
            throw exception;
        }
    }
}
