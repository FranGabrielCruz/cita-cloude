package com.citacloud.app.models;
import jakarta.persistence.*; import java.util.UUID;
@Entity @Table(name="categorias_inventario") public class CategoriaInventario { @Id @GeneratedValue private UUID id; @Column(name="empresa_id") private UUID empresaId; private String nombre; private boolean activa=true; public UUID getId(){return id;} public UUID getEmpresaId(){return empresaId;} public void setEmpresaId(UUID v){empresaId=v;} public String getNombre(){return nombre;} public void setNombre(String v){nombre=v;} public boolean isActiva(){return activa;} }
