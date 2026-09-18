package com.citacloud.app.unit;

import com.citacloud.app.models.SecuenciaComprobanteFiscal;
import com.citacloud.app.repositories.SecuenciaComprobanteFiscalRepository;
import com.citacloud.app.security.TenantUserDetails;
import com.citacloud.app.services.SecuenciaComprobanteFiscalService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecuenciaComprobanteFiscalServiceTest {
    @Mock SecuenciaComprobanteFiscalRepository repositorio;
    private AutoCloseable mocks;
    private UUID empresa;
    private SecuenciaComprobanteFiscalService servicio;

    @BeforeEach void preparar() {
        mocks = MockitoAnnotations.openMocks(this); empresa = UUID.randomUUID();
        TenantUserDetails usuario = new TenantUserDetails(UUID.randomUUID(), empresa, "CLINICA", "Clínica", "admin", "", "Admin",
                List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities()));
        servicio = new SecuenciaComprobanteFiscalService(repositorio);
    }

    @AfterEach void limpiar() throws Exception { SecurityContextHolder.clearContext(); mocks.close(); }

    @Test void consumeElSiguienteNumeroDeFormaControlada() {
        SecuenciaComprobanteFiscal secuencia = secuencia(15L, 20L);
        when(repositorio.findByEmpresaIdAndTipoAndActivaTrue(empresa, "CREDITO_FISCAL")).thenReturn(Optional.of(secuencia));
        when(repositorio.bloquear(secuencia.getId(), empresa)).thenReturn(Optional.of(secuencia));
        SecuenciaComprobanteFiscalService.Asignacion asignacion = servicio.consumirAsignacion(empresa, "CREDITO_FISCAL");
        assertEquals("B0100000015", asignacion.numero());
        assertEquals(16L, secuencia.getNumeroSiguiente());
        verify(repositorio).save(secuencia);
    }

    @Test void rechazaUnaSecuenciaAgotada() {
        SecuenciaComprobanteFiscal secuencia = secuencia(21L, 20L);
        when(repositorio.findByEmpresaIdAndTipoAndActivaTrue(empresa, "CREDITO_FISCAL")).thenReturn(Optional.of(secuencia));
        when(repositorio.bloquear(secuencia.getId(), empresa)).thenReturn(Optional.of(secuencia));
        assertThrows(IllegalArgumentException.class, () -> servicio.consumirAsignacion(empresa, "CREDITO_FISCAL"));
        verify(repositorio, never()).save(any());
    }

    private SecuenciaComprobanteFiscal secuencia(long siguiente, long hasta) {
        SecuenciaComprobanteFiscal secuencia = new SecuenciaComprobanteFiscal();
        asignarId(secuencia, UUID.randomUUID()); secuencia.setEmpresaId(empresa); secuencia.setTipo("CREDITO_FISCAL");
        secuencia.setNombre("Crédito fiscal"); secuencia.setPrefijo("B01"); secuencia.setNumeroDesde(1L);
        secuencia.setNumeroHasta(hasta); secuencia.setNumeroSiguiente(siguiente); secuencia.setActiva(true); return secuencia;
    }

    private void asignarId(Object entidad, UUID id) {
        try { var campo = entidad.getClass().getDeclaredField("id"); campo.setAccessible(true); campo.set(entidad, id); }
        catch (ReflectiveOperationException e) { throw new IllegalStateException(e); }
    }
}
