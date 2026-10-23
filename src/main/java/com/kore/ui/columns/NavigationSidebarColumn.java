package com.kore.ui.columns;

import com.kore.model.LegalContext;
import com.kore.model.RepositoryNode;
import com.kore.model.RepositoryQuota;
import com.kore.model.TaxonomyNode;
import com.kore.model.UserProfile;
import com.kore.state.DocumentViewModel;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
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
 * Columna Unificada de Navegación (Izquierda).
 * Integra verticalmente en una sola columna:
 * - Sección Superior: Árbol de Repositorio (okm:root)
 * - Sección Inferior: Contexto Jurídico & Árbol de Taxonomía Documental con contadores
 * - Base Fija: Widget de Cuota de Almacenamiento y Tarjeta de Usuario Activo.
 */
@Component
public class NavigationSidebarColumn extends VBox {

    private final DocumentViewModel viewModel;
    private Runnable onOpenDirectoryRequested;

    // Árboles
    private TreeView<RepositoryNode> repoTreeView;
    private TreeView<TaxonomyNode> taxoTreeView;

    private TreeItem<RepositoryNode> rootRepoItem;
    private TreeItem<TaxonomyNode> rootTaxoItem;

    private boolean repoExpanded = true;

    public void setOnOpenDirectoryRequested(Runnable action) {
        this.onOpenDirectoryRequested = action;
    }

    public NavigationSidebarColumn(DocumentViewModel viewModel) {
        this.viewModel = viewModel;
        getStyleClass().add("column-container");
        setSpacing(0);

        // SplitPane vertical interno para redimensionar proporcionalmente Repositorio y Taxonomía
        SplitPane verticalSplitPane = new SplitPane();
        verticalSplitPane.setOrientation(Orientation.VERTICAL);
        verticalSplitPane.getStyleClass().add("enterprise-split-pane");
        VBox.setVgrow(verticalSplitPane, Priority.ALWAYS);

        // 1. Sección Superior: Árbol de Repositorio
        VBox repoSection = createRepositorySection();

        // 2. Sección Inferior: Contexto Jurídico + Taxonomía
        VBox taxoSection = createTaxonomySection();

        verticalSplitPane.getItems().addAll(repoSection, taxoSection);
        verticalSplitPane.setDividerPositions(0.48);

        // 3. Base fija con Cuota de Repositorio y Tarjeta de Usuario
        VBox footerSection = createFooterSection();

        getChildren().addAll(verticalSplitPane, footerSection);
    }

    // ========================================================
    // SECCIÓN 1: ÁRBOL DE REPOSITORIO
    // ========================================================
    private VBox createRepositorySection() {
        VBox box = new VBox(0);
        box.getStyleClass().add("column-section-box");
        VBox.setVgrow(box, Priority.ALWAYS);

        // Cabecera de Repositorio
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(7, 10, 7, 10));
        header.getStyleClass().add("column-header");

        FontIcon folderIcon = new FontIcon(Feather.FOLDER);
        folderIcon.setIconSize(13);
        folderIcon.setIconColor(Color.web("#2563EB"));

