package com.citacloud.app.services;

import com.citacloud.app.models.Permiso;
import com.citacloud.app.models.Rol;
import com.citacloud.app.repositories.PermisoRepository;
import com.citacloud.app.repositories.RolRepository;
import com.citacloud.app.security.AuthService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class RolService {
    private final RolRepository roles;
    private final PermisoRepository permisos;

    public RolService(RolRepository roles, PermisoRepository permisos) {
        this.roles = roles;
        this.permisos = permisos;
    }

    public List<Rol> buscar(UUID empresaId, String nombre, Boolean activo) {
        String filtro = nombre == null ? "" : nombre.trim().toLowerCase(Locale.ROOT);
        return roles.findByEmpresaId(empresaId).stream()
                .filter(rol -> filtro.isBlank() || rol.getNombre().toLowerCase(Locale.ROOT).contains(filtro))
                .filter(rol -> activo == null || activo.equals(rol.getActivo())).toList();
    }

    public List<Permiso> permisosMenu() {
        return permisos.findAllByOrderByNombreAsc().stream().filter(permiso -> permiso.getCodigo().startsWith("MENU_"))
                .sorted(java.util.Comparator.comparing(Permiso::getNombre)).toList();
    }

    @Transactional
    public Rol crear(UUID empresaId, String nombre, String descripcion, Set<Permiso> permisosSeleccionados) {
        Rol rol = new Rol();
        rol.setEmpresaId(empresaId);
        rol.setActivo(true);
        actualizarDatos(rol, empresaId, nombre, descripcion, permisosSeleccionados);
        return roles.save(rol);
    }

    @Transactional
    public Rol actualizar(UUID empresaId, UUID id, String nombre, String descripcion, Set<Permiso> permisosSeleccionados) {
        Rol rol = obtener(empresaId, id);
        actualizarDatos(rol, empresaId, nombre, descripcion, permisosSeleccionados);
        return roles.save(rol);
    }

    @Transactional
    public void cambiarEstado(UUID empresaId, UUID id, boolean activo) {
        Rol rol = obtener(empresaId, id);
        rol.setActivo(activo);
        roles.save(rol);
    }

    private void actualizarDatos(Rol rol, UUID empresaId, String nombre, String descripcion,
                                 Set<Permiso> permisosSeleccionados) {
        String nombreLimpio = nombre == null ? "" : nombre.trim();
        if (nombreLimpio.isBlank()) throw new IllegalArgumentException("El nombre del rol es obligatorio.");
        boolean repetido = roles.findByEmpresaId(empresaId).stream()
                .anyMatch(item -> item.getNombre().equalsIgnoreCase(nombreLimpio) && !item.getId().equals(rol.getId()));
        if (repetido) throw new IllegalArgumentException("Ya existe un rol con ese nombre.");
        Set<UUID> ids = permisosSeleccionados == null ? Set.of() : permisosSeleccionados.stream()
                .map(Permiso::getId).collect(java.util.stream.Collectors.toSet());
        Set<Permiso> seleccionados = new HashSet<>(permisos.findAllById(ids));
        if (seleccionados.size() != ids.size()) throw new IllegalArgumentException("Uno de los permisos seleccionados no es válido.");
        boolean incluyeEmpresas = seleccionados.stream().anyMatch(this::esPermisoEmpresas);
        if (incluyeEmpresas && (!sesionEsSuperadmin() || !"SUPERADMIN".equalsIgnoreCase(nombreLimpio))) {
            throw new IllegalArgumentException("La opción Empresas está reservada exclusivamente para el perfil Superadmin.");
        }
        rol.setNombre(nombreLimpio);
        rol.setDescripcion(descripcion == null || descripcion.isBlank() ? null : descripcion.trim());
        rol.setPermisos(seleccionados);
    }

    private boolean sesionEsSuperadmin() {
        var usuario = AuthService.getAuthenticatedUser();
        return usuario != null && "SUPERADMIN".equalsIgnoreCase(usuario.getEmpresaCodigo()) && usuario.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_SUPERADMIN".equals(authority.getAuthority()));
    }

    private boolean esPermisoEmpresas(Permiso permiso) {
        return permiso.getCodigo().toUpperCase(Locale.ROOT).contains("EMPRESA")
                || permiso.getNombre().toUpperCase(Locale.ROOT).contains("EMPRESA");
    }

    private Rol obtener(UUID empresaId, UUID id) {
        return roles.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el rol solicitado."));
    }
}
