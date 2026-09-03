package com.citacloud.app.services;

import com.citacloud.app.dto.*;
import com.citacloud.app.exceptions.*;
import com.citacloud.app.models.*;
import com.citacloud.app.repositories.*;
import com.citacloud.app.security.AuthService;
import com.citacloud.app.security.TenantUserDetails;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Service
public class FacturacionService {
    public static final String BORRADOR = "BORRADOR";
    public static final String PENDIENTE = "PENDIENTE";
    public static final String PARCIAL = "PARCIAL";
    public static final String PAGADA = "PAGADA";
    public static final String ANULADA = "ANULADA";

    public record Resumen(BigDecimal facturado, long facturas, BigDecimal pendiente, long pagadas) { }

    private final FacturaRepository facturas;
    private final DetalleFacturaRepository detalles;
    private final HistorialFacturaRepository historial;
    private final PagoRepository pagos;
    private final PacienteRepository pacientes;
    private final SucursalRepository sucursales;
    private final MedicoRepository medicos;
    private final ServicioRepository servicios;
    private final ProductoInventarioRepository productos;
    private final CargoFinancieroRepository cargos;
    private final AuditoriaService auditoria;
    private final FacturaCalculadora calculadora;
    private final CajaService cajaService;
    private final SecuenciaComprobanteFiscalService secuenciasFiscales;
    private final EntityManager entityManager;

