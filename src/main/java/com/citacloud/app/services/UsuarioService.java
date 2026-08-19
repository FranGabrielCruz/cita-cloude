package com.citacloud.app.services;

import com.citacloud.app.models.Rol;
import com.citacloud.app.models.Usuario;
import com.citacloud.app.repositories.RolRepository;
import com.citacloud.app.repositories.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> buscar(UUID empresaId, String usuario, String nombre, UUID rolId, Boolean activo) {
        String usuarioFiltro = normalizar(usuario);
        String nombreFiltro = normalizar(nombre);
        return usuarioRepository.findByEmpresaId(empresaId).stream()
                .filter(item -> usuarioFiltro.isBlank() || item.getUsuario().toLowerCase(Locale.ROOT).contains(usuarioFiltro))
                .filter(item -> nombreFiltro.isBlank() || item.getNombreCompleto().toLowerCase(Locale.ROOT).contains(nombreFiltro))
                .filter(item -> rolId == null || item.getRoles().stream().anyMatch(rol -> rolId.equals(rol.getId())))
                .filter(item -> activo == null || activo.equals(item.getActivo()))
                .toList();
    }

    public List<Rol> listarRoles(UUID empresaId) {
        return rolRepository.findByEmpresaId(empresaId);
    }

    @Transactional
    public Usuario crear(UUID empresaId, String nombreUsuario, String nombre, String apellido, String email,
                         String telefono, String contrasena, String confirmacion, Rol rol) {
        if (usuarioRepository.existsByEmpresaIdAndUsuario(empresaId, limpiar(nombreUsuario))) {
            throw new IllegalArgumentException("Ese usuario ya existe para esta empresa.");
        }
        Usuario usuario = new Usuario();
        usuario.setEmpresaId(empresaId);
        actualizarDatos(usuario, empresaId, nombreUsuario, nombre, apellido, email, telefono, contrasena, confirmacion, rol, true);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario actualizar(UUID empresaId, UUID id, String nombreUsuario, String nombre, String apellido,
                              String email, String telefono, String contrasena, String confirmacion, Rol rol) {
        Usuario usuario = obtener(empresaId, id);
        String usuarioLimpio = limpiar(nombreUsuario);
        boolean duplicado = usuarioRepository.findByEmpresaIdAndUsuario(empresaId, usuarioLimpio)
                .filter(item -> !item.getId().equals(id)).isPresent();
        if (duplicado) {
            throw new IllegalArgumentException("Ese usuario ya existe para esta empresa.");
        }
        actualizarDatos(usuario, empresaId, usuarioLimpio, nombre, apellido, email, telefono, contrasena, confirmacion, rol, false);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void cambiarEstado(UUID empresaId, UUID id, boolean activo) {
        Usuario usuario = obtener(empresaId, id);
        usuario.setActivo(activo);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void guardarPreferenciaTema(UUID empresaId, UUID id, String tema) {
        if (!Set.of("CLARO", "OSCURO", "SISTEMA").contains(tema)) {
            throw new IllegalArgumentException("Tema no válido.");
        }
        Usuario usuario = obtener(empresaId, id);
        usuario.setPreferenciaTema(tema);
        usuarioRepository.save(usuario);
    }

    private void actualizarDatos(Usuario usuario, UUID empresaId, String nombreUsuario, String nombre, String apellido,
                                 String email, String telefono, String contrasena, String confirmacion, Rol rol,
                                 boolean contrasenaObligatoria) {
        if (limpiar(nombreUsuario).isBlank() || limpiar(nombre).isBlank() || limpiar(apellido).isBlank() || rol == null) {
            throw new IllegalArgumentException("Usuario, nombre, apellido y rol son obligatorios.");
        }
        if (!empresaId.equals(rol.getEmpresaId())) {
            throw new IllegalArgumentException("El rol seleccionado no pertenece a la empresa.");
        }
        if (email != null && !email.isBlank() && !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Ingresa un correo electr\u00f3nico v\u00e1lido.");
        }
        if (telefono != null && !telefono.isBlank() && !telefono.matches("\\(\\d{3}\\) \\d{3}-\\d{4}")) {
            throw new IllegalArgumentException("El tel\u00e9fono debe tener el formato (000) 000-0000.");
        }
        boolean cambiarContrasena = contrasena != null && !contrasena.isBlank();
        if (contrasenaObligatoria && !cambiarContrasena) {
            throw new IllegalArgumentException("La contrase\u00f1a es obligatoria.");
        }
        if (cambiarContrasena) {
            if (contrasena.length() < 6) throw new IllegalArgumentException("La contrase\u00f1a debe tener al menos 6 caracteres.");
            if (!contrasena.equals(confirmacion)) throw new IllegalArgumentException("La confirmaci\u00f3n de contrase\u00f1a no coincide.");
            usuario.setPasswordHash(passwordEncoder.encode(contrasena));
        }
        usuario.setUsuario(limpiar(nombreUsuario));
        usuario.setNombre(limpiar(nombre));
        usuario.setApellido(limpiar(apellido));
        usuario.setEmail(email == null || email.isBlank() ? null : email.trim());
        usuario.setTelefono(telefono == null || telefono.isBlank() ? null : telefono.trim());
        usuario.setRoles(new HashSet<>(Set.of(rol)));
    }

    private Usuario obtener(UUID empresaId, UUID id) {
        return usuarioRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontr\u00f3 el usuario solicitado."));
    }

    public String nombreCompleto(UUID empresaId, UUID id) {
        return id == null ? "—" : obtener(empresaId, id).getNombreCompleto();
    }

    private String limpiar(String valor) { return valor == null ? "" : valor.trim(); }
    private String normalizar(String valor) { return limpiar(valor).toLowerCase(Locale.ROOT); }
}
