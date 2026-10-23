package com.kore.state;

import com.kore.model.AuditLogItem;
import com.kore.model.BpmWorkflowStep;
import com.kore.model.DocumentNode;
import com.kore.model.LegalContext;
import com.kore.model.RepositoryNode;
import com.kore.model.RepositoryQuota;
import com.kore.model.TaxonomyNode;
import com.kore.model.UserProfile;
import com.kore.model.VersionItem;
import com.kore.service.LocalFileSystemService;
import com.kore.service.MockDataService;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

/**
 * ViewModel central reactivo para Kore Enterprise DMS.
 * Gestiona el estado de selección, filtros, ordenamiento, documentos activos y sincronización bidireccional
 * entre el árbol de repositorio (Col 1), taxonomía (Col 2), lista central (Col 3),
 * inspector (Col 4) y las barras de estado superior e inferior.
 */
@Component
public class DocumentViewModel {

    private final MockDataService mockDataService;
    private final ObservableList<DocumentNode> allDocuments = FXCollections.observableArrayList();
    private final FilteredList<DocumentNode> filteredDocuments;
    private final SortedList<DocumentNode> sortedDocuments;
    private final ObjectProperty<DocumentNode> selectedDocument = new SimpleObjectProperty<>();

    // Filtro de categoría activo (por defecto "contratos_2025")
    private final StringProperty activeCategoryFilter = new SimpleStringProperty("contratos_2025");

    // Estadísticas reactivas de selección
    private final IntegerProperty selectedCount = new SimpleIntegerProperty(1);
    private final LongProperty totalBufferSizeBytes = new SimpleLongProperty(4404019L);
    private final StringProperty totalBufferFormatted = new SimpleStringProperty("4.2 MB");

    // Metadatos globales y breadcrumbs
    private final StringProperty breadcrumbPath = new SimpleStringProperty("okm:root > Finanzas Corporativas > Contratos y Proveedores 2025");
    private final StringProperty breadcrumbUuid = new SimpleStringProperty("okm:node-7b89-4fa2-91cd");
    private final StringProperty jvmHeapStatus = new SimpleStringProperty("JVM Heap: 482MB / 2048MB");

    // Estados de conexión e índices
    private final StringProperty systemStatusMessage = new SimpleStringProperty("Listo - Nodo okm:node-7b89-4fa2-91cd cargado");
    private final StringProperty solrStatus = new SimpleStringProperty("Solr: Sincronizado");
    private final StringProperty ocrDaemonStatus = new SimpleStringProperty("Daemon OCR: En Línea");
    private final StringProperty solrIndexBadge = new SimpleStringProperty("Índice Solr 8.11 OK");
    private final StringProperty roleAccessBadge = new SimpleStringProperty("Paralegal Access");

    // Escala de Zoom Reactiva (50% a 150%)
    private final javafx.beans.property.DoubleProperty zoomLevel = new javafx.beans.property.SimpleDoubleProperty(100.0);

    // Modo Oscuro reactivo (Primer Light vs Primer Dark)
    private final BooleanProperty darkMode = new SimpleBooleanProperty(false);

    // Servicio de Sistema de Archivos Local
    private final LocalFileSystemService localFileSystemService;
    private final BooleanProperty localFsMode = new SimpleBooleanProperty(false);
    private final ObjectProperty<Path> currentLocalDirectory = new SimpleObjectProperty<>();
    private final ObjectProperty<RepositoryQuota> repositoryQuotaProperty = new SimpleObjectProperty<>();

    // Metadatos contextuales fijos
    private final UserProfile currentUser;
    private final LegalContext legalContext;
    private final RepositoryQuota repositoryQuota;
    private final RepositoryNode repositoryTreeRoot;
    private final TaxonomyNode taxonomyTreeRoot;

    // Nodos actualmente seleccionados en los árboles
    private final ObjectProperty<RepositoryNode> selectedRepositoryNode = new SimpleObjectProperty<>();
    private final ObjectProperty<TaxonomyNode> selectedTaxonomyNode = new SimpleObjectProperty<>();

