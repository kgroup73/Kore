package com.kore.model;

/**
 * Entidades OCR extraídas por Solr y Tesseract.
 */
public record OcrEntities(
        String precision,
        String nitIdentificado,
        String valorCuantia,
        String periodoVigencia,
        String jurisdiccion
) {}
