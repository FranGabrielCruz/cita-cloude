package com.citacloud.app.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pacientes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"empresa_id", "documento"})
})
public class Paciente {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(length = 50)
    private String documento;

    @Column(name = "numero_expediente", nullable = false, length = 30)
    private String numeroExpediente;

    @Column(name = "tipo_documento", length = 20)
    private String tipoDocumento = "CEDULA";

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(length = 20)
    private String genero;

    @Column(length = 30)
    private String telefono;

    @Column(length = 100)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String direccion;

    private String nacionalidad;
    private String provincia;
    private String municipio;
    @Column(name = "telefono_alternativo") private String telefonoAlternativo;
    @Column(name = "contacto_emergencia") private String contactoEmergencia;
    @Column(name = "telefono_emergencia") private String telefonoEmergencia;
    @Column(name = "parentesco_emergencia") private String parentescoEmergencia;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    public Paciente() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getEmpresaId() { return empresaId; }
    public void setEmpresaId(UUID empresaId) { this.empresaId = empresaId; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public String getNumeroExpediente() { return numeroExpediente; }
    public void setNumeroExpediente(String numeroExpediente) { this.numeroExpediente = numeroExpediente; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getNombreCompleto() { return nombre + " " + apellido; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getNacionalidad() { return nacionalidad; } public void setNacionalidad(String value) { nacionalidad=value; }
    public String getProvincia() { return provincia; } public void setProvincia(String value) { provincia=value; }
    public String getMunicipio() { return municipio; } public void setMunicipio(String value) { municipio=value; }
    public String getTelefonoAlternativo() { return telefonoAlternativo; } public void setTelefonoAlternativo(String value) { telefonoAlternativo=value; }
    public String getContactoEmergencia() { return contactoEmergencia; } public void setContactoEmergencia(String value) { contactoEmergencia=value; }
    public String getTelefonoEmergencia() { return telefonoEmergencia; } public void setTelefonoEmergencia(String value) { telefonoEmergencia=value; }
    public String getParentescoEmergencia() { return parentescoEmergencia; } public void setParentescoEmergencia(String value) { parentescoEmergencia=value; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
}