    // Listas observables para las pestañas del inspector (Versiones, Auditoría, BPM)
    private final ObservableList<VersionItem> currentVersions = FXCollections.observableArrayList();
    private final ObservableList<AuditLogItem> currentAuditLogs = FXCollections.observableArrayList();
    private final ObservableList<BpmWorkflowStep> currentBpmSteps = FXCollections.observableArrayList();

    public DocumentViewModel(MockDataService mockDataService, LocalFileSystemService localFileSystemService) {
        this.mockDataService = mockDataService;
        this.localFileSystemService = localFileSystemService;
        this.currentUser = mockDataService.createUserProfile();
        this.legalContext = mockDataService.createLegalContext();
        this.repositoryQuota = mockDataService.createRepositoryQuota();
        this.repositoryQuotaProperty.set(this.repositoryQuota);
        this.repositoryTreeRoot = mockDataService.createRepositoryTree();
        this.taxonomyTreeRoot = mockDataService.createTaxonomyTree();

        List<DocumentNode> docs = mockDataService.createMockDocuments();
        this.allDocuments.addAll(docs);

        // Lista filtrada reactiva
        this.filteredDocuments = new FilteredList<>(this.allDocuments, doc -> true);

        // Lista ordenada reactiva
        this.sortedDocuments = new SortedList<>(this.filteredDocuments);

        // Al inicio, ningún documento está seleccionado (el panel lateral se ocultará hasta que el usuario elija uno)
        this.selectedDocument.set(null);
        this.breadcrumbUuid.set("okm:root");
        this.systemStatusMessage.set("Listo - Repositorio conectado");

        // Listener reactivo al cambiar el documento seleccionado
        this.selectedDocument.addListener((obs, oldDoc, newDoc) -> {
            if (newDoc != null) {
                this.systemStatusMessage.set("Listo - Nodo " + newDoc.getUuid() + " cargado");
                this.breadcrumbUuid.set(newDoc.getUuid());
                loadInspectorTabData(newDoc);
            } else {
                this.systemStatusMessage.set("Listo - Repositorio conectado");
                this.breadcrumbUuid.set("okm:root");
                this.currentVersions.clear();
                this.currentAuditLogs.clear();
                this.currentBpmSteps.clear();
            }
        });

        // Escuchar cambios en el filtro de categoría
        this.activeCategoryFilter.addListener((obs, oldCat, newCat) -> applyCategoryFilter(newCat));

        // Escuchar cambios de checkbox en cada documento
        for (DocumentNode doc : docs) {
            doc.selectedProperty().addListener((obs, oldVal, newVal) -> recalculateSelectionSummary());
        }
        recalculateSelectionSummary();
    }

    private void loadInspectorTabData(DocumentNode doc) {
        if (doc == null) return;
        currentVersions.setAll(mockDataService.createMockVersions(doc.getUuid()));
        currentAuditLogs.setAll(mockDataService.createMockAuditLogs(doc.getUuid()));
        currentBpmSteps.setAll(mockDataService.createMockBpmSteps(doc.getUuid()));
    }

    private void applyCategoryFilter(String category) {
        filteredDocuments.setPredicate(doc -> {
            if (category == null || category.isBlank() || "all".equalsIgnoreCase(category)) {
                return true;
            }
            return category.equalsIgnoreCase(doc.getCategoryKey());
        });

        if (selectedDocument.get() != null && !filteredDocuments.contains(selectedDocument.get())) {
            selectedDocument.set(null);
        }
        recalculateSelectionSummary();
    }

    public void selectRepositoryNode(RepositoryNode node) {
        if (node == null) return;
        this.selectedRepositoryNode.set(node);
        this.breadcrumbPath.set(node.getPath());
        this.breadcrumbUuid.set(node.getUuid());
        this.systemStatusMessage.set("Listo - Nodo " + node.getUuid() + " cargado");
        this.activeCategoryFilter.set(node.getCategoryKey());
    }

    public void selectTaxonomyNode(TaxonomyNode node) {
        if (node == null) return;
        this.selectedTaxonomyNode.set(node);
        this.breadcrumbPath.set(node.getPath());
        this.breadcrumbUuid.set(node.getUuid());
        this.systemStatusMessage.set("Listo - Nodo " + node.getUuid() + " cargado");
        this.activeCategoryFilter.set(node.getCategoryKey());
    }

