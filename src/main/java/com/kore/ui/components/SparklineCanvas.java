package com.kore.ui.components;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

import java.util.List;

/**
 * Componente gráfico ultra-ligero para dibujar sparklines con área rellena en gradiente y línea suavizada.
 */
public class SparklineCanvas extends Canvas {

    public SparklineCanvas(double width, double height) {
        super(width, height);
    }

    public void render(List<Double> points) {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        gc.clearRect(0, 0, w, h);

        if (points == null || points.size() < 2) {
            return;
        }

        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (double v : points) {
            if (v < min) min = v;
            if (v > max) max = v;
        }
        if (min == max) {
            min = 0;
        }

        int n = points.size();
        double stepX = w / (n - 1);
        double paddingY = 4.0;
        double usableH = h - (paddingY * 2);

        double[] xCoords = new double[n];
        double[] yCoords = new double[n];

        for (int i = 0; i < n; i++) {
            xCoords[i] = i * stepX;
            double norm = (points.get(i) - min) / (max - min);
            yCoords[i] = h - paddingY - (norm * usableH);
        }

        // 1. Dibujar relleno con gradiente translúcido
        LinearGradient fillGrad = new LinearGradient(
                0, 0, 0, h, false, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#2563EB", 0.25)),
                new Stop(1.0, Color.web("#2563EB", 0.02))
        );
        gc.setFill(fillGrad);
        gc.beginPath();
        gc.moveTo(xCoords[0], h);
        for (int i = 0; i < n; i++) {
            gc.lineTo(xCoords[i], yCoords[i]);
        }
        gc.lineTo(xCoords[n - 1], h);
        gc.closePath();
        gc.fill();

        // 2. Dibujar trazo de línea principal
        gc.setStroke(Color.web("#2563EB"));
        gc.setLineWidth(1.8);
        gc.beginPath();
        gc.moveTo(xCoords[0], yCoords[0]);
        for (int i = 1; i < n; i++) {
            gc.lineTo(xCoords[i], yCoords[i]);
        }
        gc.stroke();

        // 3. Resaltar punto final
        gc.setFill(Color.web("#2563EB"));
        gc.fillOval(xCoords[n - 1] - 3, yCoords[n - 1] - 3, 6, 6);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1.0);
        gc.strokeOval(xCoords[n - 1] - 3, yCoords[n - 1] - 3, 6, 6);
    }
}
