package com.kore.model;

/**
 * Modelo para la tarjeta de usuario activo en la Columna 1.
 */
public record UserProfile(
        String username,
        String role,
        String avatarInitials
) {}