        Label title = new Label("ÁRBOL DE REPOSITORIO");
        title.getStyleClass().add("column-header-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnOpenDir = new Button();
        FontIcon openDirIcon = new FontIcon(Feather.FOLDER_PLUS);
        openDirIcon.setIconSize(12);
        btnOpenDir.setGraphic(openDirIcon);
        btnOpenDir.setTooltip(new Tooltip("Abrir carpeta local de la PC (Ctrl+O)"));
        btnOpenDir.getStyleClass().addAll("icon-tool-btn", "flat");
        btnOpenDir.setOnAction(e -> {
            if (onOpenDirectoryRequested != null) {
                onOpenDirectoryRequested.run();
            }
        });

        Button btnResetDemo = new Button();
        FontIcon resetIcon = new FontIcon(Feather.DATABASE);
        resetIcon.setIconSize(12);
        btnResetDemo.setGraphic(resetIcon);
        btnResetDemo.setTooltip(new Tooltip("Alternar a Repositorio Demo (okm:root)"));
        btnResetDemo.getStyleClass().addAll("icon-tool-btn", "flat");
        btnResetDemo.setOnAction(e -> {
            if (viewModel.isLocalFsMode()) {
                viewModel.switchBackToMockRepository();
            } else if (onOpenDirectoryRequested != null) {
                onOpenDirectoryRequested.run();
            }
        });

        Button btnRefresh = new Button();
        FontIcon refreshIcon = new FontIcon(Feather.ROTATE_CW);
        refreshIcon.setIconSize(12);
        btnRefresh.setGraphic(refreshIcon);
        btnRefresh.setTooltip(new Tooltip("Recargar repositorio"));
        btnRefresh.getStyleClass().addAll("icon-tool-btn", "flat");
        btnRefresh.setOnAction(e -> {
            if (viewModel.isLocalFsMode()) {
                Path curr = viewModel.getCurrentLocalDirectory();
                if (curr != null) {
                    viewModel.scanAndLoadFolder(curr);
                    loadLocalFolderTree(curr);
                }
            } else {
                refreshRepoTree();
            }
        });

        Button btnToggle = new Button();
        FontIcon toggleIcon = new FontIcon(Feather.MINUS_SQUARE);
        toggleIcon.setIconSize(12);
        btnToggle.setGraphic(toggleIcon);
        btnToggle.setTooltip(new Tooltip("Colapsar / expandir todo"));
        btnToggle.getStyleClass().addAll("icon-tool-btn", "flat");
        btnToggle.setOnAction(e -> {
            repoExpanded = !repoExpanded;
            toggleExpandAll(rootRepoItem, repoExpanded);
            toggleIcon.setIconCode(repoExpanded ? Feather.MINUS_SQUARE : Feather.PLUS_SQUARE);
        });

        header.getChildren().addAll(folderIcon, title, spacer, btnOpenDir, btnResetDemo, btnRefresh, btnToggle);

        // TreeView de Repositorio
        repoTreeView = new TreeView<>();
        repoTreeView.getStyleClass().add("enterprise-tree-view");
        VBox.setVgrow(repoTreeView, Priority.ALWAYS);

        // Escuchar cambios de modo Local FS vs Demo
        viewModel.localFsModeProperty().addListener((obs, oldV, isLocal) -> {
            if (isLocal) {
                title.setText("EXPLORADOR LOCAL");
                folderIcon.setIconCode(Feather.HARD_DRIVE);
                loadLocalFolderTree(viewModel.getCurrentLocalDirectory());
            } else {
                title.setText("ÁRBOL DE REPOSITORIO");
                folderIcon.setIconCode(Feather.FOLDER);
                refreshRepoTree();
            }
        });

        viewModel.currentLocalDirectoryProperty().addListener((obs, oldDir, newDir) -> {
            if (viewModel.isLocalFsMode() && newDir != null) {
                loadLocalFolderTree(newDir);
            }
        });

        refreshRepoTree();

        repoTreeView.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(RepositoryNode node, boolean empty) {
                super.updateItem(node, empty);
                if (empty || node == null) {
                    setText(null);
                    setGraphic(null);
                    getStyleClass().removeAll("repo-cell-active", "repo-cell-inactive");
                } else {
                    HBox cellBox = new HBox(6);
                    cellBox.setAlignment(Pos.CENTER_LEFT);
                    cellBox.setPadding(new Insets(2, 4, 2, 4));

                    FontIcon icon = new FontIcon();
                    icon.setIconSize(13);
                    if (node.isFolder()) {
                        icon.setIconCode(getTreeItem().isExpanded() ? Feather.FOLDER_MINUS : Feather.FOLDER);
                        icon.setIconColor(Color.web("#2563EB"));
                    } else {
                        icon.setIconCode(Feather.FILE_TEXT);
                        icon.setIconColor(Color.web("#64748B"));
                    }

                    Label label = new Label(node.getName());
                    label.getStyleClass().add("repo-tree-node-label");

                    cellBox.getChildren().addAll(icon, label);

                    if (isSelected()) {
                        getStyleClass().add("repo-cell-active");
                        getStyleClass().remove("repo-cell-inactive");
                    } else {
                        getStyleClass().add("repo-cell-inactive");
                        getStyleClass().remove("repo-cell-active");
                    }

                    setGraphic(cellBox);
                    setText(null);
                }
            }
        });

        repoTreeView.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem != null && newItem.getValue() != null) {
                RepositoryNode node = newItem.getValue();
                if (viewModel.isLocalFsMode()) {
                    try {
                        Path p = Paths.get(node.getPath());
                        viewModel.scanAndLoadFolder(p);
                    } catch (Exception ignored) {}
                } else {
                    viewModel.selectRepositoryNode(node);
                }
            }
        });

        box.getChildren().addAll(header, repoTreeView);
        return box;
    }

    private void loadLocalFolderTree(Path rootDir) {
        if (rootDir == null) return;
        rootRepoItem = buildLazyDirectoryTreeItem(rootDir);
        rootRepoItem.setExpanded(true);
        repoTreeView.setRoot(rootRepoItem);
        repoTreeView.setShowRoot(true);
        repoTreeView.getSelectionModel().select(rootRepoItem);
    }

    private TreeItem<RepositoryNode> buildLazyDirectoryTreeItem(Path dir) {
        RepositoryNode node = viewModel.getLocalFileSystemService().createRepositoryNodeForDirectory(dir);
        return new TreeItem<>(node) {
            private boolean isFirstChildren = true;

            @Override
            public boolean isLeaf() {
                return false;
            }

            @Override
            public ObservableList<TreeItem<RepositoryNode>> getChildren() {
                if (isFirstChildren) {
                    isFirstChildren = false;
                    List<Path> subdirs = viewModel.getLocalFileSystemService().listSubdirectories(dir);
                    ObservableList<TreeItem<RepositoryNode>> children = super.getChildren();
                    for (Path subdir : subdirs) {
                        children.add(buildLazyDirectoryTreeItem(subdir));
                    }
                }
                return super.getChildren();
            }
        };
    }

    private void refreshRepoTree() {
        RepositoryNode rootNode = viewModel.repositoryTreeRoot();
        rootRepoItem = buildRepoTreeItem(rootNode);
        rootRepoItem.setExpanded(true);

        repoTreeView.setRoot(rootRepoItem);
        repoTreeView.setShowRoot(true);

        selectInitialRepoNode(rootRepoItem);
    }

    private TreeItem<RepositoryNode> buildRepoTreeItem(RepositoryNode node) {
        TreeItem<RepositoryNode> item = new TreeItem<>(node);
        for (RepositoryNode child : node.getChildren()) {
            TreeItem<RepositoryNode> childItem = buildRepoTreeItem(child);
            if (child.getName().contains("Contratos Vigentes")) {
                childItem.setExpanded(true);
            }
            item.getChildren().add(childItem);
        }
        return item;
    }

    private void selectInitialRepoNode(TreeItem<RepositoryNode> current) {
        if (current == null) return;
        if ("Acuerdos Marco 2025".equals(current.getValue().getName()) ||
            "Pólizas de Cumplimiento".equals(current.getValue().getName())) {
            repoTreeView.getSelectionModel().select(current);
            return;
        }
        for (TreeItem<RepositoryNode> child : current.getChildren()) {
            selectInitialRepoNode(child);
        }
    }

    private void toggleExpandAll(TreeItem<RepositoryNode> item, boolean expand) {
        if (item == null) return;
        item.setExpanded(expand);
        for (TreeItem<RepositoryNode> child : item.getChildren()) {
            toggleExpandAll(child, expand);
        }
    }

    // ========================================================
    // SECCIÓN 2: CONTEXTO JURÍDICO & TAXONOMÍA
    // ========================================================
    private VBox createTaxonomySection() {
        VBox box = new VBox(0);
        box.getStyleClass().add("column-section-box");
        VBox.setVgrow(box, Priority.ALWAYS);

        // Tarjeta de Contexto Jurídico
        VBox contextCard = new VBox(3);
        contextCard.setPadding(new Insets(8, 10, 8, 10));
        contextCard.getStyleClass().add("legal-context-card");

        LegalContext context = viewModel.legalContext();

        HBox topRow = new HBox(6);
        topRow.setAlignment(Pos.CENTER_LEFT);

        FontIcon gavelIcon = new FontIcon(Feather.BRIEFCASE);
        gavelIcon.setIconSize(12);
        gavelIcon.setIconColor(Color.web("#2563EB"));

        Label title = new Label("Contexto Jurídico");
        title.getStyleClass().add("context-card-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label codeBadge = new Label(context.code());
        codeBadge.getStyleClass().addAll("enterprise-badge", "badge-primary");

        topRow.getChildren().addAll(gavelIcon, title, spacer, codeBadge);

        Label deptLabel = new Label(context.department());
        deptLabel.getStyleClass().add("context-card-dept");

        Label respLabel = new Label("Responsable: " + context.responsible() + " • " + context.location());
        respLabel.getStyleClass().add("context-card-detail");

        contextCard.getChildren().addAll(topRow, deptLabel, respLabel);

        // Cabecera de Taxonomía
        HBox taxoHeader = new HBox(8);
        taxoHeader.setAlignment(Pos.CENTER_LEFT);
        taxoHeader.setPadding(new Insets(7, 10, 7, 10));
        taxoHeader.getStyleClass().add("column-header");

        Label taxoTitle = new Label("TAXONOMÍA DOCUMENTAL");
        taxoTitle.getStyleClass().add("column-header-title");

        Region taxoSpacer = new Region();
        HBox.setHgrow(taxoSpacer, Priority.ALWAYS);

        Button btnFilter = new Button();
        FontIcon filterIcon = new FontIcon(Feather.FILTER);
        filterIcon.setIconSize(12);
        btnFilter.setGraphic(filterIcon);
        btnFilter.setTooltip(new Tooltip("Filtrar taxonomía"));
        btnFilter.getStyleClass().addAll("icon-tool-btn", "flat");

        taxoHeader.getChildren().addAll(taxoTitle, taxoSpacer, btnFilter);

        // TreeView de Taxonomía
        taxoTreeView = new TreeView<>();
        taxoTreeView.getStyleClass().add("enterprise-tree-view");
        VBox.setVgrow(taxoTreeView, Priority.ALWAYS);

        TaxonomyNode rootNode = viewModel.taxonomyTreeRoot();
        rootTaxoItem = buildTaxoTreeItem(rootNode);
        rootTaxoItem.setExpanded(true);

        taxoTreeView.setRoot(rootTaxoItem);
        taxoTreeView.setShowRoot(true);

        taxoTreeView.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(TaxonomyNode item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    getStyleClass().removeAll("taxo-cell-active", "taxo-cell-inactive");
                } else {
                    HBox row = new HBox(6);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.setPadding(new Insets(2, 4, 2, 4));

                    FontIcon icon = new FontIcon();
                    icon.setIconSize(12);
                    if (item.getChildren().isEmpty()) {
                        icon.setIconCode(Feather.FILE_TEXT);
                        icon.setIconColor(Color.web("#64748B"));
                    } else {
                        icon.setIconCode(getTreeItem().isExpanded() ? Feather.FOLDER_MINUS : Feather.FOLDER);
                        icon.setIconColor(Color.web("#2563EB"));
                    }

                    Label nameLabel = new Label(item.getName());
                    nameLabel.getStyleClass().add("taxonomy-node-name");

                    Region sp = new Region();
                    HBox.setHgrow(sp, Priority.ALWAYS);

                    row.getChildren().addAll(icon, nameLabel, sp);

                    if (item.hasBadge()) {
                        Label countBadge = new Label(String.valueOf(item.getCountBadge()));
                        countBadge.getStyleClass().addAll("enterprise-badge", "badge-counter");
                        row.getChildren().add(countBadge);
                    }

                    if (isSelected()) {
                        getStyleClass().add("taxo-cell-active");
                        getStyleClass().remove("taxo-cell-inactive");
                    } else {
                        getStyleClass().add("taxo-cell-inactive");
                        getStyleClass().remove("taxo-cell-active");
                    }

                    setGraphic(row);
                    setText(null);
                }
            }
        });

        taxoTreeView.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem != null && newItem.getValue() != null) {
                viewModel.selectTaxonomyNode(newItem.getValue());
            }
        });

        selectInitialTaxoNode(rootTaxoItem);

        box.getChildren().addAll(contextCard, taxoHeader, taxoTreeView);
        return box;
    }

    private TreeItem<TaxonomyNode> buildTaxoTreeItem(TaxonomyNode node) {
        TreeItem<TaxonomyNode> item = new TreeItem<>(node);
        for (TaxonomyNode child : node.getChildren()) {
            TreeItem<TaxonomyNode> childItem = buildTaxoTreeItem(child);
            childItem.setExpanded(true);
            item.getChildren().add(childItem);
        }
        return item;
    }

    private void selectInitialTaxoNode(TreeItem<TaxonomyNode> current) {
        if (current == null) return;
        if ("Contratos_2025".equals(current.getValue().getName())) {
            taxoTreeView.getSelectionModel().select(current);
            return;
        }
        for (TreeItem<TaxonomyNode> child : current.getChildren()) {
            selectInitialTaxoNode(child);
        }
    }

    // ========================================================
    // SECCIÓN 3: CUOTA & USUARIO (BASE FIJA)
    // ========================================================
    private VBox createFooterSection() {
        VBox footer = new VBox(0);

        // Widget de Cuota
        VBox quotaBox = new VBox(5);
        quotaBox.setPadding(new Insets(8, 10, 8, 10));
        quotaBox.getStyleClass().add("quota-widget-container");

        RepositoryQuota quota = viewModel.repositoryQuota();

        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label quotaTitle = new Label("Cuota Repositorio");
        quotaTitle.getStyleClass().add("quota-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label percentageLabel = new Label(quota.getPercentageFormatted());
        percentageLabel.getStyleClass().add("quota-percentage");

        titleRow.getChildren().addAll(quotaTitle, spacer, percentageLabel);

        ProgressBar progressBar = new ProgressBar(quota.percentage() / 100.0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(5);
        progressBar.getStyleClass().add("quota-progress-bar");

        HBox statsRow = new HBox(8);
        statsRow.setAlignment(Pos.CENTER_LEFT);

        Label usedLabel = new Label(quota.getUsageLabel());
        usedLabel.getStyleClass().add("quota-stat-label");

        Region statSpacer = new Region();
        HBox.setHgrow(statSpacer, Priority.ALWAYS);

        Label allocLabel = new Label(quota.getAllocatedLabel());
        allocLabel.getStyleClass().add("quota-stat-label");

        statsRow.getChildren().addAll(usedLabel, statSpacer, allocLabel);
        quotaBox.getChildren().addAll(titleRow, progressBar, statsRow);

        // Actualizar cuota reactivamente cuando cambie el disco/directorio
        viewModel.repositoryQuotaProperty().addListener((obs, oldQ, newQ) -> {
            if (newQ != null) {
                percentageLabel.setText(newQ.getPercentageFormatted());
                progressBar.setProgress(newQ.percentage() / 100.0);
                usedLabel.setText(newQ.getUsageLabel());
                allocLabel.setText(newQ.getAllocatedLabel());
            }
        });

        // Tarjeta de Usuario
        HBox userCard = new HBox(8);
        userCard.setAlignment(Pos.CENTER_LEFT);
        userCard.setPadding(new Insets(8, 10, 8, 10));
        userCard.getStyleClass().add("user-profile-card");

        UserProfile profile = viewModel.currentUser();

        StackPane avatarPane = new StackPane();
        Circle avatarCircle = new Circle(13, Color.web("#DBEAFE"));
        Label initialsLabel = new Label(profile.avatarInitials());
        initialsLabel.setStyle("-fx-font-size: 9.5px; -fx-font-weight: bold; -fx-text-fill: #1D4ED8;");
        avatarPane.getChildren().addAll(avatarCircle, initialsLabel);

        VBox infoBox = new VBox(1);
        Label nameLabel = new Label(profile.username());
        nameLabel.getStyleClass().add("user-card-name");

        Label roleLabel = new Label(profile.role());
        roleLabel.getStyleClass().add("user-card-role");

        infoBox.getChildren().addAll(nameLabel, roleLabel);

        Region uSpacer = new Region();
        HBox.setHgrow(uSpacer, Priority.ALWAYS);

        Button btnLogout = new Button();
        FontIcon logoutIcon = new FontIcon(Feather.LOG_OUT);
        logoutIcon.setIconSize(13);
        logoutIcon.setIconColor(Color.web("#64748B"));
        btnLogout.setGraphic(logoutIcon);
        btnLogout.setTooltip(new Tooltip("Cerrar sesión"));
        btnLogout.getStyleClass().addAll("icon-tool-btn", "flat");

        userCard.getChildren().addAll(avatarPane, infoBox, uSpacer, btnLogout);

        footer.getChildren().addAll(quotaBox, userCard);
        return footer;
    }
}
