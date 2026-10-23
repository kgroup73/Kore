package com.kore.ui.components;

import com.kore.state.DocumentViewModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.stereotype.Component;

/**
 * Barra de Estado Global (Pie de ventana) con indicadores de conexión en tiempo real y metadatos técnicos.
 */
@Component
public class GlobalStatusBar extends HBox {

    private final DocumentViewModel viewModel;

    public GlobalStatusBar(DocumentViewModel viewModel) {
        this.viewModel = viewModel;
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(3, 10, 3, 10));
        setSpacing(10);
        getStyleClass().add("global-status-bar");

        // ---- Lado Izquierdo: Conexión & Daemons ----
        HBox readyBox = new HBox(6);
        readyBox.setAlignment(Pos.CENTER_LEFT);
        Circle statusDot = new Circle(3.5, Color.web("#10B981"));
        Label statusLabel = new Label();
        statusLabel.textProperty().bind(viewModel.systemStatusMessageProperty());
        statusLabel.getStyleClass().add("status-text-primary");
        readyBox.getChildren().addAll(statusDot, statusLabel);

        HBox solrBox = new HBox(4);
        solrBox.setAlignment(Pos.CENTER_LEFT);
        FontIcon dbIcon = new FontIcon(Feather.DATABASE);
        dbIcon.setIconSize(11);
        dbIcon.setIconColor(Color.web("#059669"));
        Label solrLabel = new Label();
        solrLabel.textProperty().bind(viewModel.solrStatusProperty());
        solrLabel.getStyleClass().add("status-text-secondary");
        solrBox.getChildren().addAll(dbIcon, solrLabel);

        HBox ocrBox = new HBox(4);
        ocrBox.setAlignment(Pos.CENTER_LEFT);
        FontIcon serverIcon = new FontIcon(Feather.SERVER);
        serverIcon.setIconSize(11);
        serverIcon.setIconColor(Color.web("#2563EB"));
        Label ocrLabel = new Label();
        ocrLabel.textProperty().bind(viewModel.ocrDaemonStatusProperty());
        ocrLabel.getStyleClass().add("status-text-secondary");
        ocrBox.getChildren().addAll(serverIcon, ocrLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // ---- Lado Derecho: Metadatos Técnicos ----
        Label objectsLabel = new Label("48 Objetos Seleccionados (12.4 MB)");
        objectsLabel.getStyleClass().add("status-text-secondary");

        Separator sep1 = new Separator();
        Label encodingLabel = new Label("UTF-8");
        encodingLabel.getStyleClass().add("status-text-secondary");

        Separator sep2 = new Separator();
        HBox tlsBox = new HBox(4);
        tlsBox.setAlignment(Pos.CENTER_LEFT);
        FontIcon lockIcon = new FontIcon(Feather.LOCK);
        lockIcon.setIconSize(11);
        lockIcon.setIconColor(Color.web("#475569"));
        Label tlsLabel = new Label("TLS 1.3 Cifrado");
        tlsLabel.getStyleClass().add("status-text-secondary");
        tlsBox.getChildren().addAll(lockIcon, tlsLabel);

        Separator sep3 = new Separator();
        HBox zoomBox = new HBox(6);
        zoomBox.setAlignment(Pos.CENTER_LEFT);
        FontIcon zoomIcon = new FontIcon(Feather.ZOOM_IN);
        zoomIcon.setIconSize(11);
        zoomIcon.setIconColor(Color.web("#64748B"));

        Slider zoomSlider = new Slider(50, 150, 100);
        zoomSlider.setPrefWidth(70);
        zoomSlider.getStyleClass().add("zoom-slider");
        zoomSlider.valueProperty().bindBidirectional(viewModel.zoomLevelProperty());

        Label zoomLabel = new Label();
        zoomLabel.getStyleClass().add("status-text-secondary");
        zoomLabel.textProperty().bind(
                javafx.beans.binding.Bindings.concat(viewModel.zoomLevelProperty().asString(java.util.Locale.US, "%.0f"), "%")
        );

        Tooltip zoomTooltip = new Tooltip("Ajustar escala (50% - 150%). Clic en el porcentaje para restablecer (Ctrl+0)");
        Tooltip.install(zoomBox, zoomTooltip);

        // Doble clic o clic en etiqueta para restablecer al 100%
        zoomLabel.setOnMouseClicked(e -> viewModel.resetZoom());
        zoomIcon.setOnMouseClicked(e -> viewModel.resetZoom());
        zoomLabel.setStyle("-fx-cursor: hand;");
        zoomIcon.setStyle("-fx-cursor: hand;");

        zoomBox.getChildren().addAll(zoomIcon, zoomSlider, zoomLabel);

        getChildren().addAll(
                readyBox,
                new Separator(),
                solrBox,
                new Separator(),
                ocrBox,
                spacer,
                objectsLabel,
                sep1,
                encodingLabel,
                sep2,
                tlsBox,
                sep3,
                zoomBox
        );
    }
}
