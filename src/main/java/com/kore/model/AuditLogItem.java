package com.kore.model;

/**
 * Registro de auditoría forense para la pestaña de Auditoría (142).
 */
public record AuditLogItem(
        String timestamp,
        String eventType,
        String actor,
        String ipAddress,
        String details
) {}
