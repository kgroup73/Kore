package com.kore;

import atlantafx.base.theme.PrimerLight;
import com.kore.ui.MainLayout;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class KoreApplication extends Application {

    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        System.out.println("[KORE] Starting Spring Application Context...");
        this.springContext = new SpringApplicationBuilder(KoreBootstrap.class)
                .headless(false)
                .run(getParameters().getRaw().toArray(new String[0]));
        System.out.println("[KORE] Spring Application Context started successfully!");
    }

    @Override
    public void start(Stage primaryStage) {
        System.out.println("[KORE] Initializing JavaFX Primary Stage...");
        // Aplicar tema visual AtlantaFX Primer Light
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        MainLayout mainLayout = springContext.getBean(MainLayout.class);
        Scene scene = new Scene(mainLayout.getRoot(), 1440, 880);

        // Cargar hoja de estilos de alta densidad personalizada
        String cssUrl = getClass().getResource("/styles/kore.css") != null
                ? getClass().getResource("/styles/kore.css").toExternalForm()
                : null;
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl);
        }

        primaryStage.setTitle("Kore Enterprise DMS - [Contratos y Proveedores 2025] PROD-CORP-01");
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(700);
        primaryStage.setScene(scene);
        primaryStage.show();
        System.out.println("[KORE] Primary Stage visible and running!");
    }

    @Override
    public void stop() {
        if (springContext != null) {
            springContext.close();
        }
        Platform.exit();
    }
}
