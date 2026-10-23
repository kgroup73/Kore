package com.kore.ui.columns;

import com.kore.model.DocumentNode;
import com.kore.state.DocumentViewModel;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.stereotype.Component;

/**
 * Columna 3 - Lista de Documentos Activos (Centro).
 * Proporciona listado de alta densidad de expedientes con ordenamiento reactivo,
 * barra de filtros desplegable, selección sincronizada con el Inspector (Col 4)
 * y barra de resumen inferior con métricas de búfer y registro WAL.
 */
@Component
public class ActiveDocumentsColumn extends VBox {

    private final DocumentViewModel viewModel;
    private final VBox filterBarContainer = new VBox();
    private boolean filterBarVisible = false;

    public ActiveDocumentsColumn(DocumentViewModel viewModel) {
        this.viewModel = viewModel;
        getStyleClass().add("column-container");
        setSpacing(0);

        getChildren().addAll(
                createHeader(),
                createFilterBar(),
                createColumnTitles(),
                createDocumentListView(),
                createFooterSummary()
        );
    }

    private VBox createHeader() {
        VBox headerContainer = new VBox(6);
        headerContainer.setPadding(new Insets(8, 12, 8, 12));
        headerContainer.getStyleClass().add("column-header-stacked");

        HBox topRow = new HBox(8);
        topRow.setAlignment(Pos.CENTER_LEFT);

        FontIcon gridIcon = new FontIcon(Feather.GRID);
        gridIcon.setIconSize(14);
        gridIcon.setIconColor(Color.web("#2563EB"));

        Label title = new Label("Documentos Activos");
        title.getStyleClass().add("active-docs-title");

        Label countBadge = new Label();
        countBadge.textProperty().bind(
                Bindings.concat(Bindings.size(viewModel.getSortedDocuments()), " expedientes")
        );
        countBadge.getStyleClass().add("expedientes-badge");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnFilters = new Button("Filtros Avanzados");
        FontIcon filterIcon = new FontIcon(Feather.SLIDERS);
        filterIcon.setIconSize(12);
        btnFilters.setGraphic(filterIcon);
        btnFilters.getStyleClass().add("table-tool-button");
        btnFilters.setTooltip(new Tooltip("Mostrar / ocultar barra de filtros"));
        btnFilters.setOnAction(e -> toggleFilterBar());

        MenuButton sortMenu = new MenuButton("Orden: Fec. Modificación (Desc)");
        sortMenu.getStyleClass().add("sort-dropdown-button");

        MenuItem sortDate = new MenuItem("Fecha Modificación (Desc)");
        sortDate.setOnAction(e -> {
            viewModel.sortByDateDesc();
            sortMenu.setText("Orden: Fec. Modificación (Desc)");
        });

        MenuItem sortName = new MenuItem("Nombre (A-Z)");
        sortName.setOnAction(e -> {
            viewModel.sortByNameAsc();
            sortMenu.setText("Orden: Nombre (A-Z)");
        });

        MenuItem sortSize = new MenuItem("Tamaño (Mayor a Menor)");
        sortSize.setOnAction(e -> {
            viewModel.sortBySizeDesc();
            sortMenu.setText("Orden: Tamaño (Mayor)");
        });

        sortMenu.getItems().addAll(sortDate, sortName, sortSize);

        topRow.getChildren().addAll(gridIcon, title, countBadge, spacer, btnFilters, sortMenu);
        headerContainer.getChildren().add(topRow);
        return headerContainer;
    }

