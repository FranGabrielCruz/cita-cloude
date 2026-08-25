package com.citacloud.app.services;

import com.citacloud.app.models.CheckIn;
import com.citacloud.app.models.Cita;
import com.citacloud.app.repositories.CheckInRepository;
import com.citacloud.app.repositories.CitaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CheckInService {
    private final CheckInRepository checks;
    private final CitaRepository citas;
    private final NotificacionService notificaciones;

    public CheckInService(CheckInRepository checks, CitaRepository citas, NotificacionService notificaciones) {
        this.checks = checks;
        this.citas = citas;
        this.notificaciones = notificaciones;
    }

    public List<CheckIn> listar(UUID empresa) {
        return checks.findByEmpresaIdOrderByFechaHoraLlegadaDesc(empresa);
    }

    @Transactional
    public CheckIn registrar(UUID empresa, UUID usuario, UUID citaId) {
        Cita cita = citas.findById(citaId).filter(c -> empresa.equals(c.getEmpresaId()))
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada para esta empresa."));
        if (!"CONFIRMADA".equals(cita.getEstado())) {
            throw new IllegalArgumentException("Solo se puede registrar llegada de citas confirmadas.");
        }
        if (checks.existsByEmpresaIdAndCitaId(empresa, citaId)) {
            throw new IllegalArgumentException("La llegada ya fue registrada.");
        }
        CheckIn check = new CheckIn();
        check.setEmpresaId(empresa);
        check.setUsuarioId(usuario);
        check.setCita(cita);
        check.setPacienteId(cita.getPaciente().getId());
        cita.setEstado("EN_ESPERA");
        citas.save(cita);
        CheckIn guardado = checks.save(check);
        notificaciones.crearParaCita(empresa, "PACIENTE_EN_ESPERA", "CITAS", "Paciente en sala de espera",
                cita.getPaciente().getNombreCompleto() + " realizó check-in.", cita, null);
        return guardado;
    }
}
