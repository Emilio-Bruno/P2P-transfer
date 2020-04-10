module com.scriptisle {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.scriptisle to javafx.fxml;
    exports com.scriptisle;
}