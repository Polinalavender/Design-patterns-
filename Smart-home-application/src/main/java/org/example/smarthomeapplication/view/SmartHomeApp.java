package org.example.smarthomeapplication.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class SmartHomeApp extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        URL fxmlLocation = getClass().getResource("/SmartHomeUI.fxml");
        if (fxmlLocation == null) {
            throw new IOException("ERROR: SmartHomeUI.fxml not found!");
        }

        FXMLLoader loader = new FXMLLoader(fxmlLocation);
        Scene scene = new Scene(loader.load());

        URL iconPath = getClass().getResource("/icon.png");
        if (iconPath != null) {
            primaryStage.getIcons().add(new Image(iconPath.toString()));
        }

        primaryStage.setTitle("Smart Home Automation");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
