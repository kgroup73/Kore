package com.kore.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa un nodo en la taxonomía documental con contador de expedientes (Columna 2).
 * Incluye UUID de taxonomía y clave de relación con expedientes.
 */
public class TaxonomyNode {
    private final String name;
    private final String path;
    private final String uuid;
    private final String categoryKey;
    private final Integer countBadge; // Puede ser null si no tiene contador numérico
    private final List<TaxonomyNode> children = new ArrayList<>();

    public TaxonomyNode(String name, String path, String uuid, String categoryKey, Integer countBadge) {
        this.name = name;
        this.path = path;
        this.uuid = uuid;
        this.categoryKey = categoryKey;
        this.countBadge = countBadge;
    }

    public TaxonomyNode(String name, String path, String uuid, String categoryKey) {
        this(name, path, uuid, categoryKey, null);
    }

    public String getName() { return name; }
    public String getPath() { return path; }
    public String getUuid() { return uuid; }
    public String getCategoryKey() { return categoryKey; }
    public Integer getCountBadge() { return countBadge; }
    public boolean hasBadge() { return countBadge != null && countBadge > 0; }
    public List<TaxonomyNode> getChildren() { return children; }

    public void addChild(TaxonomyNode child) {
        this.children.add(child);
    }

    @Override
    public String toString() {
        return name;
    }
}
