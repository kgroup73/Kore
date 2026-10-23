package com.kore.model;

import java.util.List;

/**
 * Telemetría de uso y puntos para el gráfico sparkline (PowerBI).
 */
public record TelemetryData(
        String periodLabel,
        int accessCount,
        List<Double> sparklinePoints,
        String reportUrl
) {}
