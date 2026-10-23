package com.kore.ui.columns;

import com.kore.model.AuditLogItem;
import com.kore.model.AutomationItem;
import com.kore.model.BpmWorkflowStep;
import com.kore.model.DocumentNode;
import com.kore.model.OcrEntities;
import com.kore.model.TelemetryData;
import com.kore.model.TrdMetadata;
import com.kore.model.VersionItem;
import com.kore.state.DocumentViewModel;
import com.kore.ui.components.SparklineCanvas;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.stereotype.Component;

/**
 * Columna 4 - Inspector del Documento (Derecha).
 * Presenta cabecera con badge de formato, estado de bloqueo y pestañas completas:
 * - Detalles: Clasificación TRD, Entidades OCR Solr/Tesseract, Automatizaciones & Integraciones, Telemetría PowerBI
 * - Versiones (4): Historial de versiones forenses con autor y notas de cambio
 * - Auditoría (142): Registro inmutable de eventos de acceso y verificación SHA-256
 * - BPM: Flujo de trabajo activo por etapas
 * - Barra de acciones inferior: Check-in/Check-out reactivo, Descarga y Derivación.
 */
@Component
public class DocumentInspectorColumn extends VBox {

    private final DocumentViewModel viewModel;

    // Elementos de cabecera reactivos
    private final Label formatBadge = new Label("PDF");
    private final Label titleLabel = new Label();
    private final Label statusBadge = new Label("Bloqueado");
    private final Label expedienteLabel = new Label();
    private final Label docIdLabel = new Label();

    // Elementos TRD reactivos
    private final Label trdCodigoVal = new Label();
    private final Label trdRetencionVal = new Label();
    private final Label trdDisposicionVal = new Label();
    private final Label trdConfidencialidadVal = new Label();

    // Elementos OCR reactivos
    private final Label ocrPrecisionBadge = new Label("99.4% Precisión");
    private final Label ocrNitVal = new Label();
    private final Label ocrCuantiaVal = new Label();
    private final Label ocrVigenciaVal = new Label();
    private final Label ocrJurisdiccionVal = new Label();

    // Contenedor de automatizaciones
    private final VBox automationsBox = new VBox(6);

    // Telemetría
    private final Label telemetryAccessLabel = new Label();
    private final SparklineCanvas sparklineCanvas = new SparklineCanvas(310, 48);

    // Botón de acción principal
    private final Button btnPrimaryAction = new Button();

    public DocumentInspectorColumn(DocumentViewModel viewModel) {
        this.viewModel = viewModel;
        getStyleClass().add("column-container");
        setSpacing(0);

        getChildren().addAll(
                createInspectorHeader(),
                createTabPane(),
                createBottomActionToolbar()
        );

        // Enlazar datos con el documento seleccionado actualmente
        viewModel.selectedDocumentProperty().addListener((obs, oldDoc, newDoc) -> bindDocument(newDoc));
        if (viewModel.getSelectedDocument() != null) {
            bindDocument(viewModel.getSelectedDocument());
        }
    }

    private VBox createInspectorHeader() {
        VBox header = new VBox(4);
        header.setPadding(new Insets(10, 12, 10, 12));
        header.getStyleClass().add("inspector-header");

        HBox topRow = new HBox(6);
        topRow.setAlignment(Pos.CENTER_LEFT);

        formatBadge.getStyleClass().addAll("enterprise-badge", "badge-format");

        titleLabel.getStyleClass().add("inspector-doc-title");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        statusBadge.getStyleClass().addAll("enterprise-badge", "badge-locked");

        Button btnPreview = new Button();
        FontIcon eyeIcon = new FontIcon(Feather.EYE);
        eyeIcon.setIconSize(12);
        eyeIcon.setIconColor(Color.web("#2563EB"));
        btnPreview.setGraphic(eyeIcon);
        btnPreview.setTooltip(new Tooltip("Abrir Visor Integrado (Doble clic en fila)"));
        btnPreview.setStyle("-fx-background-color: transparent; -fx-padding: 2 4 2 4; -fx-cursor: hand;");
        btnPreview.setOnAction(e -> {
            DocumentNode doc = viewModel.getSelectedDocument();
            if (doc != null) {
                javafx.stage.Stage owner = null;
                if (getScene() != null && getScene().getWindow() instanceof javafx.stage.Stage st) {
                    owner = st;
                }
                com.kore.ui.dialogs.DocumentPreviewDialog.show(owner, doc, viewModel);
            }
        });

        topRow.getChildren().addAll(formatBadge, titleLabel, btnPreview, statusBadge);

        HBox subRow = new HBox(8);
        subRow.setAlignment(Pos.CENTER_LEFT);
        expedienteLabel.getStyleClass().add("inspector-subtitle");
        docIdLabel.getStyleClass().add("inspector-subtitle");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        subRow.getChildren().addAll(expedienteLabel, spacer, docIdLabel);

        header.getChildren().addAll(topRow, subRow);
        return header;
    }

