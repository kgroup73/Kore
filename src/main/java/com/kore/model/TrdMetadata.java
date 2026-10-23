package com.kore.model;

/**
 * Tabla de Retención Documental (TRD) Metadata.
 */
public record TrdMetadata(
        String codigoTrd,
        String tiempoRetencion,
        String disposicionFinal,
        String confidencialidad
) {}
