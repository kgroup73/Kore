package com.kore.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa un nodo en el árbol de repositorio (Columna 1).
 * Contiene metadatos de ruta jerárquica, UUID único y clave de filtrado de documentos.
 */
public class RepositoryNode {
    private final String name;
    private final String path;
    private final String uuid;
    private final String categoryKey;
    private final boolean folder;
    private final List<RepositoryNode> children = new ArrayList<>();

    public RepositoryNode(String name, String path, String uuid, String categoryKey, boolean folder) {
        this.name = name;
        this.path = path;
        this.uuid = uuid;
        this.categoryKey = categoryKey;
        this.folder = folder;
    }

    public String getName() { return name; }
    public String getPath() { return path; }
    public String getUuid() { return uuid; }
    public String getCategoryKey() { return categoryKey; }
    public boolean isFolder() { return folder; }
    public List<RepositoryNode> getChildren() { return children; }

    public void addChild(RepositoryNode child) {
        this.children.add(child);
    }

    @Override
    public String toString() {
        return name;
    }
}