    private TabPane createTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("inspector-tab-pane");
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        // 1. Pestaña Detalles
        Tab tabDetails = new Tab("Detalles", createDetailsAccordion());
        tabDetails.setClosable(false);

        // 2. Pestaña Versiones (4)
        Tab tabVersions = new Tab("Versiones (4)", createVersionsView());
        tabVersions.setClosable(false);

        // 3. Pestaña Auditoría (142)
        Tab tabAudit = new Tab("Auditoría (142)", createAuditView());
        tabAudit.setClosable(false);

        // 4. Pestaña BPM
        Tab tabBpm = new Tab("BPM", createBpmView());
        tabBpm.setClosable(false);

        tabPane.getTabs().addAll(tabDetails, tabVersions, tabAudit, tabBpm);
        return tabPane;
    }

    private ScrollPane createDetailsAccordion() {
        Accordion accordion = new Accordion();
        accordion.getStyleClass().add("enterprise-accordion");

        // Panel 1: Clasificación TRD
        TitledPane paneTrd = new TitledPane("Clasificación TRD", createTrdGrid());
        FontIcon trdIcon = new FontIcon(Feather.TAG);
        trdIcon.setIconSize(12);
        paneTrd.setGraphic(trdIcon);

        // Panel 2: Entidades OCR
        TitledPane paneOcr = new TitledPane("Entidades OCR Solr / Tesseract", createOcrSection());
        FontIcon ocrIcon = new FontIcon(Feather.SEARCH);
        ocrIcon.setIconSize(12);
        paneOcr.setGraphic(ocrIcon);

        // Panel 3: Automatizaciones & Integraciones
        TitledPane paneAuto = new TitledPane("Automatizaciones & Integraciones", automationsBox);
        FontIcon autoIcon = new FontIcon(Feather.ZAP);
        autoIcon.setIconSize(12);
        paneAuto.setGraphic(autoIcon);

        // Panel 4: Telemetría de Uso (PowerBI)
        TitledPane paneTelemetry = new TitledPane("Telemetría de Uso (PowerBI)", createTelemetrySection());
        FontIcon telemIcon = new FontIcon(Feather.BAR_CHART_2);
        telemIcon.setIconSize(12);
        paneTelemetry.setGraphic(telemIcon);

        accordion.getPanes().addAll(paneTrd, paneOcr, paneAuto, paneTelemetry);
        accordion.setExpandedPane(paneTrd);

        ScrollPane scrollPane = new ScrollPane(accordion);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("inspector-scroll-pane");
        return scrollPane;
    }

    private GridPane createTrdGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(8));

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);

        grid.add(createFieldBlock("Código TRD", trdCodigoVal), 0, 0);
        grid.add(createFieldBlock("Tiempo Retención", trdRetencionVal), 1, 0);
        grid.add(createFieldBlock("Disposición Final", trdDisposicionVal), 0, 1);
        grid.add(createFieldBlock("Confidencialidad", trdConfidencialidadVal), 1, 1);

        return grid;
    }

    private VBox createOcrSection() {
        VBox box = new VBox(6);
        box.setPadding(new Insets(8));

        HBox badgeRow = new HBox(6);
        badgeRow.setAlignment(Pos.CENTER_LEFT);
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        ocrPrecisionBadge.getStyleClass().addAll("enterprise-badge", "badge-success");
        badgeRow.getChildren().addAll(sp, ocrPrecisionBadge);

        box.getChildren().addAll(
                badgeRow,
                createKvRow("NIT Identificado:", ocrNitVal),
                createKvRow("Valor Cuantía:", ocrCuantiaVal),
                createKvRow("Periodo Vigencia:", ocrVigenciaVal),
                createKvRow("Jurisdicción:", ocrJurisdiccionVal)
        );
        return box;
    }

    private VBox createTelemetrySection() {
        VBox box = new VBox(6);
        box.setPadding(new Insets(8));

        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);
        Label period = new Label("Últimos 30 días");
        period.getStyleClass().add("telemetry-subtitle");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        telemetryAccessLabel.getStyleClass().add("telemetry-count");
        top.getChildren().addAll(period, sp, telemetryAccessLabel);

        Button btnPowerBi = new Button("Abrir Reporte en PowerBI Workspace");
        btnPowerBi.setMaxWidth(Double.MAX_VALUE);
        FontIcon pbiIcon = new FontIcon(Feather.EXTERNAL_LINK);
        pbiIcon.setIconSize(12);
        btnPowerBi.setGraphic(pbiIcon);
        btnPowerBi.getStyleClass().add("powerbi-link-button");
        btnPowerBi.setOnAction(e -> {
            DocumentNode doc = viewModel.getSelectedDocument();
            if (doc != null && doc.getTelemetryData() != null) {
                viewModel.systemStatusMessageProperty().set("PowerBI Workspace abierto: " + doc.getTelemetryData().reportUrl());
            }
        });

        box.getChildren().addAll(top, sparklineCanvas, btnPowerBi);
        return box;
    }

    private VBox createVersionsView() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(10));

        TableView<VersionItem> table = new TableView<>(viewModel.getCurrentVersions());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setStyle("-fx-font-size: 10.5px;");

        TableColumn<VersionItem, String> colVer = new TableColumn<>("Versión");
        colVer.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().version()));
        colVer.setPrefWidth(60);

        TableColumn<VersionItem, String> colDate = new TableColumn<>("Fecha");
        colDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().timestamp()));
        colDate.setPrefWidth(110);

        TableColumn<VersionItem, String> colAuthor = new TableColumn<>("Autor");
        colAuthor.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().author()));
        colAuthor.setPrefWidth(90);

        TableColumn<VersionItem, String> colNotes = new TableColumn<>("Cambio / Comentario");
        colNotes.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().comment()));
        colNotes.setPrefWidth(160);

        table.getColumns().addAll(colVer, colDate, colAuthor, colNotes);
        box.getChildren().add(table);
        return box;
    }

    private VBox createAuditView() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(10));

        TableView<AuditLogItem> table = new TableView<>(viewModel.getCurrentAuditLogs());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setStyle("-fx-font-size: 10px;");

        TableColumn<AuditLogItem, String> colTime = new TableColumn<>("Timestamp");
        colTime.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().timestamp()));
        colTime.setPrefWidth(115);

        TableColumn<AuditLogItem, String> colEvent = new TableColumn<>("Evento");
        colEvent.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().eventType()));
        colEvent.setPrefWidth(95);

        TableColumn<AuditLogItem, String> colActor = new TableColumn<>("Actor / IP");
        colActor.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().actor() + " (" + data.getValue().ipAddress() + ")"));
        colActor.setPrefWidth(130);

        TableColumn<AuditLogItem, String> colDetails = new TableColumn<>("Detalles Forenses");
        colDetails.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().details()));
        colDetails.setPrefWidth(160);

        table.getColumns().addAll(colTime, colEvent, colActor, colDetails);
        box.getChildren().add(table);
        return box;
    }

    private VBox createBpmView() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(12));

        Label bpmHeader = new Label("Flujo de Aprobación Documental (BPMN 2.0)");
        bpmHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 11.5px; -fx-text-fill: #0F172A;");
        box.getChildren().add(bpmHeader);

        VBox stepsBox = new VBox(10);
        for (BpmWorkflowStep step : viewModel.getCurrentBpmSteps()) {
            HBox stepRow = new HBox(8);
            stepRow.setAlignment(Pos.CENTER_LEFT);

            StackPane circleStack = new StackPane();
            Circle circle = new Circle(12);
            Label stepNum = new Label(String.valueOf(step.stepNumber()));

            if (step.state() == BpmWorkflowStep.StepState.COMPLETED) {
                circle.setFill(Color.web("#10B981"));
                stepNum.setStyle("-fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-font-size: 10px;");
            } else if (step.state() == BpmWorkflowStep.StepState.IN_PROGRESS) {
                circle.setFill(Color.web("#2563EB"));
                stepNum.setStyle("-fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-font-size: 10px;");
            } else {
                circle.setFill(Color.web("#E2E8F0"));
                stepNum.setStyle("-fx-text-fill: #64748B; -fx-font-weight: bold; -fx-font-size: 10px;");
            }
            circleStack.getChildren().addAll(circle, stepNum);

            VBox info = new VBox(1);
            Label title = new Label(step.title());
            title.setStyle("-fx-font-weight: 600; -fx-font-size: 11px; -fx-text-fill: #1E293B;");

            Label sub = new Label("Asignado a: " + step.assignee() + " • " + step.status());
            sub.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748B;");

            info.getChildren().addAll(title, sub);
            stepRow.getChildren().addAll(circleStack, info);
            stepsBox.getChildren().add(stepRow);
        }

        box.getChildren().add(stepsBox);
        return box;
    }

    private HBox createBottomActionToolbar() {
        HBox toolbar = new HBox(8);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(10, 12, 10, 12));
        toolbar.getStyleClass().add("inspector-action-bar");

        btnPrimaryAction.getStyleClass().addAll("btn-primary-action");
        FontIcon lockIcon = new FontIcon(Feather.LOCK);
        lockIcon.setIconSize(13);
        lockIcon.setIconColor(Color.WHITE);
        btnPrimaryAction.setGraphic(lockIcon);
        btnPrimaryAction.setTooltip(new Tooltip("Bloquear / Liberar versión del documento"));
        btnPrimaryAction.setOnAction(e -> viewModel.toggleCheckInStatus());

        Button btnDownload = new Button("Descargar");
        FontIcon dlIcon = new FontIcon(Feather.DOWNLOAD);
        dlIcon.setIconSize(13);
        btnDownload.setGraphic(dlIcon);
        btnDownload.getStyleClass().add("btn-secondary-action");
        btnDownload.setOnAction(e -> {
            DocumentNode doc = viewModel.getSelectedDocument();
            if (doc != null) {
                viewModel.systemStatusMessageProperty().set("Descargando " + doc.getName() + " (" + doc.getSizeFormatted() + ")... Completado.");
            }
        });

        Button btnDerive = new Button("Derivar");
        FontIcon devIcon = new FontIcon(Feather.CORNER_UP_RIGHT);
        devIcon.setIconSize(13);
        btnDerive.setGraphic(devIcon);
        btnDerive.getStyleClass().add("btn-secondary-action");
        btnDerive.setOnAction(e -> {
            DocumentNode doc = viewModel.getSelectedDocument();
            if (doc != null) {
                viewModel.systemStatusMessageProperty().set("Expediente " + doc.getExpediente() + " derivado a Bandeja de Revisión Jurídica.");
            }
        });

        Button btnOpenNative = new Button("Abrir");
        FontIcon openIcon = new FontIcon(Feather.EXTERNAL_LINK);
        openIcon.setIconSize(13);
        btnOpenNative.setGraphic(openIcon);
        btnOpenNative.setTooltip(new Tooltip("Abrir archivo con la aplicación predeterminada de Windows"));
        btnOpenNative.getStyleClass().add("btn-secondary-action");
        btnOpenNative.setOnAction(e -> {
            DocumentNode doc = viewModel.getSelectedDocument();
            if (doc != null) {
                try {
                    java.nio.file.Path p = java.nio.file.Paths.get(doc.getUuid());
                    if (java.nio.file.Files.exists(p)) {
                        viewModel.getLocalFileSystemService().openInSystemDefault(p);
                        viewModel.systemStatusMessageProperty().set("Archivo abierto en aplicación nativa: " + doc.getName());
                    } else {
                        viewModel.systemStatusMessageProperty().set("Archivo de demostración en memoria: " + doc.getName());
                    }
                } catch (Exception ex) {
                    viewModel.systemStatusMessageProperty().set("Ruta no accesible en sistema de archivos: " + doc.getUuid());
                }
            }
        });

        toolbar.getChildren().addAll(btnPrimaryAction, btnOpenNative, btnDownload, btnDerive);
        return toolbar;
    }

    private void bindDocument(DocumentNode doc) {
        if (doc == null) return;

        formatBadge.setText(doc.getExtension().toUpperCase());
        if ("pdf".equalsIgnoreCase(doc.getExtension())) {
            formatBadge.setStyle("-fx-background-color: #DC2626; -fx-text-fill: white; -fx-font-weight: bold;");
        } else if ("docx".equalsIgnoreCase(doc.getExtension())) {
            formatBadge.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: bold;");
        } else if ("xml".equalsIgnoreCase(doc.getExtension())) {
            formatBadge.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-font-weight: bold;");
        }

        titleLabel.setText(doc.getName());
        statusBadge.setText(doc.getStatus());
        if ("Bloqueado".equalsIgnoreCase(doc.getStatus())) {
            statusBadge.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-font-weight: bold;");
        } else if ("En Revisión".equalsIgnoreCase(doc.getStatus())) {
            statusBadge.setStyle("-fx-background-color: #FEF3C7; -fx-text-fill: #D97706; -fx-font-weight: bold;");
        } else {
            statusBadge.setStyle("-fx-background-color: #DCFCE7; -fx-text-fill: #16A34A; -fx-font-weight: bold;");
        }

        // Enlazar dinámicamente si el estado cambia
        doc.statusProperty().addListener((obs, oldS, newS) -> {
            statusBadge.setText(newS);
            if ("Bloqueado".equalsIgnoreCase(newS)) {
                statusBadge.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-font-weight: bold;");
            } else if ("En Revisión".equalsIgnoreCase(newS)) {
                statusBadge.setStyle("-fx-background-color: #FEF3C7; -fx-text-fill: #D97706; -fx-font-weight: bold;");
            } else {
                statusBadge.setStyle("-fx-background-color: #DCFCE7; -fx-text-fill: #16A34A; -fx-font-weight: bold;");
            }
        });

        expedienteLabel.setText("Expediente: " + doc.getExpediente());
        docIdLabel.setText("Doc ID: " + doc.getDocId());

        TrdMetadata trd = doc.getTrdMetadata();
        if (trd != null) {
            trdCodigoVal.setText(trd.codigoTrd());
            trdRetencionVal.setText(trd.tiempoRetencion());
            trdDisposicionVal.setText(trd.disposicionFinal());
            trdConfidencialidadVal.setText(trd.confidencialidad());
        }

        OcrEntities ocr = doc.getOcrEntities();
        if (ocr != null) {
            ocrPrecisionBadge.setText(ocr.precision());
            ocrNitVal.setText(ocr.nitIdentificado());
            ocrCuantiaVal.setText(ocr.valorCuantia());
            ocrVigenciaVal.setText(ocr.periodoVigencia());
            ocrJurisdiccionVal.setText(ocr.jurisdiccion());
        }

        // Automatizaciones
        automationsBox.getChildren().clear();
        for (AutomationItem item : doc.getAutomations()) {
            HBox row = new HBox(6);
            row.setAlignment(Pos.CENTER_LEFT);
            FontIcon icon = new FontIcon(item.type() == AutomationItem.AutomationType.SCRIPT ? Feather.TERMINAL : Feather.LINK);
            icon.setIconSize(12);
            icon.setIconColor(Color.web("#64748B"));

            Label name = new Label(item.name());
            name.getStyleClass().add("automation-item-name");

            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);

            Label status = new Label(item.status());
            status.getStyleClass().add("automation-item-status");

            row.getChildren().addAll(icon, name, sp, status);
            automationsBox.getChildren().add(row);
        }

        // Telemetría
        TelemetryData telem = doc.getTelemetryData();
        if (telem != null) {
            telemetryAccessLabel.setText(telem.accessCount() + " accesos");
            sparklineCanvas.render(telem.sparklinePoints());
        }

        // Botón principal
        btnPrimaryAction.setText(doc.getNextVersionAction());
        doc.nextVersionActionProperty().addListener((obs, oldA, newA) -> btnPrimaryAction.setText(newA));
    }

    private VBox createFieldBlock(String label, Label valueLabel) {
        VBox box = new VBox(2);
        Label title = new Label(label);
        title.getStyleClass().add("inspector-field-title");
        valueLabel.getStyleClass().add("inspector-field-value");
        box.getChildren().addAll(title, valueLabel);
        return box;
    }

    private HBox createKvRow(String label, Label valueLabel) {
        HBox row = new HBox(6);
        row.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(label);
        lbl.getStyleClass().add("inspector-kv-key");
        lbl.setMinWidth(110);
        valueLabel.getStyleClass().add("inspector-kv-val");
        row.getChildren().addAll(lbl, valueLabel);
        return row;
    }
}
