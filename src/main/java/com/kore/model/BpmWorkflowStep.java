package com.kore.model;

/**
 * Etapa de flujo BPM para la pestaña de BPM.
 */
public record BpmWorkflowStep(
        int stepNumber,
        String title,
        String assignee,
        String status,
        StepState state
) {
    public enum StepState {
        COMPLETED,
        IN_PROGRESS,
        PENDING
    }
}
