package com.citacloud.app.services;

import com.citacloud.app.models.Permiso;
import com.citacloud.app.models.Rol;
import com.citacloud.app.repositories.PermisoRepository;
import com.citacloud.app.repositories.RolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class RolService {
    private final RolRepository rolRepository; private final PermisoRepository permisoRepository;
    public RolService(RolRepository rolRepository, PermisoRepository permisoRepository) { this.rolRepository = rolRepository; this.permisoRepository = permisoRepository; }
    public List<Rol> buscar(UUID empresaId, String nombre, Boolean activo) { String filtro = nombre == null ? "" : nombre.trim().toLowerCase(Locale.ROOT); return rolRepository.findByEmpresaId(empresaId).stream().filter(rol -> filtro.isBlank() || rol.getNombre().toLowerCase(Locale.ROOT).contains(filtro)).filter(rol -> activo == null || activo.equals(rol.getActivo())).toList(); }
    public List<Permiso> permisosMenu() { return permisoRepository.findAllByOrderByNombreAsc().stream().filter(permiso -> permiso.getCodigo().startsWith("MENU_")).sorted(java.util.Comparator.comparing(Permiso::getNombre)).toList(); }
    @Transactional public Rol crear(UUID empresaId, String nombre, String descripcion, Set<Permiso> permisos) { Rol rol = new Rol(); rol.setEmpresaId(empresaId); rol.setActivo(true); actualizarDatos(rol, empresaId, nombre, descripcion, permisos); return rolRepository.save(rol); }
    @Transactional public Rol actualizar(UUID empresaId, UUID id, String nombre, String descripcion, Set<Permiso> permisos) { Rol rol = obtener(empresaId, id); actualizarDatos(rol, empresaId, nombre, descripcion, permisos); return rolRepository.save(rol); }
    @Transactional public void cambiarEstado(UUID empresaId, UUID id, boolean activo) { Rol rol = obtener(empresaId, id); rol.setActivo(activo); rolRepository.save(rol); }
    private void actualizarDatos(Rol rol, UUID empresaId, String nombre, String descripcion, Set<Permiso> permisos) { String limpio = nombre == null ? "" : nombre.trim(); if (limpio.isBlank()) throw new IllegalArgumentException("El nombre del rol es obligatorio."); boolean repetido = rolRepository.findByEmpresaId(empresaId).stream().anyMatch(item -> item.getNombre().equalsIgnoreCase(limpio) && !item.getId().equals(rol.getId())); if (repetido) throw new IllegalArgumentException("Ya existe un rol con ese nombre."); Set<UUID> ids = permisos == null ? Set.of() : permisos.stream().map(Permiso::getId).collect(java.util.stream.Collectors.toSet()); Set<Permiso> permitidos = new HashSet<>(permisoRepository.findAllById(ids)); if (permitidos.size() != ids.size()) throw new IllegalArgumentException("Uno de los permisos seleccionados no es v\u00e1lido."); rol.setNombre(limpio); rol.setDescripcion(descripcion == null || descripcion.isBlank() ? null : descripcion.trim()); rol.setPermisos(permitidos); }
    private Rol obtener(UUID empresaId, UUID id) { return rolRepository.findByIdAndEmpresaId(id, empresaId).orElseThrow(() -> new IllegalArgumentException("No se encontr\u00f3 el rol solicitado.")); }
}
