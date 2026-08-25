package com.citacloud.app.services;

import java.util.Set;
import java.util.UUID;

public record NotificacionesCreadasEvent(UUID empresaId, Set<UUID> usuariosDestinatarios) {}
