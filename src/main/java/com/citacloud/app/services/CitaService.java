package com.citacloud.app.services;

import com.citacloud.app.models.Cita;
import com.citacloud.app.repositories.CitaRepository;
import com.citacloud.app.repositories.MedicoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CitaService {

    private final CitaRepository citaRepository;
    private final DisponibilidadService disponibilidadService;
    private final MedicoRepository medicoRepository;
    private final ConfiguracionFase2Service configuracionFase2Service;
    private final NotificacionService notificaciones;
    private final AuditoriaService auditoria;

    public CitaService(CitaRepository citaRepository, DisponibilidadService disponibilidadService,
                       MedicoRepository medicoRepository, ConfiguracionFase2Service configuracionFase2Service, NotificacionService notificaciones, AuditoriaService auditoria) {
        this.citaRepository = citaRepository;
        this.disponibilidadService = disponibilidadService;
        this.medicoRepository = medicoRepository;
        this.configuracionFase2Service = configuracionFase2Service;
        this.notificaciones = notificaciones;
        this.auditoria = auditoria;
    }

    public List<Cita> listarPorEmpresa(UUID empresaId) {
        return citaRepository.findByEmpresaIdOrderByCreadoEnDesc(empresaId);
    }

    public List<Cita> listarPorFecha(UUID empresaId, LocalDate fecha) {
        return citaRepository.findByEmpresaIdAndFechaOrderByCreadoEnDesc(empresaId, fecha);
    }

    public Optional<java.time.LocalTime> obtenerHoraFinSugerida(UUID empresaId, UUID medicoId, LocalDate fecha, java.time.LocalTime inicio) {
        if (empresaId == null || medicoId == null || fecha == null || inicio == null) return Optional.empty();
        return disponibilidadService.obtenerHoraFinSugerida(empresaId, medicoId, fecha, inicio);
    }
    public List<java.time.LocalTime> obtenerSlotsDisponibles(UUID empresaId, UUID medicoId, LocalDate fecha, UUID citaExcluida) { return disponibilidadService.obtenerSlotsDisponibles(empresaId, medicoId, fecha, citaExcluida); }

    public Cita guardar(Cita cita) {
        return citaRepository.save(cita);
    }

    /**
     * Registra una cita verificando que todos los datos pertenecen a la empresa
     * autenticada y que el médico no tenga otro turno en el mismo intervalo.
     */
    public Cita registrar(UUID empresaId, Cita cita) {
        if (cita == null || cita.getFecha() == null || cita.getHoraInicio() == null) {
            throw new IllegalArgumentException("Completa la fecha y hora de la cita.");
        }
        java.time.LocalDate hoy = java.time.LocalDate.now();
        if (cita.getFecha().isBefore(hoy) || (cita.getFecha().equals(hoy) && !cita.getHoraInicio().isAfter(java.time.LocalTime.now()))) {
            throw new IllegalArgumentException("No se puede crear una cita en una hora que ya pasó.");
        }
        validarDatos(empresaId, cita);
        validarDisponibilidad(empresaId, cita, null);
        if (citaRepository.existsByEmpresaIdAndMedicoIdAndFechaAndHoraInicioLessThanAndHoraFinGreaterThan(
                empresaId, cita.getMedico().getId(), cita.getFecha(), cita.getHoraFin(), cita.getHoraInicio())) {
            throw new IllegalArgumentException("El médico ya tiene una cita en ese horario.");
        }

        cita.setEmpresaId(empresaId);
        boolean requiereAprobacion = configuracionFase2Service.obtener(empresaId).isRequiereAprobacionCitas();
        cita.setEstado(requiereAprobacion ? "PENDIENTE" : "CONFIRMADA");
        Cita guardada = citaRepository.save(cita);
        auditoria.registrar(empresaId,null,"CITAS","APPOINTMENT_CREATED","CITA",guardada.getId(),"CIT-"+guardada.getId().toString().substring(0,8),guardada.getPaciente().getId(),List.of(),"SUCCESS",null,false);
        if (requiereAprobacion) {
            notificaciones.crearParaCita(empresaId, "CITA_PENDIENTE", "CITAS",
                    "Cita pendiente de aprobación", guardada.getPaciente().getNombreCompleto()
                            + " solicitó una cita.", guardada, null);
        } else {
            notificaciones.crearParaCita(empresaId, "CITA_CONFIRMADA", "CITAS",
                    "Nueva cita confirmada", guardada.getPaciente().getNombreCompleto()
                            + " tiene una cita confirmada para el " + guardada.getFecha() + ".",
                    guardada, null);
        }
        return guardada;
    }

    public Cita actualizar(UUID empresaId, Cita cita) {
        if (cita == null || cita.getId() == null || !citaRepository.existsById(cita.getId())) {
            throw new IllegalArgumentException("La cita no existe.");
        }
        validarDatos(empresaId, cita);
        validarDisponibilidad(empresaId, cita, cita.getId());
        boolean tieneConflicto = citaRepository.findByEmpresaId(empresaId).stream()
                .filter(existente -> !existente.getId().equals(cita.getId()))
                .anyMatch(existente -> existente.getMedico().getId().equals(cita.getMedico().getId())
                        && existente.getFecha().equals(cita.getFecha())
                        && existente.getHoraInicio().isBefore(cita.getHoraFin())
                        && existente.getHoraFin().isAfter(cita.getHoraInicio()));
        if (tieneConflicto) {
            throw new IllegalArgumentException("El médico ya tiene una cita en ese horario.");
        }
        Cita anterior = citaRepository.findById(cita.getId()).orElseThrow();
        UUID medicoAnteriorId = anterior.getMedico() == null ? null : anterior.getMedico().getId();
        Cita guardada=citaRepository.save(cita);
        auditoria.registrar(empresaId,null,"CITAS","APPOINTMENT_RESCHEDULED","CITA",guardada.getId(),"CIT-"+guardada.getId().toString().substring(0,8),guardada.getPaciente().getId(),List.of(),"SUCCESS",null,false);
        boolean cambioMedico = !java.util.Objects.equals(medicoAnteriorId, guardada.getMedico().getId());
        notificaciones.crearParaCita(empresaId, cambioMedico ? "CITA_REASIGNADA" : "CITA_REPROGRAMADA", "CITAS",
                cambioMedico ? "Cita reasignada" : "Cita reprogramada",
                guardada.getPaciente().getNombreCompleto() + " · " + guardada.getFecha() + " " + guardada.getHoraInicio(),
                guardada, cambioMedico ? medicoAnteriorId : null);
        return guardada;
    }

    private void validarDisponibilidad(UUID empresaId, Cita cita, UUID citaExcluida) {
        if (!disponibilidadService.estaDisponible(empresaId, cita.getMedico().getId(), cita.getFecha(),
                cita.getHoraInicio(), cita.getHoraFin(), citaExcluida)) {
            throw new IllegalArgumentException("No se puede crear la cita: el horario no est\u00e1 disponible por horario laboral, descanso, ausencia o una cita existente.");
        }
    }

    private void validarDatos(UUID empresaId, Cita cita) {
        if (empresaId == null || cita == null || cita.getPaciente() == null || cita.getMedico() == null
                || cita.getSucursal() == null || cita.getFecha() == null || cita.getHoraInicio() == null
                || cita.getHoraFin() == null) {
            throw new IllegalArgumentException("Completa los datos obligatorios de la cita.");
        }
        if (!empresaId.equals(cita.getPaciente().getEmpresaId())
                || !empresaId.equals(cita.getMedico().getEmpresaId())
                || !empresaId.equals(cita.getSucursal().getEmpresaId())
                || (cita.getConsultorio() != null && !empresaId.equals(cita.getConsultorio().getEmpresaId()))) {
            throw new IllegalArgumentException("No puedes asociar datos de otra empresa.");
        }
        if (!cita.getHoraFin().isAfter(cita.getHoraInicio())) {
            throw new IllegalArgumentException("La hora de finalización debe ser posterior a la hora de inicio.");
        }
    }

    public void cambiarEstado(UUID citaId, String nuevoEstado) {
        Optional<Cita> citaOpt = citaRepository.findById(citaId);
        if (citaOpt.isPresent()) {
            Cita cita = citaOpt.get();
            cita.setEstado(nuevoEstado);
            citaRepository.save(cita);
        }
    }

    public void cancelar(UUID empresaId, UUID citaId) {
        Cita cita = citaRepository.findById(citaId)
                .filter(encontrada -> empresaId.equals(encontrada.getEmpresaId()))
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada para esta empresa."));
        if ("CANCELADA".equals(cita.getEstado())) {
            throw new IllegalArgumentException("La cita ya está cancelada.");
        }
        cita.setEstado("CANCELADA");
        citaRepository.save(cita);
        auditoria.registrar(empresaId,null,"CITAS","APPOINTMENT_CANCELLED","CITA",cita.getId(),"CIT-"+cita.getId().toString().substring(0,8),cita.getPaciente().getId(),List.of(new AuditoriaService.Cambio("estado","ACTIVA","CANCELADA")),"SUCCESS",null,false);
        notificaciones.crearParaCita(empresaId, "CITA_CANCELADA", "CITAS", "Cita cancelada",
                cita.getPaciente().getNombreCompleto() + " · " + cita.getFecha(), cita, null);
    }

    /** Aprueba una solicitud volviendo a validar disponibilidad antes de confirmarla. */
    public void aprobar(UUID empresaId, UUID citaId) {
        Cita cita = citaRepository.findById(citaId)
                .filter(encontrada -> empresaId.equals(encontrada.getEmpresaId()))
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada para esta empresa."));
        if (!"PENDIENTE".equals(cita.getEstado())) {
            throw new IllegalArgumentException("Solo se pueden aprobar citas pendientes.");
        }
        validarDisponibilidad(empresaId, cita, cita.getId());
        cita.setEstado("CONFIRMADA");
        citaRepository.save(cita);
        auditoria.registrar(empresaId,null,"CITAS","APPOINTMENT_APPROVED","CITA",cita.getId(),"CIT-"+cita.getId().toString().substring(0,8),cita.getPaciente().getId(),List.of(new AuditoriaService.Cambio("estado","PENDIENTE","CONFIRMADA")),"SUCCESS",null,false);
        notificaciones.crearParaCita(empresaId, "CITA_APROBADA", "CITAS", "Cita aprobada",
                cita.getPaciente().getNombreCompleto() + " · " + cita.getFecha(), cita, null);
    }

    /** Cambia el estado cl\u00ednico de una cita con control de responsabilidades. */
    public void cambiarEstadoClinico(UUID empresaId, UUID citaId, UUID usuarioId, boolean esAdministrador,
                                     boolean esMedico, boolean esSecretaria, String nuevoEstado) {
        Cita cita = citaRepository.findById(citaId)
                .filter(encontrada -> empresaId.equals(encontrada.getEmpresaId()))
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada para esta empresa."));
        if ("CANCELADA".equals(cita.getEstado()) || "ATENDIDA".equals(cita.getEstado()) || "NO_ASISTIO".equals(cita.getEstado())) {
            throw new IllegalArgumentException("No se puede cambiar el estado de una cita cancelada, atendida o marcada como no asistió.");
        }
        if ("EN_ESPERA".equals(nuevoEstado)) {
            if (!esAdministrador && !esSecretaria) throw new IllegalArgumentException("Solo la secretaria puede marcar la cita en espera.");
        } else if ("NO_ASISTIO".equals(nuevoEstado)) {
            if (!esAdministrador && !esSecretaria) throw new IllegalArgumentException("Solo la secretaria puede marcar la cita como no asistió.");
        } else if ("EN_CONSULTA".equals(nuevoEstado) || "ATENDIDA".equals(nuevoEstado)) {
            boolean esMedicoDeLaCita = esMedico && medicoRepository.findByEmpresaId(empresaId).stream()
                    .anyMatch(medico -> usuarioId.equals(medico.getUsuarioId()) && medico.getId().equals(cita.getMedico().getId()));
            if (!esAdministrador && !esMedicoDeLaCita) throw new IllegalArgumentException("Solo el m\u00e9dico asignado puede cambiar este estado.");
            if ("EN_CONSULTA".equals(nuevoEstado) && !"CONFIRMADA".equals(cita.getEstado()) && !"EN_ESPERA".equals(cita.getEstado()) && !esAdministrador) {
                throw new IllegalArgumentException("La cita debe estar confirmada o en espera antes de iniciar la consulta.");
            }
            if ("ATENDIDA".equals(nuevoEstado) && !"EN_CONSULTA".equals(cita.getEstado()) && !esAdministrador) {
                throw new IllegalArgumentException("La cita debe estar en consulta antes de marcarla como atendida.");
            }
        } else {
            throw new IllegalArgumentException("Estado de cita no permitido.");
        }
        cita.setEstado(nuevoEstado);
        citaRepository.save(cita);
    }
}
