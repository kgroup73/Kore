package com.kore.ui.dialogs;

import com.kore.model.DocumentNode;
import com.kore.state.DocumentViewModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Visor Integrado de Documentos (Previewer).
 * Soporta visualización simulada de alta fidelidad para:
 * - PDF: Paginación A4 con renderizado de marcas de agua, cláusulas y resaltado interactivo de entidades OCR.
 * - XML: Visor con sintaxis coloreada para Facturación Electrónica DIAN UBL 2.1 y validación de CUFE.
 * - DOCX: Previsualización de acuerdos legales y SLAs con formato tipográfico.
 */
public class DocumentPreviewDialog {

    public static void show(Stage ownerStage, DocumentNode doc, DocumentViewModel viewModel) {
        if (doc == null) return;

        Stage stage = new Stage();
        stage.initOwner(ownerStage);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Kore DMS - Visor Integrado: " + doc.getName());
        stage.setWidth(960);
        stage.setHeight(700);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #F1F5F9;");

        // ---- Barra Superior de Herramientas del Visor ----
        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(8, 14, 8, 14));
        topBar.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");

        // Badge de extensión
        Label formatBadge = new Label(doc.getExtension().toUpperCase());
        formatBadge.setStyle(
                "-fx-background-color: " + getFormatColor(doc.getExtension()) + ";" +
                "-fx-text-fill: #FFFFFF;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 10px;" +
                "-fx-padding: 2 6 2 6;" +
                "-fx-background-radius: 4px;"
        );

        Label titleLabel = new Label(doc.getName());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #0F172A;");

