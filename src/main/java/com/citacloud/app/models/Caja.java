package com.citacloud.app.models;
import jakarta.persistence.*; import java.time.LocalDateTime; import java.util.*;
@Entity @Table(name="cajas",uniqueConstraints=@UniqueConstraint(name="uq_caja_empresa_codigo",columnNames={"empresa_id","codigo"}))
public class Caja {
 @Id @GeneratedValue private UUID id; @Column(name="empresa_id",nullable=false) private UUID empresaId; @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="sucursal_id",nullable=false) private Sucursal sucursal;
 @Column(nullable=false) private String codigo; @Column(nullable=false) private String nombre; private String descripcion; @Column(nullable=false) private boolean activa=true; @Column(name="creado_en") private LocalDateTime creadoEn=LocalDateTime.now();
 @ManyToMany(fetch=FetchType.EAGER) @JoinTable(name="cajas_usuarios",joinColumns=@JoinColumn(name="caja_id"),inverseJoinColumns=@JoinColumn(name="usuario_id")) private Set<Usuario> usuarios=new LinkedHashSet<>();
 @Transient private String estadoOperativo; @Transient private UUID usuarioActual;
 public UUID getId(){return id;} public UUID getEmpresaId(){return empresaId;} public void setEmpresaId(UUID v){empresaId=v;} public Sucursal getSucursal(){return sucursal;} public void setSucursal(Sucursal v){sucursal=v;} public String getCodigo(){return codigo;} public void setCodigo(String v){codigo=v;} public String getNombre(){return nombre;} public void setNombre(String v){nombre=v;} public String getDescripcion(){return descripcion;} public void setDescripcion(String v){descripcion=v;} public boolean isActiva(){return activa;} public void setActiva(boolean v){activa=v;} public LocalDateTime getCreadoEn(){return creadoEn;} public Set<Usuario> getUsuarios(){return usuarios;} public String getEstadoOperativo(){return estadoOperativo;} public void setEstadoOperativo(String v){estadoOperativo=v;} public UUID getUsuarioActual(){return usuarioActual;} public void setUsuarioActual(UUID v){usuarioActual=v;}
}
