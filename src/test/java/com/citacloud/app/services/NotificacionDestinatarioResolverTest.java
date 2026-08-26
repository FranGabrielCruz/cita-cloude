package com.citacloud.app.services;

import com.citacloud.app.models.Medico;
import com.citacloud.app.models.Rol;
import com.citacloud.app.models.Usuario;
import com.citacloud.app.repositories.MedicoRepository;
import com.citacloud.app.repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class NotificacionDestinatarioResolverTest {
    @Test
    void citaSoloNotificaAdministradoresMedicoAsignadoYRecepcionDelMismoTenant() {
        UUID empresaA = UUID.randomUUID();
        UUID empresaB = UUID.randomUUID();
        UUID adminA = UUID.randomUUID(), medicoCarlos = UUID.randomUUID(), medicoElena = UUID.randomUUID(), recepcion = UUID.randomUUID();
        UUID medicoId = UUID.randomUUID();
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        MedicoRepository medicos = mock(MedicoRepository.class);
        when(usuarios.findByEmpresaId(empresaA)).thenReturn(List.of(usuario(adminA, "ADMINISTRADOR"), usuario(medicoCarlos, "MEDICO"), usuario(medicoElena, "MEDICO"), usuario(recepcion, "RECEPCIONISTA")));
        when(usuarios.findByEmpresaId(empresaB)).thenReturn(List.of(usuario(UUID.randomUUID(), "ADMINISTRADOR")));
        Medico medico = new Medico(); medico.setEmpresaId(empresaA); medico.setUsuarioId(medicoCarlos);
        when(medicos.findByIdAndEmpresaId(medicoId, empresaA)).thenReturn(Optional.of(medico));

        Set<UUID> resultado = new NotificacionDestinatarioResolver(usuarios, medicos)
                .paraCita(empresaA, "CITA_CONFIRMADA", medicoId, null);

        assertEquals(Set.of(adminA, medicoCarlos, recepcion), resultado);
        verify(usuarios, never()).findByEmpresaId(empresaB);
    }

    @Test
    void checkInNoIncluyeOtrosMedicosYPuedeIncluirEnfermeria() {
        UUID empresa = UUID.randomUUID();
        UUID admin = UUID.randomUUID(), medicoCarlos = UUID.randomUUID(), medicoElena = UUID.randomUUID(), enfermera = UUID.randomUUID();
        UUID medicoId = UUID.randomUUID();
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        MedicoRepository medicos = mock(MedicoRepository.class);
        when(usuarios.findByEmpresaId(empresa)).thenReturn(List.of(usuario(admin, "ADMINISTRADOR"), usuario(medicoCarlos, "MEDICO"), usuario(medicoElena, "MEDICO"), usuario(enfermera, "ENFERMERIA")));
        Medico medico = new Medico(); medico.setEmpresaId(empresa); medico.setUsuarioId(medicoCarlos);
        when(medicos.findByIdAndEmpresaId(medicoId, empresa)).thenReturn(Optional.of(medico));

        Set<UUID> resultado = new NotificacionDestinatarioResolver(usuarios, medicos)
                .paraCita(empresa, "PACIENTE_EN_ESPERA", medicoId, null);

        assertEquals(Set.of(admin, medicoCarlos, enfermera), resultado);
    }

    @Test
    void pagoSoloNotificaAdministradoresYPersonalFinancieroDelMismoTenant() {
        UUID empresaA = UUID.randomUUID();
        UUID empresaB = UUID.randomUUID();
        UUID admin = UUID.randomUUID(), caja = UUID.randomUUID(), medico = UUID.randomUUID(), recepcion = UUID.randomUUID();
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        MedicoRepository medicos = mock(MedicoRepository.class);
        when(usuarios.findByEmpresaId(empresaA)).thenReturn(List.of(
                usuario(admin, "ADMINISTRADOR"), usuario(caja, "CAJERO"),
                usuario(medico, "MEDICO"), usuario(recepcion, "RECEPCIONISTA")));
        when(usuarios.findByEmpresaId(empresaB)).thenReturn(List.of(usuario(UUID.randomUUID(), "ADMINISTRADOR")));

        Set<UUID> resultado = new NotificacionDestinatarioResolver(usuarios, medicos).paraPago(empresaA);

        assertEquals(Set.of(admin, caja), resultado);
        verify(usuarios, never()).findByEmpresaId(empresaB);
    }

    private Usuario usuario(UUID id, String nombreRol) {
        Usuario usuario = new Usuario(); usuario.setId(id); usuario.setActivo(true);
        Rol rol = new Rol(); rol.setNombre(nombreRol); usuario.setRoles(Set.of(rol));
        return usuario;
    }
}
