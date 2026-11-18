package com.kore.core;

import java.util.UUID;

import com.kore.core.domain.CatalogService;
import com.kore.core.domain.ResourceType;

public class KoreCore {
  public static void main(String args[]) {
    CatalogService service = new CatalogService();
    service.index();
    service.store(UUID.randomUUID(), "Ventas 2026", ResourceType.DOCUMENT);
    service.index();

    return;
  }
}