    private VBox createFilterBar() {
        filterBarContainer.setPadding(new Insets(6, 12, 6, 12));
        filterBarContainer.getStyleClass().add("filter-bar-container");
        filterBarContainer.setVisible(false);
        filterBarContainer.setManaged(false);

        HBox filterRow = new HBox(8);
        filterRow.setAlignment(Pos.CENTER_LEFT);

        Label lbl = new Label("Filtrar nombre o hash:");
        lbl.setStyle("-fx-font-size: 10.5px; -fx-text-fill: #475569;");

        TextField txtFilter = new TextField();
        txtFilter.setPromptText("Buscar en lista activa...");
        txtFilter.setPrefWidth(220);
        txtFilter.setStyle("-fx-font-size: 11px; -fx-padding: 3 6 3 6;");
        txtFilter.textProperty().addListener((obs, oldV, newV) -> {
            viewModel.getFilteredDocuments().setPredicate(doc -> {
                if (newV == null || newV.isBlank()) return true;
                String lower = newV.toLowerCase();
                return doc.getName().toLowerCase().contains(lower) ||
                       doc.getSha256().toLowerCase().contains(lower) ||
                       doc.getUuid().toLowerCase().contains(lower);
            });
            viewModel.recalculateSelectionSummary();
        });

        Button btnClear = new Button("Limpiar");
        btnClear.setStyle("-fx-font-size: 10.5px; -fx-padding: 3 8 3 8;");
        btnClear.setOnAction(e -> txtFilter.clear());

        filterRow.getChildren().addAll(lbl, txtFilter, btnClear);
        filterBarContainer.getChildren().add(filterRow);
        return filterBarContainer;
    }

    private void toggleFilterBar() {
        filterBarVisible = !filterBarVisible;
        filterBarContainer.setVisible(filterBarVisible);
        filterBarContainer.setManaged(filterBarVisible);
    }

    private HBox createColumnTitles() {
        HBox bar = new HBox(8);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(6, 12, 6, 12));
        bar.getStyleClass().add("document-list-subheader");

        CheckBox masterCheckBox = new CheckBox();
        masterCheckBox.setSelected(true);
        masterCheckBox.setTooltip(new Tooltip("Seleccionar / deseleccionar todos"));
        masterCheckBox.setOnAction(e -> viewModel.selectAll(masterCheckBox.isSelected()));

        Label colDoc = new Label("Documento / Identificador de Nodo");
        colDoc.getStyleClass().add("list-subheader-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label colVer = new Label("Versión");
        colVer.getStyleClass().add("list-subheader-title");

        bar.getChildren().addAll(masterCheckBox, colDoc, spacer, colVer);
        return bar;
    }

