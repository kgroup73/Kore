module com.kgroup.kore {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.kgroup.kore to javafx.fxml;
    exports com.kgroup.kore;
}
