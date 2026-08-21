package com.citacloud.app.services;

import com.citacloud.app.models.Paciente;
import com.citacloud.app.repositories.PacienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class PacienteService {

    private static final Pattern CEDULA_VALIDA = Pattern.compile("\\d{3}-\\d{7}-\\d");
    private static final Pattern TELEFONO_VALIDO = Pattern.compile("\\(\\d{3}\\) \\d{3}-\\d{4}");
    private static final Pattern EMAIL_VALIDO = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final PacienteRepository pacienteRepository;

    public PacienteService(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    public List<Paciente> listarPorEmpresa(UUID empresaId) {
        return pacienteRepository.findByEmpresaId(empresaId);
    }

    public List<Paciente> listarActivos(UUID empresaId) {
        return pacienteRepository.findByEmpresaIdAndActivoTrue(empresaId);
    }

    public List<Paciente> buscar(UUID empresaId, String filtro) {
        if (filtro == null || filtro.isBlank()) {
            return listarPorEmpresa(empresaId);
        }
        return pacienteRepository.buscarPorTermino(empresaId, filtro.trim());
    }

    public Paciente guardar(Paciente paciente) {
        validarDatos(paciente.getTipoDocumento(), paciente.getDocumento(), paciente.getNombre(), paciente.getApellido(), paciente.getTelefono(), paciente.getEmail());
        if (paciente.getDocumento() != null && pacienteRepository.existsByEmpresaIdAndDocumento(paciente.getEmpresaId(), paciente.getDocumento())) {
            throw new IllegalArgumentException("El documento ya existe para esta empresa.");
        }
        paciente.setNumeroExpediente(generarExpediente(paciente.getEmpresaId()));
        return pacienteRepository.save(paciente);
    }

    public Paciente actualizarPerfil(UUID empresaId, Paciente datos) {
        Paciente paciente = pacienteRepository.findByIdAndEmpresaId(datos.getId(), empresaId).orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado para esta empresa."));
        paciente.setTipoDocumento(datos.getTipoDocumento()); paciente.setFechaNacimiento(datos.getFechaNacimiento()); paciente.setGenero(datos.getGenero()); paciente.setDireccion(datos.getDireccion()); paciente.setNacionalidad(datos.getNacionalidad()); paciente.setProvincia(datos.getProvincia()); paciente.setMunicipio(datos.getMunicipio()); paciente.setTelefonoAlternativo(datos.getTelefonoAlternativo()); paciente.setContactoEmergencia(datos.getContactoEmergencia()); paciente.setTelefonoEmergencia(datos.getTelefonoEmergencia()); paciente.setParentescoEmergencia(datos.getParentescoEmergencia());
        return pacienteRepository.save(paciente);
    }

    public Paciente actualizar(UUID empresaId, UUID pacienteId, String cedula, String nombre,
                               String apellido, String telefono, String email) {
        Paciente paciente = pacienteRepository.findByIdAndEmpresaId(pacienteId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado para esta empresa."));
        validarDatos(paciente.getTipoDocumento(), cedula, nombre, apellido, telefono, email);
        if (!java.util.Objects.equals(paciente.getDocumento(), cedula)
                && pacienteRepository.existsByEmpresaIdAndDocumento(empresaId, cedula)) {
            throw new IllegalArgumentException("La cédula ya existe para esta empresa.");
        }
        paciente.setDocumento(cedula);
        paciente.setNombre(nombre);
        paciente.setApellido(apellido);
        paciente.setTelefono(telefono);
        paciente.setEmail(email);
        return pacienteRepository.save(paciente);
    }

    public void desactivar(UUID empresaId, UUID pacienteId) {
        Paciente paciente = pacienteRepository.findByIdAndEmpresaId(pacienteId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado para esta empresa."));
        paciente.setActivo(false);
        pacienteRepository.save(paciente);
    }

    public void activar(UUID empresaId, UUID pacienteId) {
        Paciente paciente = pacienteRepository.findByIdAndEmpresaId(pacienteId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado para esta empresa."));
        paciente.setActivo(true);
        pacienteRepository.save(paciente);
    }

    private String generarExpediente(UUID empresaId) { long siguiente=pacienteRepository.countByEmpresaId(empresaId)+1; String numero; do { numero="HC-"+String.format("%07d",siguiente++); } while(pacienteRepository.existsByEmpresaIdAndNumeroExpediente(empresaId,numero)); return numero; }
    private void validarDatos(String tipoDocumento, String documento, String nombre, String apellido, String telefono, String email) {
        if (nombre == null || nombre.isBlank() || apellido == null || apellido.isBlank() || telefono == null || telefono.isBlank()) {
            throw new IllegalArgumentException("Completa todos los campos obligatorios.");
        }
        if (!"SIN_DOCUMENTO".equals(tipoDocumento) && (documento == null || documento.isBlank())) throw new IllegalArgumentException("El número de documento es obligatorio.");
        if ("CEDULA".equals(tipoDocumento) && !CEDULA_VALIDA.matcher(documento).matches()) {
            throw new IllegalArgumentException("La cédula debe tener el formato 000-0000000-0.");
        }
        if (!TELEFONO_VALIDO.matcher(telefono).matches()) {
            throw new IllegalArgumentException("El teléfono debe tener el formato (000) 000-0000.");
        }
        if (email != null && !email.isBlank() && !EMAIL_VALIDO.matcher(email).matches()) {
            throw new IllegalArgumentException("Ingresa un correo electrónico válido.");
        }
    }
}
