package com.kore.ui.dialogs;

import com.kore.state.DocumentViewModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Diálogo modal para la apertura y radicación de un Nuevo Expediente documental.
 */
public class NewExpedienteDialog {

    public static void show(Stage ownerStage, DocumentViewModel viewModel) {
        Stage stage = new Stage();
        stage.initOwner(ownerStage);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Kore DMS - Radicar Nuevo Expediente");
        stage.setWidth(500);
        stage.setHeight(360);

        VBox root = new VBox(14);
        root.setPadding(new Insets(18));
        root.setStyle("-fx-background-color: #FFFFFF;");

        Label title = new Label("Radicar Nuevo Expediente en la Taxonomía");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #0F172A;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);

        // 1. Código
        Label lblCode = new Label("Código Expediente:");
        lblCode.setStyle("-fx-font-weight: 600; -fx-font-size: 11px;");
        TextField txtCode = new TextField("EXP-2025-CORP-484");
        grid.add(lblCode, 0, 0);
        grid.add(txtCode, 1, 0);

        // 2. Título
        Label lblTitle = new Label("Título Expediente:");
        lblTitle.setStyle("-fx-font-weight: 600; -fx-font-size: 11px;");
        TextField txtTitle = new TextField("Acuerdo Marco Licenciamiento Cloud 2026");
        txtTitle.setPrefWidth(260);
        grid.add(lblTitle, 0, 1);
        grid.add(txtTitle, 1, 1);

        // 3. Dependencia
        Label lblDept = new Label("Dependencia:");
        lblDept.setStyle("-fx-font-weight: 600; -fx-font-size: 11px;");
        ComboBox<String> cmbDept = new ComboBox<>();
        cmbDept.getItems().addAll(
                "Dirección Jurídica & Cumplimiento",
                "Gerencia de Finanzas y Control",
                "Operaciones & Infraestructura Cloud",
                "Auditoría Forense Corporativa"
        );
        cmbDept.getSelectionModel().select(0);
        cmbDept.setMaxWidth(Double.MAX_VALUE);
        grid.add(lblDept, 0, 2);
        grid.add(cmbDept, 1, 2);

        // 4. Serie TRD
        Label lblSerie = new Label("Serie Documental:");
        lblSerie.setStyle("-fx-font-weight: 600; -fx-font-size: 11px;");
        ComboBox<String> cmbSerie = new ComboBox<>();
        cmbSerie.getItems().addAll(
                "210.04 Contratación",
                "210.01 Acuerdos y Convenios",
                "100.12 Informes de Auditoría",
                "300.08 Facturación Electrónica"
        );
        cmbSerie.getSelectionModel().select(0);
        cmbSerie.setMaxWidth(Double.MAX_VALUE);
        grid.add(lblSerie, 0, 3);
        grid.add(cmbSerie, 1, 3);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnCancel = new Button("Cancelar");
        btnCancel.setOnAction(e -> stage.close());

        Button btnCreate = new Button("Radicar Expediente");
        btnCreate.setStyle("-fx-background-color: #2563EB; -fx-text-fill: #FFFFFF; -fx-font-weight: bold;");

        btnCreate.setOnAction(e -> {
            String code = txtCode.getText().trim();
            String expTitle = txtTitle.getText().trim();
            viewModel.systemStatusMessageProperty().set("Expediente " + code + " radicado exitosamente: " + expTitle);
            stage.close();
        });

        actions.getChildren().addAll(btnCancel, btnCreate);

        root.getChildren().addAll(title, grid, spacer, actions);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