        Label statusBadge = new Label(doc.getStatus());
        statusBadge.setStyle(
                "-fx-background-color: " + ("Bloqueado".equalsIgnoreCase(doc.getStatus()) ? "#FEE2E2" : "#DCFCE7") + ";" +
                "-fx-text-fill: " + ("Bloqueado".equalsIgnoreCase(doc.getStatus()) ? "#DC2626" : "#16A34A") + ";" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 10px;" +
                "-fx-padding: 1 7 1 7;" +
                "-fx-background-radius: 9999px;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Controles de Paginación para PDF / DOCX
        HBox pagerBox = new HBox(6);
        pagerBox.setAlignment(Pos.CENTER);

        Button btnPrev = new Button();
        btnPrev.setGraphic(new FontIcon(Feather.CHEVRON_LEFT));
        btnPrev.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #CBD5E1; -fx-padding: 2 6 2 6;");

        Label pageLabel = new Label("Página 1 de 6");
        pageLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: 600; -fx-text-fill: #475569;");

        Button btnNext = new Button();
        btnNext.setGraphic(new FontIcon(Feather.CHEVRON_RIGHT));
        btnNext.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #CBD5E1; -fx-padding: 2 6 2 6;");

        pagerBox.getChildren().addAll(btnPrev, pageLabel, btnNext);

        // Toggle de resaltado OCR
        ToggleButton btnOcrToggle = new ToggleButton("Resaltar OCR (Solr)");
        FontIcon ocrIcon = new FontIcon(Feather.TAG);
        ocrIcon.setIconSize(12);
        btnOcrToggle.setGraphic(ocrIcon);
        btnOcrToggle.setSelected(true);
        btnOcrToggle.setStyle("-fx-font-size: 10.5px; -fx-padding: 3 8 3 8;");

        Button btnDownload = new Button("Descargar");
        FontIcon dlIcon = new FontIcon(Feather.DOWNLOAD);
        dlIcon.setIconSize(12);
        btnDownload.setGraphic(dlIcon);
        btnDownload.setStyle("-fx-font-size: 10.5px; -fx-padding: 3 8 3 8;");
        btnDownload.setOnAction(e -> {
            viewModel.systemStatusMessageProperty().set("Descargado en disco local: " + doc.getName());
            stage.close();
        });

        Button btnClose = new Button();
        FontIcon closeIcon = new FontIcon(Feather.X);
        closeIcon.setIconSize(14);
        btnClose.setGraphic(closeIcon);
        btnClose.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 4px;");
        btnClose.setOnAction(e -> stage.close());

        Path realPath = null;
        try {
            Path p = Paths.get(doc.getUuid());
            if (Files.isRegularFile(p)) {
                realPath = p;
            }
        } catch (Exception ignored) {}

        if (realPath != null) {
            Button btnOpenExternal = new Button("Abrir en Windows");
            FontIcon extIcon = new FontIcon(Feather.EXTERNAL_LINK);
            extIcon.setIconSize(12);
            btnOpenExternal.setGraphic(extIcon);
            btnOpenExternal.setStyle("-fx-font-size: 10.5px; -fx-padding: 3 8 3 8;");
            Path targetP = realPath;
            btnOpenExternal.setOnAction(e -> {
                viewModel.getLocalFileSystemService().openInSystemDefault(targetP);
                viewModel.systemStatusMessageProperty().set("Abierto en Windows: " + doc.getName());
            });
            topBar.getChildren().addAll(
                    formatBadge, titleLabel, statusBadge, spacer,
                    pagerBox, new Separator(), btnOcrToggle, btnOpenExternal, btnDownload, btnClose
            );
        } else {
            topBar.getChildren().addAll(
                    formatBadge, titleLabel, statusBadge, spacer,
                    pagerBox, new Separator(), btnOcrToggle, btnDownload, btnClose
            );
        }

        // ---- Contenedor Central según Tipo de Documento ----
        Node viewerContent;
        if (realPath != null && isTextFile(doc.getExtension())) {
            viewerContent = createRealTextFileViewer(doc, realPath, viewModel);
            pagerBox.setVisible(false);
            btnOcrToggle.setVisible(false);
        } else if ("xml".equalsIgnoreCase(doc.getExtension())) {
            viewerContent = createXmlViewer(doc);
            pagerBox.setVisible(false);
            btnOcrToggle.setVisible(false);
        } else if ("docx".equalsIgnoreCase(doc.getExtension())) {
            viewerContent = createDocxViewer(doc, btnOcrToggle);
        } else {
            viewerContent = createPdfViewer(doc, btnOcrToggle);
        }

        VBox.setVgrow(viewerContent, Priority.ALWAYS);

        // ---- Barra Inferior de Metadatos de Seguridad ----
        HBox bottomBar = new HBox(12);
        bottomBar.setAlignment(Pos.CENTER_LEFT);
        bottomBar.setPadding(new Insets(6, 14, 6, 14));
        bottomBar.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E2E8F0; -fx-border-width: 1 0 0 0;");

        FontIcon shieldIcon = new FontIcon(Feather.SHIELD);
        shieldIcon.setIconSize(12);
        shieldIcon.setIconColor(Color.web("#059669"));

        Label secText = new Label("Sandbox Seguro Kore • Firma Digital Verificada • Hash: " + doc.getSha256());
        secText.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 10.5px; -fx-text-fill: #475569;");

        Region bSpacer = new Region();
        HBox.setHgrow(bSpacer, Priority.ALWAYS);

        Label sizeText = new Label("Tamaño: " + doc.getSizeFormatted() + " • WAL Checkpoint OK");
        sizeText.setStyle("-fx-font-size: 10.5px; -fx-text-fill: #64748B;");

        bottomBar.getChildren().addAll(shieldIcon, secText, bSpacer, sizeText);

        root.getChildren().addAll(topBar, viewerContent, bottomBar);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    private static Node createPdfViewer(DocumentNode doc, ToggleButton ocrToggle) {
        StackPane centerContainer = new StackPane();
        centerContainer.setPadding(new Insets(20));

        VBox paper = new VBox(14);
        paper.setMaxWidth(620);
        paper.setStyle(
                "-fx-background-color: #FFFFFF;" +
                "-fx-padding: 36px 44px 36px 44px;" +
                "-fx-border-color: #CBD5E1;" +
                "-fx-border-width: 1px;"
        );

        DropShadow shadow = new DropShadow();
        shadow.setRadius(18);
        shadow.setColor(Color.rgb(15, 23, 42, 0.12));
        paper.setEffect(shadow);

        // Encabezado de la hoja de contrato
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        Label corpHeader = new Label("KORE ENTERPRISE DMS — INSTRUMENTO CONTRACTUAL PRIVADO");
        corpHeader.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: #94A3B8; -fx-letter-spacing: 1px;");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Label trdTag = new Label(doc.getTrdMetadata() != null ? doc.getTrdMetadata().codigoTrd() : "TRD 210.04");
        trdTag.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: #2563EB; -fx-background-color: #EFF6FF; -fx-padding: 2 6 2 6; -fx-background-radius: 4px;");
        header.getChildren().addAll(corpHeader, sp, trdTag);

        Label contractTitle = new Label("CONTRATO MARCO DE PRESTACIÓN DE SERVICIOS EN LA NUBE");
        contractTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: 800; -fx-text-fill: #0F172A; -fx-padding: 10 0 4 0;");

        Label intro = new Label(
                "Entre los suscritos a saber, por una parte K-GROUP ENTERPRISES S.A.S., identificada con NIT 830.098.321-4, " +
                "y por la otra parte el PROVEEDOR SELECCIONADO, han convenido celebrar el presente Acuerdo Marco de Cooperación Tecnológica:"
        );
        intro.setWrapText(true);
        intro.setStyle("-fx-font-size: 11px; -fx-line-spacing: 3px; -fx-text-fill: #334155;");

        // Bloque de Cláusula Primera con Resaltado OCR interactivo
        VBox clause1 = new VBox(6);
        clause1.setStyle("-fx-background-color: #F8FAFC; -fx-padding: 10px; -fx-border-color: #E2E8F0; -fx-border-radius: 4px;");

        Label c1Title = new Label("CLÁUSULA PRIMERA — IDENTIFICACIÓN DE LAS PARTES Y CUANTÍA (ENTIDADES OCR)");
        c1Title.setStyle("-fx-font-weight: bold; -fx-font-size: 10.5px; -fx-text-fill: #1E293B;");

        HBox ocrTag1 = createOcrPill("NIT Proveedor: 900.245.112-1 (AWS Cloud LatAm)", "#10B981", "#DCFCE7");
        HBox ocrTag2 = createOcrPill("Cuantía Total: $185,000.00 USD", "#2563EB", "#DBEAFE");
        HBox ocrTag3 = createOcrPill("Vigencia: 01/01/2025 al 31/12/2027", "#7C3AED", "#EDE9FE");

        ocrToggle.selectedProperty().addListener((obs, oldV, showOcr) -> {
            ocrTag1.setVisible(showOcr);
            ocrTag1.setManaged(showOcr);
            ocrTag2.setVisible(showOcr);
            ocrTag2.setManaged(showOcr);
            ocrTag3.setVisible(showOcr);
            ocrTag3.setManaged(showOcr);
        });

        clause1.getChildren().addAll(c1Title, ocrTag1, ocrTag2, ocrTag3);

        Label clause2 = new Label(
                "CLÁUSULA SEGUNDA — NIVELES DE SERVICIO (SLA): El proveedor garantizará una disponibilidad operativa " +
                "del 99.95% durante el periodo anual de vigencia. Todo incidente crítico Tipo 1 será mitigado en un plazo máximo " +
                "de 120 minutos conforme a la matriz de contingencia tecnológica anexa."
        );
        clause2.setWrapText(true);
        clause2.setStyle("-fx-font-size: 11px; -fx-line-spacing: 3px; -fx-text-fill: #334155;");

        Label clause3 = new Label(
                "CLÁUSULA TERCERA — CUSTODIA Y RETENCIÓN DOCUMENTAL: El presente expediente se archivará bajo el código " +
                "TRD 210.04 en Conservación Total (CT), requiriendo custodia en archivo central por un periodo mínimo de 5 años " +
                "y registro inmutable en Write-Ahead Log de Kore."
        );
        clause3.setWrapText(true);
        clause3.setStyle("-fx-font-size: 11px; -fx-line-spacing: 3px; -fx-text-fill: #334155;");

        // Bloque de Firma Digital
        HBox signatures = new HBox(30);
        signatures.setAlignment(Pos.CENTER);
        signatures.setPadding(new Insets(24, 0, 10, 0));

        VBox sig1 = createSignatureBlock("Laura Restrepo", "Legal Ops Senior & Oficial de Cumplimiento", "Firma Digital SHA-256 Validada (2026-10-22)");
        VBox sig2 = createSignatureBlock("Representante Legal", "AWS Cloud LatAm Services", "Firma Electrónica Avanzada DIAN (Certicámara)");

        signatures.getChildren().addAll(sig1, sig2);

        paper.getChildren().addAll(header, contractTitle, intro, clause1, clause2, clause3, signatures);
        centerContainer.getChildren().add(paper);

        ScrollPane scrollPane = new ScrollPane(centerContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #F1F5F9; -fx-background-color: #F1F5F9; -fx-border-color: transparent;");
        return scrollPane;
    }

    private static Node createXmlViewer(DocumentNode doc) {
        VBox container = new VBox(8);
        container.setPadding(new Insets(14));
        container.setStyle("-fx-background-color: #0F172A;");

        HBox cufeHeader = new HBox(8);
        cufeHeader.setAlignment(Pos.CENTER_LEFT);
        cufeHeader.setStyle("-fx-background-color: #1E293B; -fx-padding: 8 12 8 12; -fx-border-color: #334155; -fx-border-radius: 4px;");

        FontIcon checkIcon = new FontIcon(Feather.CHECK_CIRCLE);
        checkIcon.setIconColor(Color.web("#10B981"));
        checkIcon.setIconSize(14);

        Label cufeLabel = new Label("Factura Electrónica UBL 2.1 — CUFE: 8b4a79c1e2f3d5a0c9b8a7f6e3b0c442 (Validado DIAN OK)");
        cufeLabel.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #38BDF8;");

        cufeHeader.getChildren().addAll(checkIcon, cufeLabel);

        TextFlow xmlFlow = new TextFlow();
        xmlFlow.setPadding(new Insets(10));
        xmlFlow.setStyle("-fx-background-color: #0B0F19;");

        appendXmlLine(xmlFlow, "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
        appendXmlTag(xmlFlow, "<Invoice", " xmlns=\"urn:oasis:names:specification:ubl:schema:xsd:Invoice-2\" xmlns:cac=\"urn:oasis:names:...\"");
        appendXmlLeaf(xmlFlow, "  <cbc:UBLVersionID>", "2.1", "</cbc:UBLVersionID>");
        appendXmlLeaf(xmlFlow, "  <cbc:CustomizationID>", "10", "</cbc:CustomizationID>");
        appendXmlLeaf(xmlFlow, "  <cbc:ProfileID>", "DIAN 2.1: Factura Electrónica de Venta", "</cbc:ProfileID>");
        appendXmlLeaf(xmlFlow, "  <cbc:ID>", "SERV-4891", "</cbc:ID>");
        appendXmlLeaf(xmlFlow, "  <cbc:UUID schemeName=\"CUFE-SHA384\">", "8b4a79c1e2f3d5a0c9b8a7f6e3b0c44299b8a7f6", "</cbc:UUID>");
        appendXmlLeaf(xmlFlow, "  <cbc:IssueDate>", "2025-10-01", "</cbc:IssueDate>");
        appendXmlLeaf(xmlFlow, "  <cbc:IssueTime>", "10:30:00-05:00", "</cbc:IssueTime>");
        appendXmlTag(xmlFlow, "  <cac:AccountingSupplierParty>", "");
        appendXmlLeaf(xmlFlow, "    <cbc:CompanyID schemeAgencyID=\"195\">", "901.445.678-0", "</cbc:CompanyID>");
        appendXmlLeaf(xmlFlow, "    <cbc:RegistrationName>", "TELECOM CORP LATIN AMERICA S.A.", "</cbc:RegistrationName>");
        appendXmlTag(xmlFlow, "  </cac:AccountingSupplierParty>", "");
        appendXmlTag(xmlFlow, "  <cac:LegalMonetaryTotal>", "");
        appendXmlLeaf(xmlFlow, "    <cbc:LineExtensionAmount currencyID=\"USD\">", "12850.50", "</cbc:LineExtensionAmount>");
        appendXmlLeaf(xmlFlow, "    <cbc:PayableAmount currencyID=\"USD\">", "12850.50", "</cbc:PayableAmount>");
        appendXmlTag(xmlFlow, "  </cac:LegalMonetaryTotal>", "");
        appendXmlTag(xmlFlow, "</Invoice>", "");

        ScrollPane scroll = new ScrollPane(xmlFlow);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #0B0F19; -fx-background-color: #0B0F19; -fx-border-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        container.getChildren().addAll(cufeHeader, scroll);
        return container;
    }

    private static Node createDocxViewer(DocumentNode doc, ToggleButton ocrToggle) {
        return createPdfViewer(doc, ocrToggle);
    }

    private static HBox createOcrPill(String text, String strokeColor, String bgColor) {
        HBox box = new HBox(6);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                "-fx-border-color: " + strokeColor + ";" +
                "-fx-border-radius: 4px;" +
                "-fx-padding: 3 8 3 8;"
        );
        FontIcon tagIcon = new FontIcon(Feather.TAG);
        tagIcon.setIconSize(11);
        tagIcon.setIconColor(Color.web(strokeColor));

        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 10.5px; -fx-font-weight: bold; -fx-text-fill: " + strokeColor + ";");
        box.getChildren().addAll(tagIcon, lbl);
        return box;
    }

    private static VBox createSignatureBlock(String name, String role, String status) {
        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-border-color: #CBD5E1; -fx-border-width: 1 0 0 0; -fx-padding: 8 20 0 20;");

        Label nameLbl = new Label(name);
        nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #0F172A;");

        Label roleLbl = new Label(role);
        roleLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748B;");

        Label statusLbl = new Label("✓ " + status);
        statusLbl.setStyle("-fx-font-size: 9.5px; -fx-font-weight: bold; -fx-text-fill: #059669;");

        box.getChildren().addAll(nameLbl, roleLbl, statusLbl);
        return box;
    }

    private static void appendXmlLine(TextFlow flow, String line) {
        Text t = new Text(line + "\n");
        t.setFont(Font.font("Consolas", 11));
        t.setFill(Color.web("#94A3B8"));
        flow.getChildren().add(t);
    }

    private static void appendXmlTag(TextFlow flow, String tag, String attrs) {
        Text tTag = new Text(tag);
        tTag.setFont(Font.font("Consolas", 11));
        tTag.setFill(Color.web("#38BDF8"));

        Text tAttr = new Text(attrs + "\n");
        tAttr.setFont(Font.font("Consolas", 11));
        tAttr.setFill(Color.web("#A78BFA"));

        flow.getChildren().addAll(tTag, tAttr);
    }

    private static void appendXmlLeaf(TextFlow flow, String openTag, String val, String closeTag) {
        Text tOpen = new Text(openTag);
        tOpen.setFont(Font.font("Consolas", 11));
        tOpen.setFill(Color.web("#38BDF8"));

        Text tVal = new Text(val);
        tVal.setFont(Font.font("Consolas", 11));
        tVal.setFill(Color.web("#34D399"));

        Text tClose = new Text(closeTag + "\n");
        tClose.setFont(Font.font("Consolas", 11));
        tClose.setFill(Color.web("#38BDF8"));

        flow.getChildren().addAll(tOpen, tVal, tClose);
    }

    private static String getFormatColor(String ext) {
        if ("pdf".equalsIgnoreCase(ext)) return "#DC2626";
        if ("docx".equalsIgnoreCase(ext) || "doc".equalsIgnoreCase(ext)) return "#2563EB";
        if ("xml".equalsIgnoreCase(ext) || "html".equalsIgnoreCase(ext)) return "#059669";
        if ("json".equalsIgnoreCase(ext) || "js".equalsIgnoreCase(ext) || "ts".equalsIgnoreCase(ext)) return "#D97706";
        if ("java".equalsIgnoreCase(ext)) return "#E11D48";
        return "#64748B";
    }

    private static boolean isTextFile(String ext) {
        if (ext == null) return false;
        String e = ext.toLowerCase();
        return e.equals("txt") || e.equals("java") || e.equals("xml") || e.equals("json") ||
               e.equals("md") || e.equals("csv") || e.equals("html") || e.equals("css") ||
               e.equals("properties") || e.equals("sql") || e.equals("yml") || e.equals("yaml") ||
               e.equals("log") || e.equals("bat") || e.equals("ps1");
    }

    private static Node createRealTextFileViewer(DocumentNode doc, Path path, DocumentViewModel viewModel) {
        VBox container = new VBox(8);
        container.setPadding(new Insets(14));
        container.setStyle("-fx-background-color: #0F172A;");

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #1E293B; -fx-padding: 8 12 8 12; -fx-border-color: #334155; -fx-border-radius: 4px;");

        FontIcon fileIcon = new FontIcon(Feather.FILE_TEXT);
        fileIcon.setIconColor(Color.web("#38BDF8"));
        fileIcon.setIconSize(14);

        Label pathLabel = new Label("Archivo Local: " + path.toAbsolutePath());
        pathLabel.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #E2E8F0;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button btnCopy = new Button("Copiar Ruta");
        btnCopy.setStyle("-fx-background-color: #334155; -fx-text-fill: #F1F5F9; -fx-font-size: 10px; -fx-padding: 2 6 2 6; -fx-cursor: hand;");
        btnCopy.setOnAction(e -> {
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(path.toAbsolutePath().toString());
            clipboard.setContent(content);
            viewModel.systemStatusMessageProperty().set("Ruta copiada al portapapeles: " + path.toAbsolutePath());
        });

        header.getChildren().addAll(fileIcon, pathLabel, sp, btnCopy);

        String previewText = viewModel.getLocalFileSystemService().readFilePreview(path, 1200);

        javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea(previewText);
        textArea.setEditable(false);
        textArea.setStyle(
                "-fx-font-family: 'Consolas', 'Courier New', monospace;" +
                "-fx-font-size: 11.5px;" +
                "-fx-text-fill: #E2E8F0;" +
                "-fx-control-inner-background: #0B0F19;" +
                "-fx-border-color: #334155;"
        );
        VBox.setVgrow(textArea, Priority.ALWAYS);

        container.getChildren().addAll(header, textArea);
        return container;
    }
}
