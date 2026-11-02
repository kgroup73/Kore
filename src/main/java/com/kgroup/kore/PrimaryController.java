package com.kgroup.kore;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

public class PrimaryController implements Initializable {

    private String path = "/home/kevin/Documentos";

    @FXML
    private Button primaryButton;

    @FXML
    private ListView<KoreFile> filesList;

    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }

    @FXML
    private void showFiles() {
        File resource = new File(path);
        ObservableList<KoreFile> ol = FXCollections.observableArrayList();
        long count = 0;

        for (File file : resource.listFiles()) {
            ol.add(new KoreFile(++count, file.getName(), file.getPath()));
        }

        filesList.setItems(ol);
        filesList.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends KoreFile> observable, KoreFile oldValue, KoreFile newValue) -> {
            System.out.println(observable.getValue().path);
        });
    }

    private String emojiToString(String emoji) {
        return new String(emoji.getBytes(StandardCharsets.UTF_8), Charset.forName("UTF-8"));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        showFiles();
    }

}