    public void selectDocument(DocumentNode doc) {
        this.selectedDocument.set(doc);
    }

    public void toggleSelection(DocumentNode doc) {
        if (doc != null) {
            doc.setSelected(!doc.isSelected());
            recalculateSelectionSummary();
        }
    }

    public void selectAll(boolean select) {
        for (DocumentNode doc : filteredDocuments) {
            doc.setSelected(select);
        }
        recalculateSelectionSummary();
    }

    public void recalculateSelectionSummary() {
        int count = 0;
        long bytes = 0;
        for (DocumentNode doc : filteredDocuments) {
            if (doc.isSelected()) {
                count++;
                bytes += doc.getSizeBytes();
            }
        }
        this.selectedCount.set(count);
        this.totalBufferSizeBytes.set(bytes);
        this.totalBufferFormatted.set(formatBytes(bytes));
    }

    public void toggleCheckInStatus() {
        DocumentNode doc = selectedDocument.get();
        if (doc == null) return;

        if ("Bloqueado".equalsIgnoreCase(doc.getStatus())) {
            doc.setStatus("Disponible");
            doc.nextVersionActionProperty().set("Check-out (v2.5)");
            systemStatusMessage.set("Check-in completado para " + doc.getName() + " - Versión liberada");
        } else {
            doc.setStatus("Bloqueado");
            doc.nextVersionActionProperty().set("Check-in (v2.5)");
            systemStatusMessage.set("Check-out activado: Nodo bloqueado para edición por " + currentUser.username());
        }
    }

    public void resetZoom() {
        this.zoomLevel.set(100.0);
    }

    public void zoomIn() {
        double current = this.zoomLevel.get();
        if (current < 150.0) {
            this.zoomLevel.set(Math.min(150.0, current + 10.0));
        }
    }

    public void zoomOut() {
        double current = this.zoomLevel.get();
        if (current > 50.0) {
            this.zoomLevel.set(Math.max(50.0, current - 10.0));
        }
    }

    public javafx.beans.property.DoubleProperty zoomLevelProperty() {
        return zoomLevel;
    }

    public double getZoomLevel() {
        return zoomLevel.get();
    }

    public void setZoomLevel(double zoomLevel) {
        this.zoomLevel.set(zoomLevel);
    }

    // Modo Oscuro
    public BooleanProperty darkModeProperty() {
        return darkMode;
    }

    public boolean isDarkMode() {
        return darkMode.get();
    }

    public void setDarkMode(boolean dark) {
        this.darkMode.set(dark);
    }

    public void toggleDarkMode() {
        this.darkMode.set(!this.darkMode.get());
    }

    // Ordenamiento reactivo
    public void sortByDateDesc() {
        sortedDocuments.setComparator(Comparator.comparing(DocumentNode::getUuid).reversed());
    }

    public void sortByNameAsc() {
        sortedDocuments.setComparator(Comparator.comparing(DocumentNode::getName));
    }

