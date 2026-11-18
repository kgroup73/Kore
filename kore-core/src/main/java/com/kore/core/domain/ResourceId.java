package com.kore.core.domain;

import java.util.UUID;

public record ResourceId(UUID value) {

  public ResourceId {
    if (value == null) {
      throw new IllegalArgumentException("Resource ID cannot be null");
    }
  }

  public static ResourceId generate() {
    return new ResourceId(UUID.randomUUID());
  }
}
