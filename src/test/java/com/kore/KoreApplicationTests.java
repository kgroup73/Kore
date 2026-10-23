package com.kore;

import com.kore.state.DocumentViewModel;
import com.kore.ui.MainLayout;
import com.kore.ui.columns.ActiveDocumentsColumn;
import com.kore.ui.columns.DocumentInspectorColumn;
import com.kore.ui.columns.NavigationSidebarColumn;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class KoreApplicationTests {

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // Ya iniciado
        }
    }

    @Autowired
    private DocumentViewModel viewModel;

    @Autowired
    private MainLayout mainLayout;

    @Autowired
    private NavigationSidebarColumn navigationSidebarColumn;

    @Autowired
    private ActiveDocumentsColumn activeDocumentsColumn;

    @Autowired
    private DocumentInspectorColumn documentInspectorColumn;

    @Test
    void contextLoadsAndBeansAreValid() {
        assertNotNull(viewModel);
        assertNotNull(mainLayout);
        assertNotNull(navigationSidebarColumn);
        assertNotNull(activeDocumentsColumn);
        assertNotNull(documentInspectorColumn);

        assertEquals(4, viewModel.getAllDocuments().size());

        // Al inicio ningún documento está seleccionado, y el Inspector debe estar oculto
        assertNull(viewModel.getSelectedDocument());
        assertFalse(mainLayout.isInspectorVisible());

        // Al seleccionar un documento, el Inspector se despliega automáticamente
        viewModel.selectDocument(viewModel.getAllDocuments().get(0));
        assertNotNull(viewModel.getSelectedDocument());
        assertTrue(mainLayout.isInspectorVisible());
        assertEquals("CONTRATO-MARCO-CLOUD-2025-V2.pdf", viewModel.getSelectedDocument().getName());
        assertEquals("v2.4", viewModel.getSelectedDocument().getVersion());
        assertEquals("Bloqueado", viewModel.getSelectedDocument().getStatus());

        // Test de reactividad de Check-in
        viewModel.toggleCheckInStatus();
        assertEquals("Disponible", viewModel.getSelectedDocument().getStatus());

        viewModel.toggleCheckInStatus();
        assertEquals("Bloqueado", viewModel.getSelectedDocument().getStatus());

        // Test de conmutación de visibilidad del Inspector (F11)
        assertTrue(mainLayout.isInspectorVisible());
        mainLayout.toggleInspector();
        assertFalse(mainLayout.isInspectorVisible());
        mainLayout.toggleInspector();
        assertTrue(mainLayout.isInspectorVisible());

        // Al deseleccionar (documento = null), el Inspector se oculta automáticamente
        viewModel.selectDocument(null);
        assertNull(viewModel.getSelectedDocument());
        assertFalse(mainLayout.isInspectorVisible());

        // Test de Escala de Zoom Reactiva
        assertEquals(100.0, viewModel.getZoomLevel());
        viewModel.setZoomLevel(120.0);
        assertEquals(120.0, viewModel.getZoomLevel());
        assertTrue(mainLayout.getRoot().getStyle().contains("13.80px"));

        viewModel.resetZoom();
        assertEquals(100.0, viewModel.getZoomLevel());
        assertTrue(mainLayout.getRoot().getStyle().contains("11.50px"));

        // Test de Modo Oscuro Reactivo
        assertFalse(viewModel.isDarkMode());
        assertFalse(mainLayout.getRoot().getStyleClass().contains("dark-theme"));

        viewModel.setDarkMode(true);
        assertTrue(viewModel.isDarkMode());
        assertTrue(mainLayout.getRoot().getStyleClass().contains("dark-theme"));

        viewModel.setDarkMode(false);
        assertFalse(viewModel.isDarkMode());
        assertFalse(mainLayout.getRoot().getStyleClass().contains("dark-theme"));

        // Test de Integración con Sistema de Archivos Local (PC)
        assertFalse(viewModel.isLocalFsMode());
        java.nio.file.Path projectDir = java.nio.file.Paths.get(System.getProperty("user.dir"));
        viewModel.openLocalDirectory(projectDir);

        assertTrue(viewModel.isLocalFsMode());
        assertNotNull(viewModel.getCurrentLocalDirectory());
        assertTrue(viewModel.getAllDocuments().size() > 0);
        assertTrue(viewModel.breadcrumbPathProperty().get().contains(projectDir.toAbsolutePath().toString()));
        assertNotNull(viewModel.repositoryQuota());

        // Test de retorno a Repositorio Demo
        viewModel.switchBackToMockRepository();
        assertFalse(viewModel.isLocalFsMode());
        assertEquals(4, viewModel.getAllDocuments().size());
    }
}