    public void sortBySizeDesc() {
        sortedDocuments.setComparator(Comparator.comparingLong(DocumentNode::getSizeBytes).reversed());
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    // Getters para vistas y data-binding
    public ObservableList<DocumentNode> getAllDocuments() { return allDocuments; }
    public FilteredList<DocumentNode> getFilteredDocuments() { return filteredDocuments; }
    public SortedList<DocumentNode> getSortedDocuments() { return sortedDocuments; }

    public ObjectProperty<DocumentNode> selectedDocumentProperty() { return selectedDocument; }
    public DocumentNode getSelectedDocument() { return selectedDocument.get(); }

    public ObjectProperty<RepositoryNode> selectedRepositoryNodeProperty() { return selectedRepositoryNode; }
    public ObjectProperty<TaxonomyNode> selectedTaxonomyNodeProperty() { return selectedTaxonomyNode; }

    public ObservableList<VersionItem> getCurrentVersions() { return currentVersions; }
    public ObservableList<AuditLogItem> getCurrentAuditLogs() { return currentAuditLogs; }
    public ObservableList<BpmWorkflowStep> getCurrentBpmSteps() { return currentBpmSteps; }

    public StringProperty activeCategoryFilterProperty() { return activeCategoryFilter; }

    public IntegerProperty selectedCountProperty() { return selectedCount; }
    public LongProperty totalBufferSizeBytesProperty() { return totalBufferSizeBytes; }
    public StringProperty totalBufferFormattedProperty() { return totalBufferFormatted; }

    public StringProperty breadcrumbPathProperty() { return breadcrumbPath; }
    public StringProperty breadcrumbUuidProperty() { return breadcrumbUuid; }
    public StringProperty jvmHeapStatusProperty() { return jvmHeapStatus; }

    public StringProperty systemStatusMessageProperty() { return systemStatusMessage; }
    public StringProperty solrStatusProperty() { return solrStatus; }
    public StringProperty ocrDaemonStatusProperty() { return ocrDaemonStatus; }
    public StringProperty solrIndexBadgeProperty() { return solrIndexBadge; }
    public StringProperty roleAccessBadgeProperty() { return roleAccessBadge; }

    public UserProfile currentUser() { return currentUser; }
    public LegalContext legalContext() { return legalContext; }
    public RepositoryQuota repositoryQuota() { return repositoryQuotaProperty.get() != null ? repositoryQuotaProperty.get() : repositoryQuota; }
    public RepositoryNode repositoryTreeRoot() { return repositoryTreeRoot; }
    public TaxonomyNode taxonomyTreeRoot() { return taxonomyTreeRoot; }

    // ========================================================
    // EXPLORADOR DE ARCHIVOS LOCAL (PC)
    // ========================================================
    public void openLocalDirectory(Path dir) {
        if (dir == null) return;
        this.localFsMode.set(true);
        this.currentLocalDirectory.set(dir);

        List<DocumentNode> docs = localFileSystemService.scanDirectory(dir);
        this.allDocuments.setAll(docs);
        this.selectedDocument.set(null);

        this.breadcrumbPath.set("PC Local > " + dir.toAbsolutePath());
        this.breadcrumbUuid.set(dir.getFileName() != null ? dir.getFileName().toString() : dir.toString());
        this.repositoryQuotaProperty.set(localFileSystemService.getDiskQuota(dir));

        this.systemStatusMessage.set("Directorio local conectado: " + dir.toAbsolutePath() + " (" + docs.size() + " archivos)");
        recalculateSelectionSummary();
    }

    public void scanAndLoadFolder(Path dir) {
        if (dir == null) return;
        this.currentLocalDirectory.set(dir);

        List<DocumentNode> docs = localFileSystemService.scanDirectory(dir);
        this.allDocuments.setAll(docs);
        this.selectedDocument.set(null);

        this.breadcrumbPath.set("PC Local > " + dir.toAbsolutePath());
        this.breadcrumbUuid.set(dir.getFileName() != null ? dir.getFileName().toString() : dir.toString());
        this.systemStatusMessage.set("Carpeta: " + dir.toAbsolutePath() + " (" + docs.size() + " archivos)");
        recalculateSelectionSummary();
    }

    public void switchBackToMockRepository() {
        this.localFsMode.set(false);
        this.currentLocalDirectory.set(null);

        this.allDocuments.setAll(mockDataService.createMockDocuments());
        this.selectedDocument.set(null);

        this.breadcrumbPath.set("okm:root > Finanzas Corporativas > Contratos y Proveedores 2025");
        this.breadcrumbUuid.set("okm:root");
        this.repositoryQuotaProperty.set(mockDataService.createRepositoryQuota());
        this.systemStatusMessage.set("Repositorio Demo (okm:root) restaurado.");
        recalculateSelectionSummary();
    }

    public LocalFileSystemService getLocalFileSystemService() {
        return localFileSystemService;
    }

    public BooleanProperty localFsModeProperty() {
        return localFsMode;
    }

    public boolean isLocalFsMode() {
        return localFsMode.get();
    }

    public ObjectProperty<Path> currentLocalDirectoryProperty() {
        return currentLocalDirectory;
    }

    public Path getCurrentLocalDirectory() {
        return currentLocalDirectory.get();
    }

    public ObjectProperty<RepositoryQuota> repositoryQuotaProperty() {
        return repositoryQuotaProperty;
    }
}
