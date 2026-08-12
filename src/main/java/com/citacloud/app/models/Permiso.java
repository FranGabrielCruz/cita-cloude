package com.citacloud.app.models;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "permisos")
public class Permiso {
    @Id @GeneratedValue private UUID id;
    @Column(nullable = false, unique = true) private String codigo;
    @Column(nullable = false) private String nombre;
    @Column private String descripcion;
    public UUID getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
}
