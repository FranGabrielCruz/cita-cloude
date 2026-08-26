package com.citacloud.app.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification_recipients", uniqueConstraints = @UniqueConstraint(columnNames = {"notification_id", "user_id"}))
public class NotificacionDestinatario {
    @Id @GeneratedValue private UUID id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "notification_id", nullable = false)
    private Notificacion notificacion;
    @Column(name = "user_id", nullable = false) private UUID usuarioId;
    @Column(name = "leida", nullable = false) private boolean leida;
    @Column(name = "leida_en") private LocalDateTime leidaEn;
    @Column(name = "creada_en", nullable = false) private LocalDateTime creadaEn = LocalDateTime.now();

    public UUID getId() { return id; }
    public Notificacion getNotificacion() { return notificacion; }
    public void setNotificacion(Notificacion notificacion) { this.notificacion = notificacion; }
    public UUID getUsuarioId() { return usuarioId; }
    public void setUsuarioId(UUID usuarioId) { this.usuarioId = usuarioId; }
    public boolean isLeida() { return leida; }
    public void setLeida(boolean leida) { this.leida = leida; }
    public LocalDateTime getLeidaEn() { return leidaEn; }
    public void setLeidaEn(LocalDateTime leidaEn) { this.leidaEn = leidaEn; }
}
