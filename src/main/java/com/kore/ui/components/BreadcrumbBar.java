package com.kore.ui.components;

import com.kore.state.DocumentViewModel;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.stereotype.Component;

/**
 * Sub-barra de navegación con migas de pan (Breadcrumbs), identificador de nodo y badges de estado.
 */
@Component
public class BreadcrumbBar extends HBox {

    private final DocumentViewModel viewModel;

    public BreadcrumbBar(DocumentViewModel viewModel) {
        this.viewModel = viewModel;
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(4, 12, 4, 12));
        setSpacing(8);
        getStyleClass().add("breadcrumb-bar");

        FontIcon folderIcon = new FontIcon(Feather.FOLDER);
        folderIcon.setIconSize(13);
        folderIcon.setIconColor(Color.web("#2563EB"));

        Label pathLabel = new Label();
        pathLabel.textProperty().bind(viewModel.breadcrumbPathProperty());
        pathLabel.getStyleClass().add("breadcrumb-path-label");

        Label uuidLabel = new Label();
        uuidLabel.textProperty().bind(
                Bindings.concat("(Nodo UUID: ", viewModel.breadcrumbUuidProperty(), ")")
        );
        uuidLabel.getStyleClass().add("breadcrumb-uuid-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Badge 1: Índice Solr 8.11 OK
        HBox solrBadge = new HBox(5);
        solrBadge.setAlignment(Pos.CENTER);
        solrBadge.getStyleClass().addAll("enterprise-badge", "badge-success");
        Circle solrDot = new Circle(3.5, Color.web("#10B981"));
        Label solrLabel = new Label();
        solrLabel.textProperty().bind(viewModel.solrIndexBadgeProperty());
        solrBadge.getChildren().addAll(solrDot, solrLabel);

        // Badge 2: Paralegal Access
        HBox accessBadge = new HBox(4);
        accessBadge.setAlignment(Pos.CENTER);
        accessBadge.getStyleClass().addAll("enterprise-badge", "badge-neutral");
        FontIcon shieldIcon = new FontIcon(Feather.SHIELD);
        shieldIcon.setIconSize(11);
        shieldIcon.setIconColor(Color.web("#475569"));
        Label accessLabel = new Label();
        accessLabel.textProperty().bind(viewModel.roleAccessBadgeProperty());
        accessBadge.getChildren().addAll(shieldIcon, accessLabel);

        getChildren().addAll(folderIcon, pathLabel, uuidLabel, spacer, solrBadge, accessBadge);
    }
}
