package com.kore.ui.dialogs;

import com.kore.model.DocumentNode;
import com.kore.model.RepositoryNode;
import com.kore.state.DocumentViewModel;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.List;

/**
 * Diálogo flotante tipo Command Palette (Spotlight / Raycast) invocado mediante Ctrl+K.
 * Permite búsqueda y navegación instantánea por nodo, expediente, nombre de archivo o hash SHA-256.
 */
public class CommandPaletteDialog {

    public record PaletteItem(String title, String subtitle, String badge, String type, Object target) {}

    public static void show(Stage ownerStage, DocumentViewModel viewModel) {
        Stage stage = new Stage();
        stage.initOwner(ownerStage);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);

        VBox root = new VBox(0);
        root.setPrefWidth(580);
        root.setMaxHeight(400);
        root.setStyle(
                "-fx-background-color: #FFFFFF;" +
                "-fx-background-radius: 8px;" +
                "-fx-border-color: #CBD5E1;" +
                "-fx-border-radius: 8px;" +
                "-fx-border-width: 1px;"
        );

        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(25);
        dropShadow.setOffsetY(8);
        dropShadow.setColor(Color.rgb(15, 23, 42, 0.28));
        root.setEffect(dropShadow);

        // Cabecera con campo de búsqueda
        HBox searchRow = new HBox(10);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        searchRow.setPadding(new Insets(12, 14, 12, 14));
        searchRow.setStyle("-fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");

        FontIcon searchIcon = new FontIcon(Feather.SEARCH);
        searchIcon.setIconSize(16);
        searchIcon.setIconColor(Color.web("#2563EB"));

