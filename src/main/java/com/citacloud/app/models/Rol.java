package com.citacloud.app.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"empresa_id", "nombre"})
})
public class Rol {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    private Boolean activo = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "rol_permisos", joinColumns = @JoinColumn(name = "rol_id"), inverseJoinColumns = @JoinColumn(name = "permiso_id"))
    private Set<Permiso> permisos = new HashSet<>();

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    public Rol() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getEmpresaId() { return empresaId; }
    public void setEmpresaId(UUID empresaId) { this.empresaId = empresaId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public Set<Permiso> getPermisos() { return permisos; }
    public void setPermisos(Set<Permiso> permisos) { this.permisos = permisos; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
}
