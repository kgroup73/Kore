package com.kore.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

/**
 * Representa un documento activo en el sistema Kore Enterprise DMS.
 * Cuenta con propiedades JavaFX reactivas para data-binding bidireccional.
 */
public class DocumentNode {

    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty extension = new SimpleStringProperty();
    private final StringProperty uuid = new SimpleStringProperty();
    private final StringProperty sha256 = new SimpleStringProperty();
    private final StringProperty version = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final StringProperty expediente = new SimpleStringProperty();
    private final StringProperty docId = new SimpleStringProperty();
    private final StringProperty sizeFormatted = new SimpleStringProperty();
    private final StringProperty mimeType = new SimpleStringProperty();
    private final StringProperty walRecord = new SimpleStringProperty();
    private final StringProperty nextVersionAction = new SimpleStringProperty();
    private final StringProperty categoryKey = new SimpleStringProperty();
    private final BooleanProperty selected = new SimpleBooleanProperty(false);
    private final ObjectProperty<TrdMetadata> trdMetadata = new SimpleObjectProperty<>();
    private final ObjectProperty<OcrEntities> ocrEntities = new SimpleObjectProperty<>();
    private final ObservableList<AutomationItem> automations = FXCollections.observableArrayList();
    private final ObjectProperty<TelemetryData> telemetryData = new SimpleObjectProperty<>();
    private final long sizeBytes;

    public DocumentNode(String name,
                        String extension,
                        String uuid,
                        String sha256,
                        String version,
                        String status,
                        String expediente,
                        String docId,
                        long sizeBytes,
                        String sizeFormatted,
                        String mimeType,
                        String walRecord,
                        String nextVersionAction,
                        String categoryKey,
                        boolean selected,
                        TrdMetadata trdMetadata,
                        OcrEntities ocrEntities,
                        List<AutomationItem> automations,
                        TelemetryData telemetryData) {
        this.name.set(name);
        this.extension.set(extension);
        this.uuid.set(uuid);
        this.sha256.set(sha256);
        this.version.set(version);
        this.status.set(status);
        this.expediente.set(expediente);
        this.docId.set(docId);
        this.sizeBytes = sizeBytes;
        this.sizeFormatted.set(sizeFormatted);
        this.mimeType.set(mimeType);
        this.walRecord.set(walRecord);
        this.nextVersionAction.set(nextVersionAction);
        this.categoryKey.set(categoryKey);
        this.selected.set(selected);
        this.trdMetadata.set(trdMetadata);
        this.ocrEntities.set(ocrEntities);
        if (automations != null) {
            this.automations.addAll(automations);
        }
        this.telemetryData.set(telemetryData);
    }

    // Property Getters
    public StringProperty nameProperty() { return name; }
    public StringProperty extensionProperty() { return extension; }
    public StringProperty uuidProperty() { return uuid; }
    public StringProperty sha256Property() { return sha256; }
    public StringProperty versionProperty() { return version; }
    public StringProperty statusProperty() { return status; }
    public StringProperty expedienteProperty() { return expediente; }
    public StringProperty docIdProperty() { return docId; }
    public StringProperty sizeFormattedProperty() { return sizeFormatted; }
    public StringProperty mimeTypeProperty() { return mimeType; }
    public StringProperty walRecordProperty() { return walRecord; }
    public StringProperty nextVersionActionProperty() { return nextVersionAction; }
    public StringProperty categoryKeyProperty() { return categoryKey; }
    public BooleanProperty selectedProperty() { return selected; }
    public ObjectProperty<TrdMetadata> trdMetadataProperty() { return trdMetadata; }
    public ObjectProperty<OcrEntities> ocrEntitiesProperty() { return ocrEntities; }
    public ObservableList<AutomationItem> getAutomations() { return automations; }
    public ObjectProperty<TelemetryData> telemetryDataProperty() { return telemetryData; }

    // Value Getters
    public String getName() { return name.get(); }
    public String getExtension() { return extension.get(); }
    public String getUuid() { return uuid.get(); }
    public String getSha256() { return sha256.get(); }
    public String getVersion() { return version.get(); }
    public String getStatus() { return status.get(); }
    public String getExpediente() { return expediente.get(); }
    public String getDocId() { return docId.get(); }
    public long getSizeBytes() { return sizeBytes; }
    public String getSizeFormatted() { return sizeFormatted.get(); }
    public String getMimeType() { return mimeType.get(); }
    public String getWalRecord() { return walRecord.get(); }
    public String getNextVersionAction() { return nextVersionAction.get(); }
    public String getCategoryKey() { return categoryKey.get(); }
    public boolean isSelected() { return selected.get(); }
    public TrdMetadata getTrdMetadata() { return trdMetadata.get(); }
    public OcrEntities getOcrEntities() { return ocrEntities.get(); }
    public TelemetryData getTelemetryData() { return telemetryData.get(); }

    // Setters
    public void setSelected(boolean selected) { this.selected.set(selected); }
    public void setStatus(String status) { this.status.set(status); }
}
