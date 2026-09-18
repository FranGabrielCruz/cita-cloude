package com.citacloud.app.services;

import com.citacloud.app.exceptions.ForbiddenException;
import com.citacloud.app.models.*;
import com.citacloud.app.repositories.EmpresaRepository;
import com.citacloud.app.security.AuthService;
import com.citacloud.app.security.TenantUserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class FacturaPdfService {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a", Locale.US);
    private final FacturacionService facturacion;
    private final EmpresaRepository empresas;
    private final JasperPdfService jasper;
    private final AuditoriaService auditoria;

    public FacturaPdfService(FacturacionService facturacion, EmpresaRepository empresas,
                             JasperPdfService jasper, AuditoriaService auditoria) {
        this.facturacion = facturacion; this.empresas = empresas; this.jasper = jasper; this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public byte[] generar(UUID empresaId, UUID facturaId) {
        validarPermiso(empresaId);
        Factura factura = facturacion.obtener(empresaId, facturaId);
        if (FacturacionService.BORRADOR.equals(factura.getEstado())) {
            throw new IllegalArgumentException("Emite la factura antes de generar el documento formal.");
        }
        Empresa empresa = empresas.findById(empresaId).orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada."));
        List<DetalleFactura> detalles = facturacion.detalles(empresaId, facturaId);
        List<Pago> pagos = facturacion.pagos(empresaId, facturaId).stream().filter(p -> !"VOIDED".equals(p.getEstado())).toList();
        List<Map<String, Object>> filas = detalles.stream().map(d -> {
            Map<String, Object> fila = new HashMap<>();
            fila.put("descripcion", d.getDescripcion()); fila.put("tipo", d.getTipoItem());
            fila.put("cantidad", numero(d.getCantidad())); fila.put("precio", numero(d.getPrecio()));
            fila.put("descuento", numero(d.getDescuento())); fila.put("impuesto", numero(d.getImpuesto()));
            fila.put("total", numero(d.getImporte())); return fila;
        }).toList();
        Sucursal sucursal = factura.getSucursal(); Paciente paciente = factura.getPaciente(); Medico medico = factura.getMedico();
        Caja caja = factura.getCaja(); SesionCaja turno = factura.getSesionCaja();
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("CLINICA", texto(empresa.getNombre())); parametros.put("RNC", texto(empresa.getRncIdentificacion()));
        parametros.put("DIRECCION", texto(sucursal == null ? empresa.getDireccion() : sucursal.getDireccion()));
        parametros.put("TELEFONO", texto(sucursal == null ? empresa.getTelefono() : sucursal.getTelefono()));
        parametros.put("SUCURSAL", sucursal == null ? "-" : texto(sucursal.getNombre()));
        parametros.put("CAJA", caja == null ? "-" : texto(caja.getNombre()));
        parametros.put("TURNO", turno == null ? "-" : texto(turno.getNumero()));
        parametros.put("NUMERO", texto(factura.getNumero())); parametros.put("FECHA", factura.getFecha() == null ? "-" : factura.getFecha().format(FECHA));
        parametros.put("ESTADO", texto(factura.getEstado()));
        parametros.put("PACIENTE", paciente == null ? "-" : texto(paciente.getNombreCompleto()));
        parametros.put("EXPEDIENTE", paciente == null ? "" : texto(paciente.getNumeroExpediente()));
        parametros.put("DOCUMENTO", paciente == null ? "" : texto(paciente.getDocumento()));
        parametros.put("MEDICO", medico == null ? "No aplica" : texto(medico.getNombreCompleto()));
        String comprobante = SecuenciaComprobanteFiscalService.etiqueta(factura.getTipoComprobante());
        if (factura.getTipoComprobante() == null || factura.getTipoComprobante().isBlank()) comprobante = "";
        else if (factura.getNumeroComprobanteFiscal() != null) comprobante += " · " + factura.getNumeroComprobanteFiscal();
        parametros.put("COMPROBANTE", comprobante);
        parametros.put("SUBTOTAL", numero(factura.getSubtotal())); parametros.put("DESCUENTO", numero(factura.getDescuento()));
        parametros.put("IMPUESTOS", numero(factura.getImpuestos())); parametros.put("TOTAL", numero(factura.getTotal()));
        parametros.put("PAGADO", numero(factura.getMontoPagado())); parametros.put("PENDIENTE", numero(factura.getSaldo()));
        parametros.put("PAGOS", pagos.stream().map(p -> texto(p.getNumero()) + " · " + (p.getCreadoEn() == null ? p.getFecha().format(FECHA) : p.getCreadoEn().format(FECHA_HORA)) + " · " + texto(p.getMetodoPago()) + " · RD$ " + numero(p.getMonto())).reduce((a,b) -> a + "\n" + b).orElse("Sin pagos registrados"));
        parametros.put("OBSERVACION", texto(factura.getObservacion()));
        byte[] pdf = jasper.generar("factura", parametros, filas);
        auditoria.registrar(empresaId, null, "FACTURACION", "INVOICE_PRINTED", "FACTURA", facturaId,
                factura.getNumero(), paciente == null ? null : paciente.getId(), List.of(), "SUCCESS", null, true);
        return pdf;
    }

    private void validarPermiso(UUID empresaId) {
        TenantUserDetails usuario = AuthService.getAuthenticatedUser();
        if (usuario == null || !Objects.equals(empresaId, usuario.getEmpresaId())) throw new ForbiddenException();
        boolean permitido = usuario.getAuthorities().stream().anyMatch(a -> Set.of("ROLE_ADMINISTRADOR", "ROLE_SUPERADMIN", "BILLING_PRINT").contains(a.getAuthority()));
        if (!permitido) throw new ForbiddenException();
    }
    private String numero(BigDecimal valor) { NumberFormat formato = NumberFormat.getNumberInstance(Locale.US); formato.setMinimumFractionDigits(2); formato.setMaximumFractionDigits(2); return formato.format(valor == null ? BigDecimal.ZERO : valor); }
    private String texto(String valor) { return valor == null || valor.isBlank() ? "" : valor; }
}
