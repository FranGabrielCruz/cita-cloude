package com.citacloud.app.unit;

import com.citacloud.app.exceptions.ForbiddenException;
import com.citacloud.app.models.*;
import com.citacloud.app.repositories.*;
import com.citacloud.app.security.TenantUserDetails;
import com.citacloud.app.services.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FacturacionServiceTest {
    @Mock FacturaRepository facturas; @Mock DetalleFacturaRepository detalles; @Mock HistorialFacturaRepository historial;
    @Mock PagoRepository pagos; @Mock PacienteRepository pacientes; @Mock SucursalRepository sucursales;
    @Mock MedicoRepository medicos; @Mock ServicioRepository servicios; @Mock ProductoInventarioRepository productos;
    @Mock CargoFinancieroRepository cargos; @Mock AuditoriaEventoRepository auditoriaRepo;
    @Mock SesionCajaRepository sesionesCaja; @Mock CajaRepository cajasRepo; @Mock SecuenciaComprobanteFiscalRepository secuenciasRepo;
    private CajaService cajaService; private SecuenciaComprobanteFiscalService secuenciasFiscales;
    private AutoCloseable mocks; private FacturacionService service; private AuditoriaService auditoria; private UUID empresa;

    @BeforeEach void preparar() {
        mocks = MockitoAnnotations.openMocks(this); empresa = UUID.randomUUID(); auditoria = new AuditoriaService(auditoriaRepo, null);
        TenantUserDetails usuario = new TenantUserDetails(UUID.randomUUID(), empresa, "CLINICA", "Clínica", "admin", "", "Admin",
                List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities()));
        cajaService = new CajaService(sesionesCaja, null, null, cajasRepo, null, null, null, null, null, null);
        secuenciasFiscales = new SecuenciaComprobanteFiscalService(secuenciasRepo);
        service = new FacturacionService(facturas, detalles, historial, pagos, pacientes, sucursales, medicos,
                servicios, productos, cargos, auditoria, new FacturaCalculadora(), cajaService, secuenciasFiscales, null);
    }

    @AfterEach void limpiar() throws Exception { SecurityContextHolder.clearContext(); if (mocks != null) mocks.close(); }

    @Test void emiteUnaSolaVezYCreaLaCuentaPorCobrar() {
        UUID facturaId = UUID.randomUUID(); Factura factura = facturaBorrador(); DetalleFactura linea = linea(factura);
        when(facturas.bloquearPorIdYEmpresa(facturaId, empresa)).thenReturn(Optional.of(factura));
        when(detalles.findByEmpresaIdAndFacturaIdOrderByCreadoEnAsc(empresa, facturaId)).thenReturn(List.of(linea));
        when(cargos.findByEmpresaIdAndFacturaId(empresa, facturaId)).thenReturn(Optional.empty());
        SesionCaja sesion = new SesionCaja(); Caja caja = new Caja(); sesion.setCaja(caja);
        when(sesionesCaja.findFirstByEmpresaIdAndSucursalIdAndAbiertoPorAndEstadoOrderByAbiertoEnDesc(eq(empresa), any(), any(), eq("OPEN"))).thenReturn(Optional.of(sesion));

        Factura emitida = service.emitir(empresa, facturaId);
        assertAll(() -> assertEquals("PENDIENTE", emitida.getEstado()),
                () -> assertEquals(new BigDecimal("1180.00"), emitida.getTotal()),
                () -> assertEquals(new BigDecimal("1180.00"), emitida.getSaldo()));
        ArgumentCaptor<CargoFinanciero> cargo = ArgumentCaptor.forClass(CargoFinanciero.class);
        verify(cargos).save(cargo.capture()); assertEquals(emitida.getTotal(), cargo.getValue().getSaldo());

        assertSame(emitida, service.emitir(empresa, facturaId));
        verify(cargos, times(1)).save(any());
    }

    @Test void noEmiteSinCajaActiva() {
        UUID facturaId = UUID.randomUUID(); Factura factura = facturaBorrador(); DetalleFactura linea = linea(factura);
        when(facturas.bloquearPorIdYEmpresa(facturaId, empresa)).thenReturn(Optional.of(factura));
        when(detalles.findByEmpresaIdAndFacturaIdOrderByCreadoEnAsc(empresa, facturaId)).thenReturn(List.of(linea));
        when(sesionesCaja.findFirstByEmpresaIdAndSucursalIdAndAbiertoPorAndEstadoOrderByAbiertoEnDesc(eq(empresa), any(), any(), eq("OPEN"))).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.emitir(empresa, facturaId));
        verifyNoInteractions(cargos);
    }

    @Test void emiteEnLaCajaSeleccionadaConTurnoAbierto() {
        UUID facturaId = UUID.randomUUID(), cajaId = UUID.randomUUID(); Factura factura = facturaBorrador(); DetalleFactura linea = linea(factura);
        Caja caja = new Caja(); asignarId(caja, cajaId); caja.setEmpresaId(empresa); caja.setActiva(true); caja.setSucursal(factura.getSucursal());
        SesionCaja sesion = new SesionCaja(); asignarId(sesion, UUID.randomUUID()); sesion.setEmpresaId(empresa); sesion.setCaja(caja); sesion.setSucursal(factura.getSucursal()); sesion.setEstado("OPEN");
        when(facturas.bloquearPorIdYEmpresa(facturaId, empresa)).thenReturn(Optional.of(factura));
        when(detalles.findByEmpresaIdAndFacturaIdOrderByCreadoEnAsc(empresa, facturaId)).thenReturn(List.of(linea));
        when(cargos.findByEmpresaIdAndFacturaId(empresa, facturaId)).thenReturn(Optional.empty());
        when(cajasRepo.findByIdAndEmpresaId(cajaId, empresa)).thenReturn(Optional.of(caja));
        when(sesionesCaja.findByEmpresaIdAndCajaIdAndEstado(empresa, cajaId, "OPEN")).thenReturn(Optional.of(sesion));

        Factura emitida = service.emitir(empresa, facturaId, cajaId);

        assertSame(caja, emitida.getCaja()); assertSame(sesion, emitida.getSesionCaja());
    }

    @Test void bloqueaEmisionCuandoLaCajaSeleccionadaEstaCerrada() {
        UUID facturaId = UUID.randomUUID(), cajaId = UUID.randomUUID(); Factura factura = facturaBorrador();
        Caja caja = new Caja(); asignarId(caja, cajaId); caja.setEmpresaId(empresa); caja.setActiva(true); caja.setSucursal(factura.getSucursal());
        when(facturas.bloquearPorIdYEmpresa(facturaId, empresa)).thenReturn(Optional.of(factura));
        when(detalles.findByEmpresaIdAndFacturaIdOrderByCreadoEnAsc(empresa, facturaId)).thenReturn(List.of(linea(factura)));
        when(cajasRepo.findByIdAndEmpresaId(cajaId, empresa)).thenReturn(Optional.of(caja));
        when(sesionesCaja.findByEmpresaIdAndCajaIdAndEstado(empresa, cajaId, "OPEN")).thenReturn(Optional.empty());

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> service.emitir(empresa, facturaId, cajaId));

        assertTrue(error.getMessage().contains("cerrada")); verifyNoInteractions(cargos);
    }

    @Test void bloqueaConsultaConTenantManipulado() {
        assertThrows(ForbiddenException.class, () -> service.listar(UUID.randomUUID()));
        verifyNoInteractions(facturas);
    }

    private Factura facturaBorrador() {
        Factura f = new Factura(); f.setEmpresaId(empresa); f.setNumero("FAC-000001"); f.setFecha(LocalDate.now()); f.setEstado("BORRADOR");
        Paciente p = new Paciente(); p.setEmpresaId(empresa); p.setNombre("Ana"); p.setApellido("Pérez"); p.setNumeroExpediente("HC-1"); f.setPaciente(p);
        Sucursal s = new Sucursal(); asignarId(s, UUID.randomUUID()); s.setEmpresaId(empresa); s.setNombre("Central"); s.setActiva(true); f.setSucursal(s); return f;
    }

    private DetalleFactura linea(Factura factura) {
        DetalleFactura d = new DetalleFactura(); d.setEmpresaId(empresa); d.setFactura(factura); d.setTipoItem("SERVICIO");
        d.setCodigoSnapshot("SER-1"); d.setDescripcion("Consulta"); d.setCantidad(BigDecimal.ONE); d.setPrecio(new BigDecimal("1000"));
        d.setDescuento(BigDecimal.ZERO); d.setTasaImpuesto(new BigDecimal("18")); d.setSubtotal(new BigDecimal("1000"));
        d.setImpuesto(new BigDecimal("180")); d.setImporte(new BigDecimal("1180")); return d;
    }

    private void asignarId(Object entidad, UUID id) {
        try { var campo = entidad.getClass().getDeclaredField("id"); campo.setAccessible(true); campo.set(entidad, id); }
        catch (ReflectiveOperationException e) { throw new IllegalStateException(e); }
    }
}
