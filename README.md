# KORE

Kore es un gestor documental con énfasis en el análisis de datos, brinda un camino sencillo para integrar el flujo del analista con el resto del negocio.

## Frontend Empresarial Nativo

Sistema Empresarial de Gestión Documental de alta densidad visual, desarrollado de forma nativa con **Java 21 LTS**, **JavaFX**, **Spring Boot 3.3.4**, **AtlantaFX (Primer Light & Primer Dark)** e **Ikonli (Feather Icons)**.

---

## 🚀 Características Principales

* **Arquitectura MVVM Reactiva:** Separación estricta de responsabilidades entre vistas independientes, modelos de dominio y un `DocumentViewModel` con propiedades reactivas (`Property`, `ObservableValue`, `FilteredList`, `SortedList`).
* **Diseño Empresarial de Alta Densidad:**
  * **Barra Superior y Menús:** Acciones rápidas (Subir, Nuevo, Check-out, Check-in, Aprobar Flujo, Consola Daemons F12, Inspector F11, Switch de Modo Oscuro).
  * **Switch Modo Claro / Modo Oscuro:** Integración nativa con AtlantaFX *Primer Light* y *Primer Dark* (paleta GitHub Dark).
  * **Columna de Navegación Unificada:** Árbol de Repositorio superior y Taxonomía Documental inferior con *SplitPane* vertical redimensionable, métricas de cuota de disco en tiempo real y tarjeta de sesión de usuario.
  * **Lista Central de Documentos Activos:** Ordenamiento por fecha/nombre/tamaño, buscador y filtros avanzados, cálculo dinámico de búfer WAL y badges de versión tipo píldora.
  * **Panel Inspector Desplegable:** Se oculta automáticamente cuando no hay selección y se monta al seleccionar un archivo; incluye pestañas de clasificación TRD, entidades OCR, automatizaciones, telemetría sparkline, historial de versiones y pista forense de auditoría.
  * **Explorador de Archivos Local de la PC:** Permite abrir cualquier carpeta de la máquina (`Ctrl+O`), navegar carpetas con carga diferida (*lazy loading*), calcular hashes SHA-256 criptográficos reales y previsualizar código/texto o abrir en aplicaciones nativas de Windows.
  * **Escala de Zoom Dinámica:** Ajuste fino de 50% a 150% vía slider o teclado (`Ctrl +`, `Ctrl -`, `Ctrl 0`).

---

## 🛠 Requisitos del Sistema

* **Java:** JDK 21 LTS o superior (ej. Microsoft OpenJDK 21 o Eclipse Temurin 21).
* **Gestor de Construcción:** Maven 3.8+.
* **Sistema Operativo:** Windows 10 / 11 (soporte multiplataforma en Linux/macOS).

---

## 💻 Ejecución

### Opción 1: Script Directo (Windows)
Haz doble clic sobre `run.bat` o ejecútalo en la terminal:
```powershell
.\run.bat
```

### Opción 2: Maven
```powershell
mvn spring-boot:run
```

---

## ⌨ Atajos de Teclado Globales

| Atajo | Función |
| :--- | :--- |
| <kbd>Ctrl</kbd> + <kbd>K</kbd> | Command Palette / Búsqueda Global |
| <kbd>Ctrl</kbd> + <kbd>O</kbd> | Abrir Carpeta Local (Explorador de PC) |
| <kbd>Ctrl</kbd> + <kbd>U</kbd> | Diálogo de Carga de Archivos |
| <kbd>F11</kbd> | Alternar Visibilidad del Panel Inspector |
| <kbd>F12</kbd> | Abrir Consola de Daemons Solr & OCR |
| <kbd>Esc</kbd> | Deseleccionar Documento Activo |
| <kbd>Ctrl</kbd> + <kbd>+</kbd> / <kbd>-</kbd> | Aumentar / Disminuir Escala de Zoom |
| <kbd>Ctrl</kbd> + <kbd>0</kbd> | Restablecer Escala de Zoom al 100% |
