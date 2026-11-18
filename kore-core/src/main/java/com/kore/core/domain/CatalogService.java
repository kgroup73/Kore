package com.kore.core.domain;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CatalogService {

  private static final boolean IN_MEMORY = true;
  private Map<ResourceId, ResourceEntry> resources = new HashMap<>();

  public List<ResourceEntry> index() {
    return resources.values().stream().toList();
  }

  public ResourceId store(UUID organizationId, String name, ResourceType type) {
    if (IN_MEMORY) {
      ResourceId resourceId = ResourceId.generate();
      ResourceEntry resource = new ResourceEntry(resourceId, organizationId, name, type, Instant.now());
      resources.put(resourceId, resource);
      return resourceId;
    } else {
      throw new RuntimeException("Only in memory service avaliable");
    }
  }

}
