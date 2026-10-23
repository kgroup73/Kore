package com.kore.ui.components;

import atlantafx.base.controls.ToggleSwitch;
import com.kore.model.DocumentNode;
import com.kore.state.DocumentViewModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.stereotype.Component;

/**
 * Cabecera Superior y Barra de Herramientas de Kore Enterprise DMS.
 * Incluye:
 * - Barra de menús empresariales (Archivo, Edición, Ver, Herramientas, Repositorio, Ventana, Ayuda)
 * - Barra de acciones rápidas con iconos (Subir, Nuevo, Check-out, Check-in, Aprobar, Consola F12, Inspector F11)
 * - Buscador global rápido con soporte para Command Palette (Ctrl+K)
 * - Monitor de memoria JVM Heap reactivo
 */
@Component
public class TopHeaderBar extends VBox {

    private final DocumentViewModel viewModel;

    private Button btnOpenFolder;
    private Button btnUpload;
    private Button btnNewFolder;
    private Button btnCheckOut;
    private Button btnCheckIn;
    private Button btnApprove;
    private Button btnConsole;
    private Button btnInspector;
    private TextField searchField;
    private HBox searchBox;

    private MenuItem menuOpenFolder;
    private MenuItem menuUpload;
    private MenuItem menuConsole;
    private MenuItem menuToggleInspector;
    private Runnable onOpenLocalFolderAction;

