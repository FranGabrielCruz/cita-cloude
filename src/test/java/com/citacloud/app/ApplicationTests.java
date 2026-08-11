package com.citacloud.app;

import com.citacloud.app.models.Empresa;
import com.citacloud.app.models.Usuario;
import com.citacloud.app.repositories.EmpresaRepository;
import com.citacloud.app.repositories.UsuarioRepository;
import com.citacloud.app.security.AuthService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApplicationTests {

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthService(empresaRepository, usuarioRepository, new BCryptPasswordEncoder());
    }

    @Test
    @DisplayName("Login exitoso con empresa, usuario y contraseña válidos")
    void testLoginExitoso() {
        UUID empresaId = UUID.randomUUID();
        Empresa empresa = new Empresa("CLINICA01", "Clínica San Rafael");
        empresa.setId(empresaId);
        empresa.setActiva(true);

        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setEmpresaId(empresaId);
        usuario.setUsuario("admin");
        usuario.setPasswordHash(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("admin123"));
        usuario.setNombre("Gabriel");
        usuario.setApellido("Martínez");
        usuario.setActivo(true);

        when(empresaRepository.findByCodigo("CLINICA01")).thenReturn(Optional.of(empresa));
        when(usuarioRepository.findByEmpresaIdAndUsuario(empresaId, "admin")).thenReturn(Optional.of(usuario));

        boolean resultado = authService.login("CLINICA01", "admin", "admin123");
        assertTrue(resultado, "El login multitenant debe ser exitoso");
    }

    @Test
    @DisplayName("Login fallido cuando el código de empresa no existe")
    void testLoginEmpresaInexistente() {
        when(empresaRepository.findByCodigo("EMPRESA_FAKER")).thenReturn(Optional.empty());

        boolean resultado = authService.login("EMPRESA_FAKER", "admin", "admin123");
        assertFalse(resultado, "El login debe fallar si la empresa no existe");
    }

    @Test
    @DisplayName("Login fallido cuando la contraseña es incorrecta")
    void testLoginPasswordIncorrecto() {
        UUID empresaId = UUID.randomUUID();
        Empresa empresa = new Empresa("CLINICA01", "Clínica San Rafael");
        empresa.setId(empresaId);
        empresa.setActiva(true);

        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setEmpresaId(empresaId);
        usuario.setUsuario("admin");
        usuario.setPasswordHash("$2a$10$eD4iQpL64WdG1J4W0m4oSe6yO1Yh6d6u0X3w5t/A8bC4dE6fG8h2i"); // hash BCrypt
        usuario.setNombre("Gabriel");
        usuario.setApellido("Martínez");
        usuario.setActivo(true);

        when(empresaRepository.findByCodigo("CLINICA01")).thenReturn(Optional.of(empresa));
        when(usuarioRepository.findByEmpresaIdAndUsuario(empresaId, "admin")).thenReturn(Optional.of(usuario));

        boolean resultado = authService.login("CLINICA01", "admin", "password_equivocado");
        assertFalse(resultado, "El login debe fallar con contraseña incorrecta");
    }
}