        TextField searchInput = new TextField();
        searchInput.setPromptText("Buscar nodo, expediente o UUID en Kore (Ctrl+K)...");
        searchInput.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-border-color: transparent;" +
                "-fx-font-size: 13px;" +
                "-fx-text-fill: #0F172A;"
        );
        HBox.setHgrow(searchInput, Priority.ALWAYS);

        Label escBadge = new Label("ESC");
        escBadge.setStyle(
                "-fx-font-size: 10px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #64748B;" +
                "-fx-background-color: #F1F5F9;" +
                "-fx-padding: 2 6 2 6;" +
                "-fx-background-radius: 4px;"
        );

        searchRow.getChildren().addAll(searchIcon, searchInput, escBadge);

        // Lista de resultados
        ListView<PaletteItem> resultsList = new ListView<>();
        resultsList.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(resultsList, Priority.ALWAYS);

        // Construir catálogo de búsqueda completo
        List<PaletteItem> allItems = new ArrayList<>();

        for (DocumentNode doc : viewModel.getAllDocuments()) {
            allItems.add(new PaletteItem(
                    doc.getName(),
                    "UUID: " + doc.getUuid() + " • " + doc.getExpediente(),
                    doc.getVersion(),
                    "DOCUMENT",
                    doc
            ));
        }

        addRepoItems(viewModel.repositoryTreeRoot(), allItems);

        resultsList.getItems().addAll(allItems);

        // Filtrado dinámico al teclear
        searchInput.textProperty().addListener((obs, oldV, query) -> {
            resultsList.getItems().clear();
            if (query == null || query.isBlank()) {
                resultsList.getItems().addAll(allItems);
            } else {
                String q = query.toLowerCase().trim();
                for (PaletteItem item : allItems) {
                    if (item.title().toLowerCase().contains(q) || item.subtitle().toLowerCase().contains(q)) {
                        resultsList.getItems().add(item);
                    }
                }
            }
            if (!resultsList.getItems().isEmpty()) {
                resultsList.getSelectionModel().select(0);
            }
        });

        // Renderizado de elementos
        resultsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(PaletteItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    HBox row = new HBox(10);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.setPadding(new Insets(6, 10, 6, 10));

                    FontIcon icon = new FontIcon("DOCUMENT".equals(item.type()) ? Feather.FILE_TEXT : Feather.FOLDER);
                    icon.setIconSize(14);
                    icon.setIconColor("DOCUMENT".equals(item.type()) ? Color.web("#DC2626") : Color.web("#2563EB"));

                    VBox textBlock = new VBox(1);
                    Label title = new Label(item.title());
                    title.setStyle("-fx-font-weight: 600; -fx-font-size: 11.5px; -fx-text-fill: #0F172A;");

                    Label sub = new Label(item.subtitle());
                    sub.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 10px; -fx-text-fill: #64748B;");

                    textBlock.getChildren().addAll(title, sub);

                    Region sp = new Region();
                    HBox.setHgrow(sp, Priority.ALWAYS);

                    Label badge = new Label(item.badge());
                    badge.setStyle(
                            "-fx-background-color: #EFF6FF;" +
                            "-fx-text-fill: #2563EB;" +
                            "-fx-border-color: #BFDBFE;" +
                            "-fx-border-radius: 9999px;" +
                            "-fx-background-radius: 9999px;" +
                            "-fx-padding: 1 6 1 6;" +
                            "-fx-font-size: 9.5px;" +
                            "-fx-font-weight: bold;"
                    );

                    row.getChildren().addAll(icon, textBlock, sp, badge);

                    if (isSelected()) {
                        row.setStyle("-fx-background-color: #EFF6FF; -fx-background-radius: 4px;");
                    } else {
                        row.setStyle("-fx-background-color: transparent;");
                    }

                    setGraphic(row);
                    setText(null);
                }
            }
        });

        // Navegación por teclado
        Runnable executeSelection = () -> {
            PaletteItem selected = resultsList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (selected.target() instanceof DocumentNode doc) {
                    viewModel.selectDocument(doc);
                    viewModel.systemStatusMessageProperty().set("Navegado a documento: " + doc.getName());
                } else if (selected.target() instanceof RepositoryNode repo) {
                    viewModel.selectRepositoryNode(repo);
                    viewModel.systemStatusMessageProperty().set("Navegado a carpeta: " + repo.getName());
                }
                stage.close();
            }
        };

        searchInput.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DOWN) {
                resultsList.getSelectionModel().selectNext();
                resultsList.scrollTo(resultsList.getSelectionModel().getSelectedIndex());
                e.consume();
            } else if (e.getCode() == KeyCode.UP) {
                resultsList.getSelectionModel().selectPrevious();
                resultsList.scrollTo(resultsList.getSelectionModel().getSelectedIndex());
                e.consume();
            } else if (e.getCode() == KeyCode.ENTER) {
                executeSelection.run();
            } else if (e.getCode() == KeyCode.ESCAPE) {
                stage.close();
            }
        });

        resultsList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1 || e.getClickCount() == 2) {
                executeSelection.run();
            }
        });

        // Pie de ayuda de atajos
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(8, 14, 8, 14));
        footer.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-width: 1 0 0 0; -fx-background-radius: 0 0 8px 8px;");

        Label hint1 = new Label("↑↓ Navegar");
        hint1.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748B;");

        Label hint2 = new Label("↵ Seleccionar");
        hint2.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748B;");

        Label hint3 = new Label("ESC Cerrar");
        hint3.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748B;");

        footer.getChildren().addAll(hint1, hint2, hint3);

        root.getChildren().addAll(searchRow, resultsList, footer);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);

        // Centrar respecto a la ventana padre
        stage.setOnShown(e -> {
            stage.setX(ownerStage.getX() + (ownerStage.getWidth() - root.getPrefWidth()) / 2.0);
            stage.setY(ownerStage.getY() + 110);
            Platform.runLater(searchInput::requestFocus);
        });

        stage.show();
    }

    private static void addRepoItems(RepositoryNode node, List<PaletteItem> list) {
        if (node == null) return;
        list.add(new PaletteItem(
                node.getName(),
                "Ruta: " + node.getPath() + " • UUID: " + node.getUuid(),
                "CARPETA",
                "FOLDER",
                node
        ));
        for (RepositoryNode child : node.getChildren()) {
            addRepoItems(child, list);
        }
    }
}
