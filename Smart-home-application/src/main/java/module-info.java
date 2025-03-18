module org.example.smarthomeapplication {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens org.example.smarthomeapplication to javafx.fxml;
    exports org.example.smarthomeapplication;
}