package com.kore.ui;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import com.kore.state.DocumentViewModel;
import com.kore.ui.columns.ActiveDocumentsColumn;
import com.kore.ui.columns.DocumentInspectorColumn;
import com.kore.ui.columns.NavigationSidebarColumn;
import com.kore.ui.components.BreadcrumbBar;
import com.kore.ui.components.GlobalStatusBar;
import com.kore.ui.components.TopHeaderBar;
import com.kore.ui.dialogs.CommandPaletteDialog;
import com.kore.ui.dialogs.DaemonConsoleDialog;
import com.kore.ui.dialogs.NewExpedienteDialog;
import com.kore.ui.dialogs.UploadFileDialog;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Contenedor principal (MainLayout) de Kore Enterprise DMS.
 * Ensambla el layout de 3 columnas horizontales:
 * - Columna 1 (Izquierda): Navegación unificada (Árbol de Repositorio superior + Contexto Jurídico & Taxonomía inferior)
 * - Columna 2 (Centro): Lista de Documentos Activos
 * - Columna 3 (Derecha): Inspector Técnico del Documento (conmutable mediante F11)
 * Además gestiona atajos globales de teclado (Ctrl+K, F12, F11, Ctrl+U, Zoom).
 */
@Component
public class MainLayout {

    private final BorderPane root;
    private final SplitPane splitPane;
    private final DocumentViewModel viewModel;
    private final DocumentInspectorColumn documentInspectorColumn;
    private final NavigationSidebarColumn navigationSidebarColumn;

    private boolean inspectorVisible = true;