    public TopHeaderBar(DocumentViewModel viewModel) {
        this.viewModel = viewModel;
        getStyleClass().add("top-header-bar");
        setSpacing(0);

        getChildren().addAll(createMenuBar(), createToolBar());
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.getStyleClass().add("enterprise-menu-bar");

        Menu fileMenu = new Menu("Archivo");
        menuOpenFolder = new MenuItem("Abrir Carpeta Local... (Ctrl+O)");
        menuUpload = new MenuItem("Subir Archivo...");
        fileMenu.getItems().addAll(menuOpenFolder, menuUpload, new MenuItem("Exportar Todo..."), new MenuItem("Salir"));

        Menu editMenu = new Menu("Edición");
        editMenu.getItems().addAll(new MenuItem("Deshacer"), new MenuItem("Rehacer"), new MenuItem("Preferencias..."));

        Menu viewMenu = new Menu("Ver");
        menuToggleInspector = new MenuItem("Alternar Inspector (F11)");
        CheckMenuItem menuDarkMode = new CheckMenuItem("Modo Oscuro");
        menuDarkMode.selectedProperty().bindBidirectional(viewModel.darkModeProperty());
        viewMenu.getItems().addAll(new MenuItem("Recargar Vista"), menuToggleInspector, menuDarkMode, new MenuItem("Modo Alta Densidad"));

        Menu toolsMenu = new Menu("Herramientas");
        menuConsole = new MenuItem("Consola de Daemons (F12)");
        toolsMenu.getItems().addAll(menuConsole, new MenuItem("Auditoría Solr"), new MenuItem("Validador TRD"));

        Menu repoMenu = new Menu("Repositorio");
        MenuItem menuOpenLocal = new MenuItem("Abrir Carpeta de la PC (Ctrl+O)...");
        menuOpenLocal.setOnAction(e -> {
            if (onOpenLocalFolderAction != null) onOpenLocalFolderAction.run();
        });
        MenuItem menuResetDemo = new MenuItem("Restablecer a Repositorio Demo (okm:root)");
        menuResetDemo.setOnAction(e -> viewModel.switchBackToMockRepository());
        repoMenu.getItems().addAll(menuOpenLocal, menuResetDemo, new SeparatorMenuItem(), new MenuItem("Sincronizar okm:root"), new MenuItem("Verificar Integridad SHA-256"));

        Menu windowMenu = new Menu("Ventana");
        windowMenu.getItems().addAll(new MenuItem("Restablecer Disposición"));

        Menu helpMenu = new Menu("Ayuda");
        helpMenu.getItems().addAll(new MenuItem("Documentación Kore"), new MenuItem("Acerca de..."));

        menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu, toolsMenu, repoMenu, windowMenu, helpMenu);
        return menuBar;
    }

    private ToolBar createToolBar() {
        ToolBar toolBar = new ToolBar();
        toolBar.getStyleClass().add("enterprise-tool-bar");

        btnOpenFolder = createIconButton("Abrir Carpeta", Feather.FOLDER);
        btnOpenFolder.setTooltip(new Tooltip("Explorar archivos reales de la PC (Ctrl+O)"));

        btnUpload = createIconButton("Subir Archivo", Feather.UPLOAD);
        btnUpload.setTooltip(new Tooltip("Cargar nuevo archivo al repositorio (Ctrl+U)"));

        btnNewFolder = createIconButton("Nuevo Expediente", Feather.FOLDER_PLUS);
        btnNewFolder.setTooltip(new Tooltip("Aperturar y radicar nuevo expediente"));

        Separator sep1 = new Separator();

        btnCheckOut = createIconButton("Check-out", Feather.LOCK);
        btnCheckOut.setTooltip(new Tooltip("Bloquear documento para edición exclusiva"));
        btnCheckOut.disableProperty().bind(viewModel.selectedDocumentProperty().isNull());
        btnCheckOut.setOnAction(e -> {
            DocumentNode doc = viewModel.getSelectedDocument();
            if (doc != null && "Disponible".equalsIgnoreCase(doc.getStatus())) {
                viewModel.toggleCheckInStatus();
            }
        });

        btnCheckIn = createIconButton("Check-in", Feather.UNLOCK);
        btnCheckIn.setTooltip(new Tooltip("Liberar documento e incrementar versión"));
        btnCheckIn.disableProperty().bind(viewModel.selectedDocumentProperty().isNull());
        btnCheckIn.setOnAction(e -> {
            DocumentNode doc = viewModel.getSelectedDocument();
            if (doc != null && "Bloqueado".equalsIgnoreCase(doc.getStatus())) {
                viewModel.toggleCheckInStatus();
            }
        });

        btnApprove = createIconButton("Aprobar Flujo", Feather.CHECK_CIRCLE);
        btnApprove.setTooltip(new Tooltip("Aprobar etapa activa en flujo BPM"));
        btnApprove.disableProperty().bind(viewModel.selectedDocumentProperty().isNull());
        btnApprove.setOnAction(e -> {
            DocumentNode doc = viewModel.getSelectedDocument();
            if (doc != null) {
                viewModel.systemStatusMessageProperty().set("Flujo BPM aprobado para " + doc.getName());
            }
        });

        Separator sep2 = new Separator();

        btnConsole = createIconButton("Consola (F12)", Feather.TERMINAL);
        btnConsole.setTooltip(new Tooltip("Abrir consola de daemons Solr y OCR (F12)"));

        btnInspector = createIconButton("Inspector (F11)", Feather.SIDEBAR);
        btnInspector.setTooltip(new Tooltip("Mostrar / ocultar inspector lateral (F11)"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Buscador global rápido (Ctrl+K)
        searchBox = new HBox(6);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.getStyleClass().add("search-container");
        Tooltip.install(searchBox, new Tooltip("Haga clic o presione Ctrl+K para abrir Command Palette"));

        FontIcon searchIcon = new FontIcon(Feather.SEARCH);
        searchIcon.setIconSize(13);
        searchIcon.getStyleClass().add("search-icon");

        searchField = new TextField();
        searchField.setPromptText("Buscar nodo, expediente o UUID (Ctrl+K)");
        searchField.setPrefWidth(280);
        searchField.getStyleClass().add("enterprise-search-field");
        searchField.setEditable(false); // Abre Command Palette al enfocar o hacer clic

        searchBox.getChildren().addAll(searchIcon, searchField);

        // Indicador de memoria JVM Heap
        HBox heapBox = new HBox(6);
        heapBox.setAlignment(Pos.CENTER_LEFT);
        heapBox.setPadding(new Insets(0, 8, 0, 8));
        heapBox.getStyleClass().add("heap-indicator-box");

        Circle heapDot = new Circle(4, Color.web("#10B981"));
        Label heapLabel = new Label();
        heapLabel.textProperty().bind(viewModel.jvmHeapStatusProperty());
        heapLabel.getStyleClass().add("heap-indicator-label");

        heapBox.getChildren().addAll(heapDot, heapLabel);

        Separator sep3 = new Separator();

        // Switch de Modo Oscuro en la parte superior derecha
        HBox themeSwitchBox = new HBox(6);
        themeSwitchBox.setAlignment(Pos.CENTER_LEFT);
        themeSwitchBox.setPadding(new Insets(0, 4, 0, 4));

        FontIcon sunIcon = new FontIcon(Feather.SUN);
        sunIcon.setIconSize(13);
        sunIcon.setIconColor(Color.web("#F59E0B")); // Ámbar dorado para Sol

        ToggleSwitch darkModeSwitch = new ToggleSwitch();
        darkModeSwitch.setSelected(viewModel.isDarkMode());
        darkModeSwitch.setTooltip(new Tooltip("Cambiar entre Modo Claro y Modo Oscuro"));
        darkModeSwitch.selectedProperty().addListener((obs, oldVal, newVal) -> {
            viewModel.setDarkMode(newVal);
        });
        viewModel.darkModeProperty().addListener((obs, oldVal, newVal) -> {
            if (darkModeSwitch.isSelected() != newVal) {
                darkModeSwitch.setSelected(newVal);
            }
        });

        FontIcon moonIcon = new FontIcon(Feather.MOON);
        moonIcon.setIconSize(13);
        moonIcon.setIconColor(Color.web("#64748B")); // Pizarra para Luna

        themeSwitchBox.getChildren().addAll(sunIcon, darkModeSwitch, moonIcon);

        toolBar.getItems().addAll(
                btnOpenFolder, btnUpload, btnNewFolder,
                sep1,
                btnCheckOut, btnCheckIn, btnApprove,
                sep2,
                btnConsole, btnInspector,
                spacer,
                searchBox,
                heapBox,
                sep3,
                themeSwitchBox
        );

        return toolBar;
    }

    public void setOnOpenLocalFolder(Runnable action) {
        this.onOpenLocalFolderAction = action;
        btnOpenFolder.setOnAction(e -> action.run());
        menuOpenFolder.setOnAction(e -> action.run());
    }

    public void setOnUpload(Runnable action) {
        btnUpload.setOnAction(e -> action.run());
        menuUpload.setOnAction(e -> action.run());
    }

    public void setOnNewFolder(Runnable action) {
        btnNewFolder.setOnAction(e -> action.run());
    }

    public void setOnOpenConsole(Runnable action) {
        btnConsole.setOnAction(e -> action.run());
        menuConsole.setOnAction(e -> action.run());
    }

    public void setOnToggleInspector(Runnable action) {
        btnInspector.setOnAction(e -> action.run());
        menuToggleInspector.setOnAction(e -> action.run());
    }

    public void setOnOpenCommandPalette(Runnable action) {
        searchBox.setOnMouseClicked(e -> action.run());
        searchField.setOnMouseClicked(e -> action.run());
    }

    private Button createIconButton(String text, Feather icon) {
        Button button = new Button(text);
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(14);
        fontIcon.setIconColor(Color.web("#334155"));
        button.setGraphic(fontIcon);
        button.getStyleClass().add("quick-action-button");
        return button;
    }
}