    private ListView<DocumentNode> createDocumentListView() {
        ListView<DocumentNode> listView = new ListView<>(viewModel.getSortedDocuments());
        listView.getStyleClass().add("document-custom-list-view");
        VBox.setVgrow(listView, Priority.ALWAYS);

        // Selección sincronizada con el ViewModel (soporta tanto seleccionar como deseleccionar)
        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            viewModel.selectDocument(newVal);
        });

        // Permitir deseleccionar con tecla Escape o clic en área vacía
        listView.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                listView.getSelectionModel().clearSelection();
            }
        });
        listView.setOnMousePressed(e -> {
            if (e.getTarget() instanceof ListView<?>) {
                listView.getSelectionModel().clearSelection();
            }
        });

        // Doble clic para abrir el Visor Integrado de Documentos (Previewer)
        listView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                DocumentNode selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    javafx.stage.Stage owner = null;
                    if (getScene() != null && getScene().getWindow() instanceof javafx.stage.Stage st) {
                        owner = st;
                    }
                    com.kore.ui.dialogs.DocumentPreviewDialog.show(owner, selected, viewModel);
                }
            }
        });

        // Sincronizar selección bidireccional desde el ViewModel
        viewModel.selectedDocumentProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                if (listView.getSelectionModel().getSelectedItem() != null) {
                    listView.getSelectionModel().clearSelection();
                }
            } else if (listView.getSelectionModel().getSelectedItem() != newVal) {
                listView.getSelectionModel().select(newVal);
            }
        });
        if (viewModel.getSelectedDocument() != null) {
            listView.getSelectionModel().select(viewModel.getSelectedDocument());
        }

        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(DocumentNode item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    getStyleClass().removeAll("doc-cell-active", "doc-cell-inactive");
                } else {
                    HBox row = new HBox(10);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.setPadding(new Insets(8, 10, 8, 10));

                    // Checkbox de selección individual
                    CheckBox checkBox = new CheckBox();
                    checkBox.selectedProperty().bindBidirectional(item.selectedProperty());

                    // Icono de tipo de archivo con color contextual
                    FontIcon fileIcon = createFileIcon(item.getExtension());

                    // Bloque de títulos (Nombre + Subtítulo monoespaciado)
                    VBox textBlock = new VBox(2);
                    Label nameLabel = new Label();
                    nameLabel.textProperty().bind(item.nameProperty());
                    nameLabel.getStyleClass().add("document-row-name");

                    Label subLabel = new Label("UUID: " + item.getUuid() + " • Hash: " + item.getSha256());
                    subLabel.getStyleClass().add("document-row-subtext");

                    textBlock.getChildren().addAll(nameLabel, subLabel);

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    // Badge de versión tipo píldora
                    Label verBadge = new Label();
                    verBadge.textProperty().bind(item.versionProperty());
                    verBadge.getStyleClass().addAll("enterprise-badge", "badge-version");

                    row.getChildren().addAll(checkBox, fileIcon, textBlock, spacer, verBadge);

                    // Estilo de selección activa
                    if (isSelected()) {
                        getStyleClass().add("doc-cell-active");
                        getStyleClass().remove("doc-cell-inactive");
                    } else {
                        getStyleClass().add("doc-cell-inactive");
                        getStyleClass().remove("doc-cell-active");
                    }

                    setGraphic(row);
                    setText(null);
                }
            }
        });

        return listView;
    }

    private FontIcon createFileIcon(String extension) {
        FontIcon icon = new FontIcon();
        icon.setIconSize(18);
        if ("pdf".equalsIgnoreCase(extension)) {
            icon.setIconCode(Feather.FILE_TEXT);
            icon.setIconColor(Color.web("#DC2626")); // Rojo para PDF
        } else if ("docx".equalsIgnoreCase(extension)) {
            icon.setIconCode(Feather.FILE_TEXT);
            icon.setIconColor(Color.web("#2563EB")); // Azul para DOCX
        } else if ("xml".equalsIgnoreCase(extension)) {
            icon.setIconCode(Feather.CODE);
            icon.setIconColor(Color.web("#059669")); // Verde para XML
        } else {
            icon.setIconCode(Feather.FILE);
            icon.setIconColor(Color.web("#64748B"));
        }
        return icon;
    }

    private HBox createFooterSummary() {
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(6, 12, 6, 12));
        footer.getStyleClass().add("document-list-footer");

        Label selectedSummary = new Label();
        selectedSummary.textProperty().bind(
                Bindings.concat(viewModel.selectedCountProperty(), " fila(s) seleccionada(s)")
        );
        selectedSummary.getStyleClass().add("list-footer-text");

        Separator sep1 = new Separator();

        Label bufferSummary = new Label();
        bufferSummary.textProperty().bind(
                Bindings.concat(viewModel.totalBufferFormattedProperty(), " total en búfer")
        );
        bufferSummary.getStyleClass().add("list-footer-text");

        Separator sep2 = new Separator();

        Label mimeLabel = new Label("MIME: --");
        if (viewModel.getSelectedDocument() != null) {
            mimeLabel.textProperty().bind(
                    Bindings.concat("MIME: ", viewModel.getSelectedDocument().mimeTypeProperty())
            );
        }
        viewModel.selectedDocumentProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                mimeLabel.textProperty().bind(Bindings.concat("MIME: ", newVal.mimeTypeProperty()));
            } else {
                mimeLabel.textProperty().unbind();
                mimeLabel.setText("MIME: --");
            }
        });
        mimeLabel.getStyleClass().add("list-footer-text");

        Separator sep3 = new Separator();

        Label walLabel = new Label("Registro en WAL: Inactivo");
        if (viewModel.getSelectedDocument() != null) {
            walLabel.textProperty().bind(viewModel.getSelectedDocument().walRecordProperty());
        }
        viewModel.selectedDocumentProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                walLabel.textProperty().bind(newVal.walRecordProperty());
            } else {
                walLabel.textProperty().unbind();
                walLabel.setText("Registro en WAL: Inactivo");
            }
        });
        walLabel.getStyleClass().add("list-footer-text-wal");

        footer.getChildren().addAll(selectedSummary, sep1, bufferSummary, sep2, mimeLabel, sep3, walLabel);
        return footer;
    }
}