    public MainLayout(TopHeaderBar topHeaderBar,
                      BreadcrumbBar breadcrumbBar,
                      NavigationSidebarColumn navigationSidebarColumn,
                      ActiveDocumentsColumn activeDocumentsColumn,
                      DocumentInspectorColumn documentInspectorColumn,
                      GlobalStatusBar globalStatusBar,
                      DocumentViewModel viewModel) {

        this.viewModel = viewModel;
        this.navigationSidebarColumn = navigationSidebarColumn;
        this.documentInspectorColumn = documentInspectorColumn;
        this.root = new BorderPane();
        this.root.getStyleClass().add("kore-main-layout");

        // Cabecera superior compuesta (Menú + Toolbar + Breadcrumbs)
        VBox topContainer = new VBox(topHeaderBar, breadcrumbBar);
        this.root.setTop(topContainer);

        // Área central de 3 columnas redimensionables
        this.splitPane = new SplitPane();
        this.splitPane.getStyleClass().add("enterprise-split-pane");

        navigationSidebarColumn.setMinWidth(260);
        activeDocumentsColumn.setMinWidth(420);
        documentInspectorColumn.setMinWidth(330);

        this.splitPane.getItems().addAll(
                navigationSidebarColumn,
                activeDocumentsColumn
        );

        // Si hay un documento seleccionado inicialmente, se muestra el inspector; si no, permanece oculto
        if (viewModel.getSelectedDocument() != null) {
            this.splitPane.getItems().add(documentInspectorColumn);
            this.splitPane.setDividerPositions(0.26, 0.68);
            this.inspectorVisible = true;
        } else {
            this.splitPane.setDividerPositions(0.28);
            this.inspectorVisible = false;
        }

        // Listener reactivo: mostrar u ocultar la barra lateral derecha según haya o no selección de documento
        viewModel.selectedDocumentProperty().addListener((obs, oldDoc, newDoc) -> {
            if (newDoc != null) {
                if (!splitPane.getItems().contains(documentInspectorColumn)) {
                    splitPane.getItems().add(documentInspectorColumn);
                    splitPane.setDividerPositions(0.26, 0.68);
                }
                inspectorVisible = true;
            } else {
                splitPane.getItems().remove(documentInspectorColumn);
                splitPane.setDividerPositions(0.28);
                inspectorVisible = false;
            }
        });
        this.root.setCenter(this.splitPane);

        // Barra de estado global en el pie de página
        this.root.setBottom(globalStatusBar);

        // Cableado de acciones de la barra superior y navegación
        topHeaderBar.setOnOpenLocalFolder(this::openDirectoryChooser);
        navigationSidebarColumn.setOnOpenDirectoryRequested(this::openDirectoryChooser);
        topHeaderBar.setOnUpload(() -> UploadFileDialog.show(getOwnerStage(), viewModel));
        topHeaderBar.setOnNewFolder(() -> NewExpedienteDialog.show(getOwnerStage(), viewModel));
        topHeaderBar.setOnOpenConsole(() -> DaemonConsoleDialog.show(getOwnerStage(), viewModel));
        topHeaderBar.setOnToggleInspector(this::toggleInspector);
        topHeaderBar.setOnOpenCommandPalette(() -> CommandPaletteDialog.show(getOwnerStage(), viewModel));

        // Aplicar escala de zoom reactiva al tamaño de fuente base
        viewModel.zoomLevelProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                applyZoomScale(newV.doubleValue());
            }
        });
        applyZoomScale(viewModel.getZoomLevel());

        // Escuchar cambios de Modo Oscuro / Modo Claro reactivo
        viewModel.darkModeProperty().addListener((obs, oldV, isDark) -> {
            if (isDark != null) {
                applyTheme(isDark);
            }
        });
        applyTheme(viewModel.isDarkMode());

        // Registro de aceleradores y atajos de teclado cuando se monte en la Scene
        this.root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                registerKeyboardAccelerators(newScene);
            }
        });
    }

    public void applyTheme(boolean isDark) {
        try {
            if (isDark) {
                Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
                if (!root.getStyleClass().contains("dark-theme")) {
                    root.getStyleClass().add("dark-theme");
                }
                viewModel.systemStatusMessageProperty().set("Modo Oscuro activado (AtlantaFX Primer Dark).");
            } else {
                Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
                root.getStyleClass().remove("dark-theme");
                viewModel.systemStatusMessageProperty().set("Modo Claro activado (AtlantaFX Primer Light).");
            }
        } catch (Exception ignored) {
            // Manejo de contingencia en ejecución de tests headless
            if (isDark) {
                if (!root.getStyleClass().contains("dark-theme")) {
                    root.getStyleClass().add("dark-theme");
                }
            } else {
                root.getStyleClass().remove("dark-theme");
            }
        }
    }

    private void applyZoomScale(double zoomPercent) {
        double baseFontSize = 11.5 * (zoomPercent / 100.0);
        root.setStyle(String.format(java.util.Locale.US, "-fx-font-size: %.2fpx;", baseFontSize));
    }

    public void toggleInspector() {
        if (viewModel.getSelectedDocument() == null) {
            viewModel.systemStatusMessageProperty().set("Seleccione un documento de la lista para desplegar el Inspector (F11).");
            return;
        }

        if (isInspectorVisible()) {
            splitPane.getItems().remove(documentInspectorColumn);
            inspectorVisible = false;
            viewModel.systemStatusMessageProperty().set("Inspector ocultado (Presione F11 para restaurar).");
        } else {
            if (!splitPane.getItems().contains(documentInspectorColumn)) {
                splitPane.getItems().add(documentInspectorColumn);
                splitPane.setDividerPositions(0.26, 0.68);
            }
            inspectorVisible = true;
            viewModel.systemStatusMessageProperty().set("Inspector lateral restaurado.");
        }
    }

    public boolean isInspectorVisible() {
        return splitPane.getItems().contains(documentInspectorColumn);
    }

    private void registerKeyboardAccelerators(Scene scene) {
        // Ctrl+K -> Command Palette
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.K, KeyCombination.CONTROL_DOWN),
                () -> CommandPaletteDialog.show(getOwnerStage(), viewModel)
        );

        // F12 -> Consola de Daemons
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.F12),
                () -> DaemonConsoleDialog.show(getOwnerStage(), viewModel)
        );

        // F11 -> Toggle Inspector
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.F11),
                this::toggleInspector
        );

        // Ctrl+U -> Subir Archivo
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.U, KeyCombination.CONTROL_DOWN),
                () -> UploadFileDialog.show(getOwnerStage(), viewModel)
        );

        // Ctrl+O -> Abrir Carpeta Local (Explorador de PC)
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN),
                this::openDirectoryChooser
        );

        // Ctrl + / Ctrl - / Ctrl 0 -> Controles de Zoom
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.PLUS, KeyCombination.CONTROL_DOWN),
                viewModel::zoomIn
        );
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.EQUALS, KeyCombination.CONTROL_DOWN),
                viewModel::zoomIn
        );
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.ADD, KeyCombination.CONTROL_DOWN),
                viewModel::zoomIn
        );
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.MINUS, KeyCombination.CONTROL_DOWN),
                viewModel::zoomOut
        );
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.SUBTRACT, KeyCombination.CONTROL_DOWN),
                viewModel::zoomOut
        );
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.DIGIT0, KeyCombination.CONTROL_DOWN),
                viewModel::resetZoom
        );
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.NUMPAD0, KeyCombination.CONTROL_DOWN),
                viewModel::resetZoom
        );
    }

    public void openDirectoryChooser() {
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Seleccionar Carpeta para Explorar en Kore DMS");
            File initial = viewModel.getCurrentLocalDirectory() != null
                    ? viewModel.getCurrentLocalDirectory().toFile()
                    : new File(System.getProperty("user.dir"));
            if (initial.exists() && initial.isDirectory()) {
                chooser.setInitialDirectory(initial);
            }
            File selected = chooser.showDialog(getOwnerStage());
            if (selected != null) {
                viewModel.openLocalDirectory(selected.toPath());
            }
        } catch (Exception e) {
            System.err.println("[MainLayout] Error abriendo selector de directorios: " + e.getMessage());
        }
    }

    private Stage getOwnerStage() {
        if (root.getScene() != null && root.getScene().getWindow() instanceof Stage stage) {
            return stage;
        }
        return null;
    }

    public BorderPane getRoot() {
        return root;
    }
}
