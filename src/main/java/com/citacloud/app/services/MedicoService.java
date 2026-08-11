package com.citacloud.app.services;

import com.citacloud.app.models.Medico;
import com.citacloud.app.models.Rol;
import com.citacloud.app.models.Usuario;
import com.citacloud.app.repositories.MedicoRepository;
import com.citacloud.app.repositories.RolRepository;
import com.citacloud.app.repositories.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class MedicoService {

    private static final Pattern CODIGO_MEDICO = Pattern.compile("^MED(\\d+)$");

    private final MedicoRepository medicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public MedicoService(MedicoRepository medicoRepository, UsuarioRepository usuarioRepository,
                         RolRepository rolRepository, PasswordEncoder passwordEncoder) {
        this.medicoRepository = medicoRepository;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Medico> listarPorEmpresa(UUID empresaId) {
        return medicoRepository.findByEmpresaId(empresaId);
    }

    public List<Medico> listarActivos(UUID empresaId) {
        return medicoRepository.findByEmpresaIdAndActivoTrue(empresaId);
    }

    public List<Medico> buscar(UUID empresaId, String codigo, String nombre, UUID especialidadId) {
        String codigoNormalizado = codigo == null ? "" : codigo.trim().toLowerCase();
        String nombreNormalizado = nombre == null ? "" : nombre.trim().toLowerCase();
        return listarPorEmpresa(empresaId).stream()
                .filter(medico -> codigoNormalizado.isBlank() || medico.getCodigo().toLowerCase().contains(codigoNormalizado))
                .filter(medico -> nombreNormalizado.isBlank() || medico.getNombreCompleto().toLowerCase().contains(nombreNormalizado))
                .filter(medico -> especialidadId == null || medico.getEspecialidades().stream()
                        .anyMatch(especialidad -> especialidadId.equals(especialidad.getId())))
                .toList();
    }

    @Transactional
    public Medico registrar(UUID empresaId, Medico medico, boolean crearUsuario, String nombreUsuario,
                            String contrasena, String confirmacionContrasena, String nombreRol,
                            boolean usuarioActivo) {
        medico.setCodigo(generarCodigo(empresaId));
        validarMedico(empresaId, medico);
        medico.setEmpresaId(empresaId);
        medico.setActivo(true);
        if (crearUsuario) {
            crearUsuario(empresaId, medico, nombreUsuario, contrasena, confirmacionContrasena, nombreRol, usuarioActivo);
        }
        return medicoRepository.save(medico);
    }

    @Transactional
    public Medico actualizar(UUID empresaId, UUID medicoId, Medico datos) {
        Medico medico = medicoRepository.findByIdAndEmpresaId(medicoId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("M\u00e9dico no encontrado para esta empresa."));
        medico.setNombre(datos.getNombre());
        medico.setApellido(datos.getApellido());
        medico.setCedula(datos.getCedula());
        medico.setTelefono(datos.getTelefono());
        medico.setEmail(datos.getEmail());
        medico.setExequatur(datos.getExequatur());
        medico.setEspecialidades(datos.getEspecialidades());
        medico.setSucursal(datos.getSucursal());
        validarMedico(empresaId, medico);
        return medicoRepository.save(medico);
    }

    @Transactional
    public void cambiarEstado(UUID empresaId, UUID medicoId, boolean activo) {
        Medico medico = medicoRepository.findByIdAndEmpresaId(medicoId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("M\u00e9dico no encontrado para esta empresa."));
        medico.setActivo(activo);
        medicoRepository.save(medico);
    }

    private void crearUsuario(UUID empresaId, Medico medico, String nombreUsuario, String contrasena,
                               String confirmacionContrasena, String nombreRol, boolean usuarioActivo) {
        if (esVacio(nombreUsuario) || esVacio(contrasena)) {
            throw new IllegalArgumentException("Completa el usuario y la contrase\u00f1a.");
        }
        if (!contrasena.equals(confirmacionContrasena)) {
            throw new IllegalArgumentException("La confirmaci\u00f3n de contrase\u00f1a no coincide.");
        }
        if (usuarioRepository.existsByEmpresaIdAndUsuario(empresaId, nombreUsuario.trim())) {
            throw new IllegalArgumentException("Ese usuario ya existe para esta empresa.");
        }
        Rol rol = rolRepository.findByEmpresaIdAndNombre(empresaId, nombreRol == null ? "MEDICO" : nombreRol)
                .orElseThrow(() -> new IllegalArgumentException("El rol seleccionado no est\u00e1 disponible."));
        Usuario usuario = new Usuario();
        usuario.setEmpresaId(empresaId);
        usuario.setUsuario(nombreUsuario.trim());
        usuario.setPasswordHash(passwordEncoder.encode(contrasena));
        usuario.setNombre(medico.getNombre());
        usuario.setApellido(medico.getApellido());
        usuario.setEmail(medico.getEmail());
        usuario.setTelefono(medico.getTelefono());
        usuario.setActivo(usuarioActivo);
        usuario.setRoles(new HashSet<>(java.util.Set.of(rol)));
        usuarioRepository.save(usuario);
        medico.setUsuarioId(usuario.getId());
    }

    private void validarMedico(UUID empresaId, Medico medico) {
        if (empresaId == null || medico == null || esVacio(medico.getNombre()) || esVacio(medico.getApellido())
                || esVacio(medico.getCedula()) || esVacio(medico.getCodigo()) || esVacio(medico.getExequatur())
                || medico.getSucursal() == null || medico.getEspecialidades() == null || medico.getEspecialidades().isEmpty()) {
            throw new IllegalArgumentException("Completa los datos obligatorios del m\u00e9dico.");
        }
        if (!medico.getCedula().matches("\\d{3}-\\d{7}-\\d")) {
            throw new IllegalArgumentException("La c\u00e9dula debe tener el formato 000-0000000-0.");
        }
        if (!esVacio(medico.getTelefono()) && !medico.getTelefono().matches("\\(\\d{3}\\) \\d{3}-\\d{4}")) {
            throw new IllegalArgumentException("El tel\u00e9fono debe tener el formato (000) 000-0000.");
        }
        if (!esVacio(medico.getEmail()) && !medico.getEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Ingresa un correo electr\u00f3nico v\u00e1lido.");
        }
        if (!empresaId.equals(medico.getSucursal().getEmpresaId())
                || medico.getEspecialidades().stream().anyMatch(especialidad -> !empresaId.equals(especialidad.getEmpresaId()))) {
            throw new IllegalArgumentException("La sucursal y las especialidades deben pertenecer a esta empresa.");
        }
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.isBlank();
    }

    private String generarCodigo(UUID empresaId) {
        int siguiente = listarPorEmpresa(empresaId).stream()
                .map(Medico::getCodigo)
                .filter(codigo -> codigo != null && CODIGO_MEDICO.matcher(codigo).matches())
                .mapToInt(codigo -> Integer.parseInt(codigo.substring(3)))
                .max()
                .orElse(0) + 1;
        return "MED" + String.format("%03d", siguiente);
    }
}
