package com.kore.core.domain;

import java.time.Instant;
import java.util.UUID;

public record ResourceEntry(ResourceId id, UUID organizationId, String name, ResourceType type, Instant createdAt) {

  public ResourceEntry {
    if (id == null || organizationId == null) {
      throw new IllegalArgumentException("Resource need identity");
    }

    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Resource name not be blank");
    }
  }
}
