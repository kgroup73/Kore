package com.kore.model;

/**
 * Contexto Jurídico mostrado en la Columna 2.
 */
public record LegalContext(
        String code,
        String department,
        String responsible,
        String location
) {}
