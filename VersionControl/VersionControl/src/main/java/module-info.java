module com.example.versioncontrol {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.versioncontrol to javafx.fxml;
    exports com.example.versioncontrol;
}