    public FacturacionService(FacturaRepository facturas, DetalleFacturaRepository detalles,
            HistorialFacturaRepository historial, PagoRepository pagos, PacienteRepository pacientes,
            SucursalRepository sucursales, MedicoRepository medicos, ServicioRepository servicios,
            ProductoInventarioRepository productos, CargoFinancieroRepository cargos,
            AuditoriaService auditoria, FacturaCalculadora calculadora, CajaService cajaService,
            SecuenciaComprobanteFiscalService secuenciasFiscales, EntityManager entityManager) {
        this.facturas = facturas;
        this.detalles = detalles;
        this.historial = historial;
        this.pagos = pagos;
        this.pacientes = pacientes;
        this.sucursales = sucursales;
        this.medicos = medicos;
        this.servicios = servicios;
        this.productos = productos;
        this.cargos = cargos;
        this.auditoria = auditoria;
        this.calculadora = calculadora;
        this.cajaService = cajaService;
        this.secuenciasFiscales = secuenciasFiscales;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public List<Factura> listar(UUID empresaId) {
        validarLectura(empresaId);
        return facturas.findByEmpresaIdOrderByFechaDesc(empresaId);
    }

    @Transactional(readOnly = true)
    public Page<Factura> buscarPagina(UUID empresaId, FacturaFiltro filtro, int pagina, int tamanio) {
        validarLectura(empresaId);
        FacturaFiltro valor = filtro == null ? new FacturaFiltro(null, null, null, null, null, null, null) : filtro;
        Pageable pageable = PageRequest.of(Math.max(0, pagina), normalizarTamanio(tamanio),
                Sort.by(Sort.Order.desc("fecha"), Sort.Order.desc("creadoEn")));
        return facturas.findAll(especificacion(empresaId, valor), pageable);
    }

    @Transactional(readOnly = true)
    public Resumen resumen(UUID empresaId, FacturaFiltro filtro) {
        validarLectura(empresaId);
        List<Factura> lista = facturas.findAll(especificacion(empresaId,
                filtro == null ? new FacturaFiltro(null, null, null, null, null, null, null) : filtro));
        List<Factura> emitidas = lista.stream().filter(f -> !BORRADOR.equals(f.getEstado()) && !ANULADA.equals(f.getEstado())).toList();
        return new Resumen(
                calculadora.dinero(emitidas.stream().map(Factura::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add)),
                emitidas.size(),
                calculadora.dinero(emitidas.stream().map(Factura::getSaldo).reduce(BigDecimal.ZERO, BigDecimal::add)),
                emitidas.stream().filter(f -> PAGADA.equals(f.getEstado())).count());
    }

    @Transactional(readOnly = true)
    public Factura obtener(UUID empresaId, UUID facturaId) {
        validarLectura(empresaId);
        return buscarFactura(empresaId, facturaId);
    }

    @Transactional(readOnly = true)
    public List<DetalleFactura> detalles(UUID empresaId, UUID facturaId) {
        buscarFactura(empresaId, facturaId);
        return detalles.findByEmpresaIdAndFacturaIdOrderByCreadoEnAsc(empresaId, facturaId);
    }

    @Transactional(readOnly = true)
    public List<HistorialFactura> historial(UUID empresaId, UUID facturaId) {
        buscarFactura(empresaId, facturaId);
        return historial.findByEmpresaIdAndFacturaIdOrderByCreadoEnDesc(empresaId, facturaId);
    }

    @Transactional(readOnly = true)
    public List<Pago> pagos(UUID empresaId, UUID facturaId) {
        buscarFactura(empresaId, facturaId);
        return pagos.findByEmpresaIdAndFactura(empresaId, facturaId);
    }

    @Transactional(readOnly = true)
    public List<Servicio> serviciosFacturables(UUID empresaId) {
        validarLectura(empresaId);
        return servicios.findByEmpresaIdOrderByNombre(empresaId).stream()
                .filter(s -> Boolean.TRUE.equals(s.getActivo()) && Boolean.TRUE.equals(s.getFacturable())).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoInventario> productosFacturables(UUID empresaId) {
        validarLectura(empresaId);
        return productos.findByEmpresaIdOrderByNombre(empresaId).stream()
                .filter(p -> p.isActivo() && p.isEsFacturable()).toList();
    }

    @Transactional
    public Factura crearBorrador(UUID empresaId, FacturaSolicitud solicitud) {
        requerir(empresaId, "BILLING_CREATE");
        validarSolicitud(solicitud);
        String clave = limitar(solicitud.claveIdempotencia(), 100);
        if (clave != null && !clave.isBlank()) {
            Optional<Factura> previa = facturas.findByEmpresaIdAndClaveIdempotencia(empresaId, clave);
            if (previa.isPresent()) return previa.get();
        }
        validarOrigenUnico(empresaId, solicitud.origenTipo(), solicitud.origenId(), null);
        Factura factura = new Factura();
        factura.setEmpresaId(empresaId);
        factura.setNumero(siguienteNumero(empresaId));
        factura.setCreadoPor(usuarioActualId());
        factura.setEstado(BORRADOR);
        factura.setClaveIdempotencia(clave);
        aplicarCabecera(empresaId, factura, solicitud);
        factura = facturas.save(factura);
        guardarLineas(empresaId, factura, solicitud.lineas());
        registrarHistorial(factura, null, BORRADOR, "CREACION", null);
        auditoria.registrar(empresaId, null, "FACTURACION", "INVOICE_DRAFT_CREATED", "FACTURA",
                factura.getId(), factura.getNumero(), factura.getPaciente().getId(), List.of(), "SUCCESS", null, true);
        return factura;
    }

    @Transactional
    public Factura guardarBorrador(UUID empresaId, UUID facturaId, FacturaSolicitud solicitud) {
        requerir(empresaId, "BILLING_EDIT_DRAFT");
        validarSolicitud(solicitud);
        Factura factura = bloquearFactura(empresaId, facturaId);
        if (!BORRADOR.equals(factura.getEstado())) {
            throw new ConflictException("INVOICE_ALREADY_ISSUED", "Una factura emitida no puede editarse como borrador.");
        }
        validarOrigenUnico(empresaId, solicitud.origenTipo(), solicitud.origenId(), facturaId);
        aplicarCabecera(empresaId, factura, solicitud);
        detalles.deleteByEmpresaIdAndFacturaId(empresaId, facturaId);
        entityManager.flush();
        guardarLineas(empresaId, factura, solicitud.lineas());
        registrarHistorial(factura, BORRADOR, BORRADOR, "EDICION", null);
        auditoria.registrar(empresaId, null, "FACTURACION", "INVOICE_DRAFT_UPDATED", "FACTURA",
                facturaId, factura.getNumero(), factura.getPaciente().getId(), List.of(), "SUCCESS", null, true);
        return factura;
    }

    @Transactional
    public Factura emitir(UUID empresaId, UUID facturaId) {
        return emitir(empresaId, facturaId, null);
    }

    @Transactional
    public Factura emitir(UUID empresaId, UUID facturaId, UUID cajaId) {
        requerir(empresaId, "BILLING_ISSUE");
        Factura factura = bloquearFactura(empresaId, facturaId);
        if (!BORRADOR.equals(factura.getEstado())) {
            if (Set.of(PENDIENTE, PARCIAL, PAGADA).contains(factura.getEstado())) return factura;
            throw new BusinessRuleException("INVALID_INVOICE_STATE", "La factura no se encuentra en un estado que permita emitirla.");
        }
        List<DetalleFactura> lineas = detalles.findByEmpresaIdAndFacturaIdOrderByCreadoEnAsc(empresaId, facturaId);
        if (lineas.isEmpty()) {
            throw new BusinessRuleException("INVALID_INVOICE_ITEM", "La factura debe contener al menos una línea.");
        }
        if (factura.getSucursal() == null || factura.getPaciente() == null) {
            throw new BusinessRuleException("VALIDATION_ERROR", "La sucursal y el paciente son obligatorios.");
        }
        SesionCaja sesionCaja = cajaId == null
                ? cajaService.sesionActivaRequerida(empresaId, factura.getSucursal().getId())
                : cajaService.sesionActivaRequerida(empresaId, factura.getSucursal().getId(), cajaId);
        factura.setCaja(sesionCaja.getCaja());
        factura.setSesionCaja(sesionCaja);
        recalcularDesdeSnapshot(factura, lineas);
        if (factura.getTotal().signum() <= 0) {
            throw new BusinessRuleException("INVALID_INVOICE_AMOUNT", "El total de la factura debe ser mayor que cero.");
        }
        String anterior = factura.getEstado();
        factura.setMontoPagado(BigDecimal.ZERO.setScale(2));
        factura.setSaldo(factura.getTotal());
        factura.setEstado(PENDIENTE);
        factura.setEmitidoPor(usuarioActualId());
        factura.setEmitidoEn(LocalDateTime.now());
        if (factura.getTipoComprobante() != null && !factura.getTipoComprobante().isBlank()
                && factura.getNumeroComprobanteFiscal() == null) {
            SecuenciaComprobanteFiscalService.Asignacion asignacion = secuenciasFiscales.consumirAsignacion(
                    empresaId, factura.getTipoComprobante());
            factura.setSecuenciaComprobante(asignacion.secuencia());
            factura.setNumeroComprobanteFiscal(asignacion.numero());
        }
        facturas.save(factura);

        CargoFinanciero cargo = cargos.findByEmpresaIdAndFacturaId(empresaId, facturaId).orElseGet(CargoFinanciero::new);
        cargo.setEmpresaId(empresaId);
        cargo.setPaciente(factura.getPaciente());
        cargo.setSucursal(factura.getSucursal());
        cargo.setFactura(factura);
        cargo.setOrigen("FACTURA");
        cargo.setReferenciaOrigen(factura.getNumero());
        cargo.setConcepto("Factura " + factura.getNumero());
        cargo.setFecha(factura.getFecha());
        cargo.setMontoOriginal(factura.getTotal());
        cargo.setMontoPagado(BigDecimal.ZERO.setScale(2));
        cargo.setSaldo(factura.getTotal());
        cargo.setEstado(PENDIENTE);
        cargos.save(cargo);

        registrarHistorial(factura, anterior, PENDIENTE, "EMISION", null);
        auditoria.registrar(empresaId, null, "FACTURACION", "INVOICE_ISSUED", "FACTURA", facturaId,
                factura.getNumero(), factura.getPaciente().getId(),
                List.of(new AuditoriaService.Cambio("estado", anterior, PENDIENTE),
                        new AuditoriaService.Cambio("total", null, factura.getTotal().toPlainString())),
                "SUCCESS", null, true);
        return factura;
    }

    @Transactional
    public Factura anular(UUID empresaId, UUID facturaId, String motivo, String observacion) {
        requerir(empresaId, "BILLING_VOID");
        Factura factura = bloquearFactura(empresaId, facturaId);
        if (ANULADA.equals(factura.getEstado())) return factura;
        if (BORRADOR.equals(factura.getEstado())) {
            throw new BusinessRuleException("INVOICE_VOID_NOT_ALLOWED", "Un borrador debe editarse; todavía no es una factura emitida.");
        }
        if (factura.getMontoPagado().signum() > 0 || !pagos.findByEmpresaIdAndFactura(empresaId, facturaId).isEmpty()) {
            throw new BusinessRuleException("INVOICE_HAS_PAYMENTS", "La factura tiene pagos. Anula o reembolsa los pagos antes de anularla.");
        }
        if (!Set.of("NO_APLICA", "PENDIENTE").contains(valor(factura.getEstadoEcf(), "NO_APLICA"))) {
            throw new BusinessRuleException("INVOICE_VOID_NOT_ALLOWED", "El estado fiscal/e-CF requiere procesar primero el ajuste correspondiente.");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new BusinessRuleException("VALIDATION_ERROR", "El motivo de anulación es obligatorio.");
        }
        String anterior = factura.getEstado();
        factura.setEstado(ANULADA);
        factura.setAnuladoPor(usuarioActualId());
        factura.setAnuladoEn(LocalDateTime.now());
        factura.setMotivoAnulacion(limitar(motivo, 120));
        factura.setObservacionAnulacion(limitar(observacion, 3000));
        cargos.findByEmpresaIdAndFacturaId(empresaId, facturaId).ifPresent(cargo -> {
            cargo.setSaldo(BigDecimal.ZERO.setScale(2));
            cargo.setEstado(ANULADA);
            cargos.save(cargo);
        });
        registrarHistorial(factura, anterior, ANULADA, "ANULACION", motivo + detalleObservacion(observacion));
        auditoria.registrar(empresaId, null, "FACTURACION", "INVOICE_VOIDED", "FACTURA", facturaId,
                factura.getNumero(), factura.getPaciente().getId(),
                List.of(new AuditoriaService.Cambio("estado", anterior, ANULADA)), "SUCCESS", motivo, true);
        return factura;
    }

    private void aplicarCabecera(UUID empresaId, Factura factura, FacturaSolicitud solicitud) {
        Sucursal sucursal = sucursales.findByIdAndEmpresaId(solicitud.sucursalId(), empresaId)
                .filter(s -> Boolean.TRUE.equals(s.getActiva()))
                .orElseThrow(() -> new BusinessRuleException("VALIDATION_ERROR", "La sucursal no está activa o no pertenece a esta empresa."));
        Paciente paciente = pacientes.findByIdAndEmpresaId(solicitud.pacienteId(), empresaId)
                .orElseThrow(() -> new NotFoundException("PATIENT_NOT_FOUND", "Paciente no encontrado."));
        Medico medico = solicitud.medicoId() == null ? null : medicos.findByIdAndEmpresaId(solicitud.medicoId(), empresaId)
                .filter(m -> Boolean.TRUE.equals(m.getActivo()))
                .orElseThrow(() -> new NotFoundException("DOCTOR_NOT_FOUND", "Médico no encontrado."));
        factura.setSucursal(sucursal);
        factura.setCaja(null);
        factura.setSesionCaja(null);
        factura.setPaciente(paciente);
        factura.setMedico(medico);
        factura.setFecha(solicitud.fecha() == null ? LocalDate.now() : solicitud.fecha());
        factura.setTipoComprobante(limitar(solicitud.tipoComprobante(), 30));
        factura.setOrigenTipo(normalizar(solicitud.origenTipo(), 40));
        factura.setOrigenId(solicitud.origenId());
        factura.setObservacion(limitar(solicitud.observacion(), 3000));
        factura.setMoneda("DOP");
    }

    private void guardarLineas(UUID empresaId, Factura factura, List<FacturaLineaSolicitud> solicitudes) {
        List<FacturaCalculadora.Linea> calculadas = new ArrayList<>();
        if (solicitudes != null) {
            for (FacturaLineaSolicitud solicitud : solicitudes) {
                DetalleFactura detalle = resolverLinea(empresaId, factura, solicitud);
                calculadas.add(new FacturaCalculadora.Linea(detalle.getSubtotal(), detalle.getDescuento(),
                        detalle.getSubtotal().subtract(detalle.getDescuento()), detalle.getImpuesto(), detalle.getImporte()));
                detalles.save(detalle);
            }
        }
        aplicarTotales(factura, calculadora.totalizar(calculadas));
        factura.setMontoPagado(BigDecimal.ZERO.setScale(2));
        factura.setSaldo(factura.getTotal());
        facturas.save(factura);
    }

    private DetalleFactura resolverLinea(UUID empresaId, Factura factura, FacturaLineaSolicitud solicitud) {
        if (solicitud == null || solicitud.itemId() == null) {
            throw new BusinessRuleException("INVALID_INVOICE_ITEM", "Seleccione un servicio o producto válido.");
        }
        String tipo = valor(solicitud.tipoItem(), "").trim().toUpperCase(Locale.ROOT);
        BigDecimal sugerido;
        BigDecimal tasa;
        String codigo;
        String descripcion;
        Servicio servicio = null;
        ProductoInventario producto = null;
        if ("SERVICIO".equals(tipo)) {
            servicio = servicios.findByIdAndEmpresaId(solicitud.itemId(), empresaId)
                    .orElseThrow(() -> new BusinessRuleException("SERVICE_NOT_BILLABLE", "Servicio no disponible para facturación."));
            if (!Boolean.TRUE.equals(servicio.getActivo()) || !Boolean.TRUE.equals(servicio.getFacturable())) {
                throw new BusinessRuleException("SERVICE_NOT_BILLABLE", "Servicio no disponible para facturación.");
            }
            sugerido = servicio.getPrecio(); tasa = servicio.getTasaImpuesto(); codigo = servicio.getCodigo(); descripcion = servicio.getNombre();
        } else if ("PRODUCTO".equals(tipo)) {
            producto = productos.findByIdAndEmpresaId(solicitud.itemId(), empresaId)
                    .orElseThrow(() -> new BusinessRuleException("PRODUCT_NOT_BILLABLE", "Producto no disponible para facturación."));
            if (!producto.isActivo()) throw new BusinessRuleException("PRODUCT_INACTIVE", "El producto está inactivo.");
            if (!producto.isEsFacturable() || producto.getPrecioVenta() == null) {
                throw new BusinessRuleException("PRODUCT_NOT_BILLABLE", "Producto no disponible para facturación.");
            }
            sugerido = producto.getPrecioVenta(); tasa = producto.getTasaImpuesto(); codigo = producto.getCodigo(); descripcion = producto.getNombre();
        } else {
            throw new BusinessRuleException("INVALID_INVOICE_ITEM", "El tipo de línea debe ser SERVICIO o PRODUCTO.");
        }
        BigDecimal precio = solicitud.precioUnitario() == null ? sugerido : solicitud.precioUnitario();
        if (calculadora.dinero(precio).compareTo(calculadora.dinero(sugerido)) != 0) {
            requerir(empresaId, "BILLING_CHANGE_PRICE");
        }
        BigDecimal descuento = solicitud.descuento() == null ? BigDecimal.ZERO : solicitud.descuento();
        if (descuento.signum() > 0) requerir(empresaId, "BILLING_APPLY_DISCOUNT");
        FacturaCalculadora.Linea calculo = calculadora.calcularLinea(solicitud.cantidad(), precio,
                solicitud.tipoDescuento(), descuento, tasa);
        DetalleFactura detalle = new DetalleFactura();
        detalle.setEmpresaId(empresaId); detalle.setFactura(factura); detalle.setTipoItem(tipo);
        detalle.setServicio(servicio); detalle.setProducto(producto);
        detalle.setCodigoSnapshot(valor(codigo, "SIN-CODIGO")); detalle.setDescripcion(limitar(descripcion, 255));
        detalle.setCantidad(solicitud.cantidad().setScale(2, java.math.RoundingMode.HALF_UP));
        detalle.setPrecio(calculadora.dinero(precio)); detalle.setDescuento(calculo.descuento());
        detalle.setTasaImpuesto(tasa == null ? BigDecimal.ZERO : tasa); detalle.setImpuesto(calculo.impuesto());
        detalle.setSubtotal(calculo.subtotal()); detalle.setImporte(calculo.total());
        if (precio != null && sugerido != null && calculadora.dinero(precio).compareTo(calculadora.dinero(sugerido)) != 0) {
            auditoria.registrar(empresaId, null, "FACTURACION", "INVOICE_PRICE_CHANGED", "FACTURA", factura.getId(),
                    factura.getNumero(), factura.getPaciente().getId(),
                    List.of(new AuditoriaService.Cambio("precio", calculadora.dinero(sugerido).toPlainString(), calculadora.dinero(precio).toPlainString())),
                    "SUCCESS", null, true);
        }
        if (calculo.descuento().signum() > 0) {
            auditoria.registrar(empresaId, null, "FACTURACION", "INVOICE_DISCOUNT_APPLIED", "FACTURA", factura.getId(),
                    factura.getNumero(), factura.getPaciente().getId(),
                    List.of(new AuditoriaService.Cambio("descuento", null, calculo.descuento().toPlainString())), "SUCCESS", null, true);
        }
        return detalle;
    }

    private void recalcularDesdeSnapshot(Factura factura, List<DetalleFactura> lineas) {
        List<FacturaCalculadora.Linea> calculadas = lineas.stream().map(d -> calculadora.calcularLinea(
                d.getCantidad(), d.getPrecio(), "MONTO", d.getDescuento(), d.getTasaImpuesto())).toList();
        for (int indice = 0; indice < lineas.size(); indice++) {
            DetalleFactura d = lineas.get(indice); FacturaCalculadora.Linea c = calculadas.get(indice);
            d.setSubtotal(c.subtotal()); d.setDescuento(c.descuento()); d.setImpuesto(c.impuesto()); d.setImporte(c.total());
        }
        aplicarTotales(factura, calculadora.totalizar(calculadas));
    }

    private void aplicarTotales(Factura factura, FacturaCalculadora.Totales totales) {
        factura.setSubtotal(totales.subtotal()); factura.setDescuento(totales.descuento());
        factura.setImpuestos(totales.impuestos()); factura.setTotal(totales.total());
    }

    private Specification<Factura> especificacion(UUID empresaId, FacturaFiltro filtro) {
        return (root, query, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            condiciones.add(cb.equal(root.get("empresaId"), empresaId));
            if (filtro.desde() != null) condiciones.add(cb.greaterThanOrEqualTo(root.get("fecha"), filtro.desde()));
            if (filtro.hasta() != null) condiciones.add(cb.lessThanOrEqualTo(root.get("fecha"), filtro.hasta()));
            if (!vacio(filtro.estado()) && !"TODOS".equalsIgnoreCase(filtro.estado())) condiciones.add(cb.equal(root.get("estado"), filtro.estado()));
            if (filtro.sucursalId() != null) condiciones.add(cb.equal(root.get("sucursal").get("id"), filtro.sucursalId()));
            if (filtro.pacienteId() != null) condiciones.add(cb.equal(root.get("paciente").get("id"), filtro.pacienteId()));
            if (filtro.medicoId() != null) condiciones.add(cb.equal(root.get("medico").get("id"), filtro.medicoId()));
            if (!vacio(filtro.termino())) {
                String termino = "%" + filtro.termino().trim().toLowerCase(Locale.ROOT) + "%";
                condiciones.add(cb.or(cb.like(cb.lower(root.get("numero")), termino),
                        cb.like(cb.lower(root.get("paciente").get("nombre")), termino),
                        cb.like(cb.lower(root.get("paciente").get("apellido")), termino),
                        cb.like(cb.lower(root.get("paciente").get("numeroExpediente")), termino)));
            }
            return cb.and(condiciones.toArray(Predicate[]::new));
        };
    }

    private void validarSolicitud(FacturaSolicitud solicitud) {
        if (solicitud == null || solicitud.sucursalId() == null || solicitud.pacienteId() == null) {
            throw new BusinessRuleException("VALIDATION_ERROR", "La sucursal y el paciente son obligatorios.");
        }
        if (solicitud.fecha() != null && solicitud.fecha().isAfter(LocalDate.now().plusDays(1))) {
            throw new BusinessRuleException("VALIDATION_ERROR", "La fecha de la factura no puede ser futura.");
        }
        if ((solicitud.origenTipo() == null) != (solicitud.origenId() == null)) {
            throw new BusinessRuleException("VALIDATION_ERROR", "El tipo y la referencia de origen deben informarse juntos.");
        }
    }

    private void validarOrigenUnico(UUID empresaId, String tipo, UUID origenId, UUID facturaActual) {
        if (vacio(tipo) || origenId == null) return;
        facturas.findByEmpresaIdAndOrigenTipoAndOrigenId(empresaId, tipo.trim().toUpperCase(Locale.ROOT), origenId)
                .filter(f -> !f.getId().equals(facturaActual))
                .ifPresent(f -> { throw new ConflictException("INVOICE_ALREADY_ISSUED", "Ya existe una factura para este origen."); });
    }

    private String siguienteNumero(UUID empresaId) {
        entityManager.createNativeQuery("insert into consecutivos_facturacion(empresa_id,siguiente_factura) values (?1,1) on conflict (empresa_id) do nothing")
                .setParameter(1, empresaId).executeUpdate();
        Number numero = (Number) entityManager.createNativeQuery("update consecutivos_facturacion set siguiente_factura=siguiente_factura+1 where empresa_id=?1 returning siguiente_factura-1")
                .setParameter(1, empresaId).getSingleResult();
        return "FAC-%06d".formatted(numero.longValue());
    }

    private void registrarHistorial(Factura factura, String anterior, String nuevo, String accion, String motivo) {
        HistorialFactura evento = new HistorialFactura(); evento.setEmpresaId(factura.getEmpresaId()); evento.setFactura(factura);
        evento.setEstadoAnterior(anterior); evento.setEstadoNuevo(nuevo); evento.setAccion(accion); evento.setMotivo(limitar(motivo, 3000));
        evento.setUsuarioId(usuarioActualId()); historial.save(evento);
    }

    private Factura buscarFactura(UUID empresaId, UUID facturaId) {
        if (facturaId == null) throw new NotFoundException("INVOICE_NOT_FOUND", "Factura no encontrada.");
        return facturas.findByIdAndEmpresaId(facturaId, empresaId)
                .orElseThrow(() -> new NotFoundException("INVOICE_NOT_FOUND", "Factura no encontrada."));
    }

    private Factura bloquearFactura(UUID empresaId, UUID facturaId) {
        if (facturaId == null) throw new NotFoundException("INVOICE_NOT_FOUND", "Factura no encontrada.");
        return facturas.bloquearPorIdYEmpresa(facturaId, empresaId)
                .orElseThrow(() -> new NotFoundException("INVOICE_NOT_FOUND", "Factura no encontrada."));
    }

    private void validarLectura(UUID empresaId) {
        TenantUserDetails usuario = validarTenant(empresaId);
        if (!esAdministrador(usuario) && usuario.getAuthorities().stream().noneMatch(a -> a.getAuthority().startsWith("BILLING_") || Set.of(
                "MENU_FACTURACION", "MENU_CUENTAS_COBRAR", "MENU_REPORTES", "MENU_REPORTES_FINANCIEROS")
                .contains(a.getAuthority()))) throw new ForbiddenException();
    }

    private void requerir(UUID empresaId, String permiso) {
        TenantUserDetails usuario = validarTenant(empresaId);
        if (!esAdministrador(usuario) && usuario.getAuthorities().stream().noneMatch(a -> permiso.equals(a.getAuthority()))) {
            throw new ForbiddenException();
        }
    }

    private TenantUserDetails validarTenant(UUID empresaId) {
        TenantUserDetails usuario = AuthService.getAuthenticatedUser();
        if (usuario == null || empresaId == null || !empresaId.equals(usuario.getEmpresaId())) throw new ForbiddenException();
        return usuario;
    }

    private boolean esAdministrador(TenantUserDetails usuario) {
        return usuario.getAuthorities().stream().anyMatch(a -> "ROLE_ADMINISTRADOR".equals(a.getAuthority()) || "ROLE_SUPERADMIN".equals(a.getAuthority()));
    }

    private UUID usuarioActualId() { TenantUserDetails u = AuthService.getAuthenticatedUser(); return u == null ? null : u.getUsuarioId(); }
    private int normalizarTamanio(int tamanio) { return Set.of(10, 20, 50, 100).contains(tamanio) ? tamanio : 10; }
    private boolean vacio(String valor) { return valor == null || valor.isBlank(); }
    private <T> T valor(T valor, T predeterminado) { return valor == null ? predeterminado : valor; }
    private String normalizar(String valor, int maximo) { return vacio(valor) ? null : limitar(valor.trim().toUpperCase(Locale.ROOT), maximo); }
    private String limitar(String valor, int maximo) { return valor == null ? null : valor.length() <= maximo ? valor : valor.substring(0, maximo); }
    private String detalleObservacion(String observacion) { return vacio(observacion) ? "" : " · " + observacion.trim(); }
}
