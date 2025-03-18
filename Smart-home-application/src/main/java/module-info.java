module org.example.smarthomeapplication {
    requires javafx.controls;
    requires javafx.fxml;

    opens org.example.smarthomeapplication.view to javafx.fxml; // Allow JavaFX to reflectively access the controller
    exports org.example.smarthomeapplication.view; // Allow public access if needed
}
