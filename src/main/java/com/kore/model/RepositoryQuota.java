package com.kore.model;

/**
 * Cuota de almacenamiento del repositorio en la Columna 2.
 */
public record RepositoryQuota(
        double percentage,
        int usedGb,
        int allocatedGb
) {
    public String getPercentageFormatted() {
        return String.format("%.1f%%", percentage);
    }

    public String getUsageLabel() {
        return String.format("%,d GB Usado", usedGb);
    }

    public String getAllocatedLabel() {
        return String.format("%,d GB Asignado", allocatedGb);
    }
}
