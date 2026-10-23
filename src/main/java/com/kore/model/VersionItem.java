package com.kore.model;

/**
 * Representa una versión histórica de un documento para la pestaña de Versiones.
 */
public record VersionItem(
        String version,
        String timestamp,
        String author,
        String sizeFormatted,
        String comment,
        boolean current
) {}
