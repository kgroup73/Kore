package com.kore.ui.dialogs;

import com.kore.model.AutomationItem;
import com.kore.model.DocumentNode;
import com.kore.model.OcrEntities;
import com.kore.model.TelemetryData;
import com.kore.model.TrdMetadata;
import com.kore.state.DocumentViewModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.UUID;

/**
 * Diálogo modal para la carga de nuevos archivos al repositorio documental con metadatos TRD y OCR.
 */
public class UploadFileDialog {

    public static void show(Stage ownerStage, DocumentViewModel viewModel) {
        Stage stage = new Stage();
        stage.initOwner(ownerStage);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Kore DMS - Subir Archivo al Repositorio");
        stage.setWidth(520);
        stage.setHeight(410);

        VBox root = new VBox(14);
        root.setPadding(new Insets(18));
        root.setStyle("-fx-background-color: #FFFFFF;");

        Label title = new Label("Cargar Documento al Repositorio okm:root");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #0F172A;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);

        // 1. Selector de archivo
        Label lblFile = new Label("Archivo:");
        lblFile.setStyle("-fx-font-weight: 600; -fx-font-size: 11px;");

        TextField txtFilePath = new TextField();
        txtFilePath.setPromptText("Seleccione un archivo del disco...");
        txtFilePath.setPrefWidth(260);

        Button btnBrowse = new Button("Examinar...");
        btnBrowse.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Seleccionar archivo");
            chooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Documentos DMS (*.pdf, *.docx, *.xml)", "*.pdf", "*.docx", "*.xml", "*.xlsx"),
                    new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
            );
            File f = chooser.showOpenDialog(stage);
            if (f != null) {
                txtFilePath.setText(f.getAbsolutePath());
            }
        });

        HBox fileBox = new HBox(6, txtFilePath, btnBrowse);
        grid.add(lblFile, 0, 0);
        grid.add(fileBox, 1, 0);

        // 2. Expediente destino
        Label lblExp = new Label("Expediente:");
        lblExp.setStyle("-fx-font-weight: 600; -fx-font-size: 11px;");
        ComboBox<String> cmbExp = new ComboBox<>();
        cmbExp.getItems().addAll(
                "EXP-2025-CORP-482 (Contratos Marco Cloud)",
                "EXP-2025-CORP-483 (Acuerdos Confidencialidad)",
                "EXP-2025-AUD-104 (Auditorías Externas Q3)",
                "EXP-2025-FIN-992 (Facturación y Proveedores)"
        );
        cmbExp.getSelectionModel().select(0);
        cmbExp.setMaxWidth(Double.MAX_VALUE);
        grid.add(lblExp, 0, 1);
        grid.add(cmbExp, 1, 1);

        // 3. Serie TRD
        Label lblTrd = new Label("Serie TRD:");
        lblTrd.setStyle("-fx-font-weight: 600; -fx-font-size: 11px;");
        ComboBox<String> cmbTrd = new ComboBox<>();
        cmbTrd.getItems().addAll(
                "210.04 Contratación (5 años Archivo Central)",
                "210.01 Acuerdos y Convenios (10 años)",
                "100.12 Informes de Auditoría (20 años)",
                "300.08 Facturación Electrónica (5 años)"
        );
        cmbTrd.getSelectionModel().select(0);
        cmbTrd.setMaxWidth(Double.MAX_VALUE);
        grid.add(lblTrd, 0, 2);
        grid.add(cmbTrd, 1, 2);

        // 4. Confidencialidad
        Label lblConf = new Label("Confidencialidad:");
        lblConf.setStyle("-fx-font-weight: 600; -fx-font-size: 11px;");
        ComboBox<String> cmbConf = new ComboBox<>();
        cmbConf.getItems().addAll(
                "Nivel 1 - Interno",
                "Nivel 2 - Confidencial",
                "Nivel 3 - Restringido",
                "Nivel 4 - Secreto Corporativo"
        );
        cmbConf.getSelectionModel().select(2);
        cmbConf.setMaxWidth(Double.MAX_VALUE);
        grid.add(lblConf, 0, 3);
        grid.add(cmbConf, 1, 3);

        // 5. Checkbox OCR
        CheckBox chkOcr = new CheckBox("Ejecutar validación de firmas y OCR Solr inmediatamente");
        chkOcr.setSelected(true);
        chkOcr.setStyle("-fx-font-size: 11px; -fx-text-fill: #334155;");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Botonera de acciones
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnCancel = new Button("Cancelar");
        btnCancel.setOnAction(e -> stage.close());

        Button btnUpload = new Button("Subir y Procesar");
        btnUpload.setStyle("-fx-background-color: #2563EB; -fx-text-fill: #FFFFFF; -fx-font-weight: bold;");

        btnUpload.setOnAction(e -> {
            String path = txtFilePath.getText().trim();
            String fileName = path.isEmpty() ? "ANEXO_TECNICO_SEGURIDAD_2025.pdf" : new File(path).getName();
            String ext = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.') + 1) : "pdf";
            String newUuid = "okm:node-" + UUID.randomUUID().toString().substring(0, 8);

            DocumentNode newDoc = new DocumentNode(
                    fileName,
                    ext,
                    newUuid,
                    "SHA-256 a8f9c1e2",
                    "v1.0",
                    "Disponible",
                    "EXP-2025-CORP-482",
                    "#" + (789016 + viewModel.getAllDocuments().size()),
                    2450100L,
                    "2.4 MB",
                    "application/pdf",
                    "Registro en WAL: Activo (Tx# 90816)",
                    "Check-out (v1.1)",
                    "contratos_2025",
                    false,
                    new TrdMetadata(
                            "210.04 Contratación",
                            "5 años (Archivo Central)",
                            "Conservación Total (CT)",
                            cmbConf.getValue()
                    ),
                    new OcrEntities(
                            "99.6% Precisión",
                            "900.245.112-1 (AWS Cloud LatAm)",
                            "$95,000.00 USD",
                            "01/01/2025 - 31/12/2026",
                            "Cámara de Comercio de Bogotá"
                    ),
                    List.of(
                            new AutomationItem("validador_trd.py", AutomationItem.AutomationType.SCRIPT, "Ejecutado OK", true),
                            new AutomationItem("Webhook SAP ERP (MM)", AutomationItem.AutomationType.WEBHOOK, "Sincronizado", true)
                    ),
                    new TelemetryData(
                            "Últimos 30 días",
                            12,
                            List.of(1.0, 2.0, 4.0, 6.0, 8.0, 12.0),
                            "https://app.powerbi.com/groups/me/reports/kore-anexos"
                    )
            );

            viewModel.getAllDocuments().add(newDoc);
            viewModel.selectDocument(newDoc);
            viewModel.systemStatusMessageProperty().set("Archivo " + fileName + " cargado e indexado con UUID: " + newUuid);
            stage.close();
        });

        actions.getChildren().addAll(btnCancel, btnUpload);

        root.getChildren().addAll(title, grid, chkOcr, spacer, actions);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
