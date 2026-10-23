package com.kore.model;

/**
 * Estado de scripts y webhooks en la sección de Automatizaciones & Integraciones.
 */
public record AutomationItem(
        String name,
        AutomationType type,
        String status,
        boolean success
) {
    public enum AutomationType {
        SCRIPT,
        WEBHOOK
    }
}
