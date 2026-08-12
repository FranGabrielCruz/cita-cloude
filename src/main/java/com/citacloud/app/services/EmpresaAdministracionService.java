package com.citacloud.app.services;

import com.citacloud.app.models.Empresa;
import com.citacloud.app.models.Rol;
import com.citacloud.app.models.Sucursal;
import com.citacloud.app.models.Usuario;
import com.citacloud.app.repositories.EmpresaRepository;
import com.citacloud.app.repositories.PermisoRepository;
import com.citacloud.app.repositories.RolRepository;
import com.citacloud.app.repositories.SucursalRepository;
import com.citacloud.app.repositories.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class EmpresaAdministracionService {
    private final EmpresaRepository empresaRepository;
    private final SucursalRepository sucursalRepository;
    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PermisoRepository permisoRepository;
    private final PasswordEncoder passwordEncoder;

    public EmpresaAdministracionService(EmpresaRepository empresaRepository, SucursalRepository sucursalRepository,
                                        RolRepository rolRepository, UsuarioRepository usuarioRepository,
                                        PermisoRepository permisoRepository, PasswordEncoder passwordEncoder) {
        this.empresaRepository = empresaRepository; this.sucursalRepository = sucursalRepository;
        this.rolRepository = rolRepository; this.usuarioRepository = usuarioRepository;
        this.permisoRepository = permisoRepository; this.passwordEncoder = passwordEncoder;
    }

    public List<Empresa> buscar(String codigo, String nombre, Boolean activa) {
        String codigoFiltro = normalizar(codigo); String nombreFiltro = normalizar(nombre);
        return empresaRepository.findAll().stream()
                .filter(empresa -> codigoFiltro.isBlank() || empresa.getCodigo().toLowerCase(Locale.ROOT).contains(codigoFiltro))
                .filter(empresa -> nombreFiltro.isBlank() || empresa.getNombre().toLowerCase(Locale.ROOT).contains(nombreFiltro))
                .filter(empresa -> activa == null || activa.equals(empresa.getActiva()))
                .toList();
    }

    public List<Usuario> usuarios(UUID empresaId) { return usuarioRepository.findByEmpresaId(empresaId); }

    @Transactional
    public Empresa crear(String codigo, String nombre, String rnc, String telefono, String correo, String direccion,
                          String sucursalNombre, String adminUsuario, String adminNombre, String adminApellido,
                          String adminCorreo, String contrasena) {
        String codigoLimpio = limpiar(codigo).toUpperCase(Locale.ROOT);
        if (codigoLimpio.isBlank() || limpiar(nombre).isBlank() || limpiar(sucursalNombre).isBlank()) throw new IllegalArgumentException("Código, nombre y sucursal principal son obligatorios.");
        if (empresaRepository.findByCodigo(codigoLimpio).isPresent()) throw new IllegalArgumentException("Ya existe una empresa con ese código.");
        Empresa empresa = new Empresa(codigoLimpio, limpiar(nombre)); empresa.setRncIdentificacion(nulo(rnc)); empresa.setTelefono(nulo(telefono)); empresa.setEmail(nulo(correo)); empresa.setDireccion(nulo(direccion)); empresa.setActiva(true); empresa = empresaRepository.save(empresa);
        Sucursal sucursal = new Sucursal(); sucursal.setEmpresaId(empresa.getId()); sucursal.setCodigo("PRINCIPAL"); sucursal.setNombre(limpiar(sucursalNombre)); sucursal.setActiva(true); sucursalRepository.save(sucursal);
        Rol administrador = crearRolAdministrador(empresa.getId());
        crearAdministrador(empresa.getId(), administrador, adminUsuario, adminNombre, adminApellido, adminCorreo, contrasena);
        return empresa;
    }

    @Transactional
    public Empresa actualizar(UUID id, String codigo, String nombre, String rnc, String telefono, String correo, String direccion) {
        Empresa empresa = empresaRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada."));
        String codigoLimpio = limpiar(codigo).toUpperCase(Locale.ROOT);
        if (codigoLimpio.isBlank() || limpiar(nombre).isBlank()) throw new IllegalArgumentException("Código y nombre son obligatorios.");
        empresaRepository.findByCodigo(codigoLimpio).filter(otra -> !otra.getId().equals(id)).ifPresent(otra -> { throw new IllegalArgumentException("Ya existe una empresa con ese código."); });
        empresa.setCodigo(codigoLimpio); empresa.setNombre(limpiar(nombre)); empresa.setRncIdentificacion(nulo(rnc)); empresa.setTelefono(nulo(telefono)); empresa.setEmail(nulo(correo)); empresa.setDireccion(nulo(direccion));
        return empresaRepository.save(empresa);
    }

    @Transactional
    public void cambiarEstado(UUID id, boolean activa) { Empresa empresa = empresaRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada.")); empresa.setActiva(activa); empresaRepository.save(empresa); }

    @Transactional
    public void cambiarEstadoUsuario(UUID empresaId, UUID usuarioId, boolean activo) {
        Usuario usuario = usuarioRepository.findByIdAndEmpresaId(usuarioId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado para esta empresa."));
        usuario.setActivo(activo);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario crearAdministrador(UUID empresaId, String usuario, String nombre, String apellido, String correo, String contrasena) {
        Rol rol = crearRolAdministrador(empresaId);
        return crearAdministrador(empresaId, rol, usuario, nombre, apellido, correo, contrasena);
    }

    @Transactional
    public void restablecerContrasena(UUID empresaId, UUID usuarioId, String contrasena, String confirmacion) {
        if (contrasena == null || contrasena.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres.");
        }
        if (!contrasena.equals(confirmacion)) {
            throw new IllegalArgumentException("La confirmación de contraseña no coincide.");
        }
        Usuario usuario = usuarioRepository.findByIdAndEmpresaId(usuarioId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado para esta empresa."));
        usuario.setPasswordHash(passwordEncoder.encode(contrasena));
        usuarioRepository.save(usuario);
    }

    private Rol crearRolAdministrador(UUID empresaId) {
        return rolRepository.findByEmpresaIdAndNombre(empresaId, "ADMINISTRADOR").orElseGet(() -> { Rol rol = new Rol(); rol.setEmpresaId(empresaId); rol.setNombre("ADMINISTRADOR"); rol.setDescripcion("Acceso completo a la empresa"); rol.setActivo(true); rol.setPermisos(new HashSet<>(permisoRepository.findAll())); return rolRepository.save(rol); });
    }

    private Usuario crearAdministrador(UUID empresaId, Rol rol, String usuario, String nombre, String apellido, String correo, String contrasena) {
        String usuarioLimpio = limpiar(usuario);
        if (usuarioLimpio.isBlank() || limpiar(nombre).isBlank() || limpiar(apellido).isBlank()
                || limpiar(correo).isBlank() || contrasena == null || contrasena.length() < 6) {
            throw new IllegalArgumentException("Los datos del administrador son obligatorios; la contraseña debe tener al menos 6 caracteres.");
        }
        if (usuarioLimpio.isBlank() || limpiar(nombre).isBlank() || limpiar(apellido).isBlank() || contrasena == null || contrasena.length() < 6) throw new IllegalArgumentException("Usuario, nombre, apellido y una contraseña de al menos 6 caracteres son obligatorios.");
        if (usuarioRepository.existsByEmpresaIdAndUsuario(empresaId, usuarioLimpio)) throw new IllegalArgumentException("Ese usuario ya existe para la empresa.");
        Usuario admin = new Usuario(); admin.setEmpresaId(empresaId); admin.setUsuario(usuarioLimpio); admin.setNombre(limpiar(nombre)); admin.setApellido(limpiar(apellido)); admin.setEmail(nulo(correo)); admin.setActivo(true); admin.setPasswordHash(passwordEncoder.encode(contrasena)); admin.setRoles(new HashSet<>(java.util.Set.of(rol))); return usuarioRepository.save(admin);
    }
    private String limpiar(String texto) { return texto == null ? "" : texto.trim(); }
    private String nulo(String texto) { String limpio = limpiar(texto); return limpio.isBlank() ? null : limpio; }
    private String normalizar(String texto) { return limpiar(texto).toLowerCase(Locale.ROOT); }
}
