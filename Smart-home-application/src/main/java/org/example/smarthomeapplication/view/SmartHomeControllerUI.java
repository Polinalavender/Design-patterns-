package org.example.smarthomeapplication.view;

import com.smarthome.util.UIHelper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.util.Duration;
import javafx.animation.PauseTransition;

import org.example.smarthomeapplication.model.device.SmartDevice;
import org.example.smarthomeapplication.model.device.SmartCamera;
import org.example.smarthomeapplication.viewmodel.SmartHomeController;
import org.example.smarthomeapplication.user.User;
import org.example.smarthomeapplication.user.Observer;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SmartHomeControllerUI implements Observer {
    @FXML private ComboBox<String> deviceTypeBox;
    @FXML private TextField deviceNameField;
    @FXML private ComboBox<String> deviceListBox;
    @FXML private ComboBox<String> deviceStateBox;
    @FXML private TextArea statusOutput;

    @FXML private Button addDeviceButton;
    @FXML private Button removeDeviceButton;
    @FXML private Button changeStateButton;
    @FXML private Button checkStatusButton;
    @FXML private Button takePhotoButton;
    @FXML private Button galleryButton;

    private final SmartHomeController controller = new SmartHomeController();
    private final User currentUser = new User("Default User");
    private String lastPhotoTaken = null;

    private final Map<String, List<String>> deviceStates = Map.of(
            "Light", List.of("on", "off", "dimmed", "color mode"),
            "Thermostat", List.of("18°C", "20°C", "22°C", "25°C", "cooling", "heating"),
            "Camera", List.of("on", "off", "recording", "night mode"),
            "Doorbell", List.of("standby", "ringing", "mute")
    );

    @FXML
    private void initialize() {
        deviceTypeBox.getItems().addAll(deviceStates.keySet());
        deviceTypeBox.setOnAction(event -> updateStateOptions());

        addDeviceButton.setOnAction(event -> addDevice());
        removeDeviceButton.setOnAction(event -> removeDevice());
        changeStateButton.setOnAction(event -> changeState());
        checkStatusButton.setOnAction(event -> checkStatus());

        // Initialize the camera buttons if they exist in the FXML
        if (takePhotoButton != null) {
            takePhotoButton.setOnAction(event -> takePhoto());
        }

        if (galleryButton != null) {
            galleryButton.setOnAction(event -> openGallery());
        }
    }

    @Override
    public void update(String message) {
        // This method will be called when the device sends a notification
        updateStatus("📱 NOTIFICATION: " + message);

        // If the notification is about a photo being taken, show a popup
        if (message.contains("Photo taken")) {
            // Extract the photo name if possible
            int startIndex = message.indexOf("Photo taken: ");
            int endIndex = message.indexOf(".", startIndex);

            if (startIndex >= 0 && endIndex >= 0) {
                lastPhotoTaken = message.substring(startIndex + 13, endIndex);
            }

            // Show a notification popup on the JavaFX thread
            javafx.application.Platform.runLater(() -> {
                showPhotoNotification();
            });
        }
    }

    private void showPhotoNotification() {
        // Create a custom notification popup
        Stage notificationStage = new Stage();
        notificationStage.setTitle("New Photo Taken");

        // Create the notification content
        Label messageLabel = new Label("A new photo has been taken successfully!");
        messageLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label timeLabel = new Label("Time: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));

        Button viewButton = new Button("View in Gallery");
        viewButton.setOnAction(e -> {
            notificationStage.close();
            openGallery();
        });

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> notificationStage.close());

        // Layout
        HBox buttonBox = new HBox(10, viewButton, closeButton);
        buttonBox.setAlignment(Pos.CENTER);

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));
        layout.getChildren().addAll(
                messageLabel,
                timeLabel,
                buttonBox
        );
        layout.setStyle("-fx-background-color: #f0f8ff;");

        // Set scene
        Scene scene = new Scene(layout);
        notificationStage.setScene(scene);
        notificationStage.setWidth(300);
        notificationStage.setHeight(180);

        // Set the notification to auto-close after 10 seconds
        PauseTransition delay = new PauseTransition(Duration.seconds(10));
        delay.setOnFinished(e -> notificationStage.close());
        delay.play();

        // Show the notification
        notificationStage.show();
    }

    @FXML
    private void removeDevice() {
        String name = deviceListBox.getValue();
        if (name == null) {
            UIHelper.showErrorAlert("Selection Error", "Please select a device to remove.");
            return;
        }

        if (UIHelper.showConfirmationAlert("Confirm Removal", "Are you sure you want to remove " + name + "?")) {
            controller.removeDevice(name);
            deviceListBox.getItems().remove(name);
            deviceListBox.setValue(null);
            updateStatus("❌ Removed device: " + name);
        }
    }

    private void updateStateOptions() {
        String selectedType = deviceTypeBox.getValue();
        deviceStateBox.getItems().clear();
        if (selectedType != null) {
            deviceStateBox.getItems().addAll(deviceStates.get(selectedType));
        }
    }

    @FXML
    private void addDevice() {
        String type = deviceTypeBox.getValue();
        String name = deviceNameField.getText().trim();

        if (type == null || name.isEmpty()) {
            UIHelper.showErrorAlert("Input Error", "Please select a device type and enter a name.");
            return;
        }

        if (controller.getDevice(name) != null) {
            UIHelper.showErrorAlert("Duplicate Device", "A device with this name already exists.");
            return;
        }

        SmartDevice device = controller.addDevice(type, name);
        deviceListBox.getItems().add(name);
        deviceListBox.setValue(name);
        updateStatus("✅ Added " + type + ": " + name);

        // Add both the current user and this UI controller as observers of the device
        device.addObserver(currentUser);
        device.addObserver(this); // Adding this UI controller as an observer
    }

    @FXML
    private void changeState() {
        String name = deviceListBox.getValue();
        String state = deviceStateBox.getValue();

        if (name == null || state == null) {
            UIHelper.showErrorAlert("Input Error", "Please select a device and a valid state.");
            return;
        }

        controller.changeDeviceState(name, state);
        updateStatus("🔄 Changed " + name + " state to: " + state);
    }

    @FXML
    private void checkStatus() {
        String name = deviceListBox.getValue();
        if (name == null) {
            UIHelper.showErrorAlert("Selection Error", "Please select a device.");
            return;
        }

        SmartDevice device = controller.getDevice(name);
        updateStatus("ℹ️ Device: " + name + "\n📊 Status: " + device.getStatus() + "\n🟢 Active: " + device.isActive());

        // Additional info for cameras
        if (device instanceof SmartCamera camera) {
            updateStatus("📷 Recording: " + camera.isRecording() +
                    "\n🌙 Night Mode: " + camera.isNightMode());

            String[] photos = camera.getPhotosList();
            updateStatus("🖼️ Photos taken: " + photos.length);
        }
    }

    @FXML
    private void takePhoto() {
        String name = deviceListBox.getValue();
        if (name == null) {
            UIHelper.showErrorAlert("Selection Error", "Please select a camera device.");
            return;
        }

        SmartDevice device = controller.getDevice(name);
        if (!(device instanceof SmartCamera camera)) {
            UIHelper.showErrorAlert("Device Type Error", "Selected device is not a camera.");
            return;
        }

        // Try to take the photo
        if (camera.takePhoto()) {
            updateStatus("📸 Photo taken with " + name);

            // Show visual feedback on the button
            String originalText = takePhotoButton.getText();
            takePhotoButton.setDisable(true);
            takePhotoButton.setText("Processing...");

            // Reset button after a short delay
            PauseTransition delay = new PauseTransition(Duration.seconds(1));
            delay.setOnFinished(e -> {
                takePhotoButton.setText(originalText);
                takePhotoButton.setDisable(false);

                // Show visual notification confirming photo is taken
                // Note: The Observer pattern will also trigger the notification popup
            });
            delay.play();
        } else {
            UIHelper.showErrorAlert("Camera Error",
                    "Failed to take photo. Make sure camera is in recording mode (on or night mode).");
        }
    }

    @FXML
    private void openGallery() {
        String name = deviceListBox.getValue();
        if (name == null) {
            UIHelper.showErrorAlert("Selection Error", "Please select a camera device.");
            return;
        }

        SmartDevice device = controller.getDevice(name);
        if (!(device instanceof SmartCamera camera)) {
            UIHelper.showErrorAlert("Device Type Error", "Selected device is not a camera.");
            return;
        }

        String[] photos = camera.getPhotosList();
        if (photos.length == 0) {
            UIHelper.showInfoAlert("Empty Gallery", "No photos found for " + name);
            return;
        }

        // Create a new window for the gallery
        Stage galleryStage = new Stage();
        galleryStage.setTitle("Photo Gallery - " + name);

        // Create a grid for photos
        GridPane photoGrid = new GridPane();
        photoGrid.setPadding(new Insets(10));
        photoGrid.setHgap(10);
        photoGrid.setVgap(10);

        // Add photos to the grid
        int col = 0;
        int row = 0;
        final int MAX_COLS = 3;
        String highlightedPhoto = lastPhotoTaken;

        try {
            for (String photoName : photos) {
                String photoPath = camera.getPhotoPath(photoName);
                File photoFile = new File(photoPath);

                if (photoFile.exists()) {
                    // Create thumbnail
                    Image image = new Image(new FileInputStream(photoFile), 200, 150, true, true);
                    ImageView imageView = new ImageView(image);

                    // Add styling and highlight the last taken photo
                    if (photoName.equals(highlightedPhoto)) {
                        imageView.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,255,0,0.8), 15, 0, 0, 0);");
                    } else {
                        imageView.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);");
                    }

                    // Create a label with the photo name
                    Label photoLabel = new Label(photoName);
                    if (photoName.equals(highlightedPhoto)) {
                        photoLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    }

                    // Create a container for the image and label
                    VBox photoBox = new VBox(10, imageView, photoLabel);
                    photoBox.setAlignment(Pos.CENTER);

                    // Add photo to grid
                    photoGrid.add(photoBox, col, row);

                    // Move to next position
                    col++;
                    if (col >= MAX_COLS) {
                        col = 0;
                        row++;
                    }

                    // Set up click event to open full-size image
                    imageView.setOnMouseClicked(event -> {
                        try {
                            Stage fullImageStage = new Stage();
                            Image fullImage = new Image(new FileInputStream(photoFile));
                            ImageView fullImageView = new ImageView(fullImage);

                            // Fit to screen while maintaining aspect ratio
                            fullImageView.setPreserveRatio(true);
                            fullImageView.setFitWidth(800);
                            fullImageView.setFitHeight(600);

                            VBox fullImageBox = new VBox(fullImageView);
                            fullImageBox.setAlignment(Pos.CENTER);

                            Scene fullImageScene = new Scene(fullImageBox);
                            fullImageStage.setScene(fullImageScene);
                            fullImageStage.setTitle(photoName);
                            fullImageStage.show();
                        } catch (Exception e) {
                            UIHelper.showErrorAlert("Error", "Could not open image: " + e.getMessage());
                        }
                    });
                }
            }
        } catch (Exception e) {
            UIHelper.showErrorAlert("Gallery Error", "Error loading photos: " + e.getMessage());
        }

        // Create scroll pane for the grid
        ScrollPane scrollPane = new ScrollPane(photoGrid);
        scrollPane.setFitToWidth(true);

        // Create delete all button
        Button deleteAllButton = new Button("Delete All Photos");
        deleteAllButton.setOnAction(event -> {
            if (UIHelper.showConfirmationAlert("Confirm Deletion", "Are you sure you want to delete all photos?")) {
                if (camera.clearAllPhotos()) {
                    UIHelper.showInfoAlert("Success", "All photos deleted successfully.");
                    galleryStage.close();
                } else {
                    UIHelper.showErrorAlert("Error", "Failed to delete some photos.");
                }
            }
        });

        // Create take new photo button
        Button takeNewPhotoButton = new Button("Take New Photo");
        takeNewPhotoButton.setOnAction(event -> {
            galleryStage.close();
            takePhoto();
        });

        // Create close button
        Button closeButton = new Button("Close Gallery");
        closeButton.setOnAction(event -> galleryStage.close());

        // Create button container
        HBox buttonBox = new HBox(10, takeNewPhotoButton, deleteAllButton, closeButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10));

        // Add everything to the main layout
        VBox mainLayout = new VBox(10, scrollPane, buttonBox);

        // Set the scene and show the stage
        Scene galleryScene = new Scene(mainLayout, 700, 500);
        galleryStage.setScene(galleryScene);
        galleryStage.show();

        // If there's a highlighted photo, scroll to it
        if (highlightedPhoto != null) {
            // Find the highlighted photo and scroll to it
            // This is a simple approach that works in many cases
            scrollPane.setVvalue(0.5);
        }
    }

    @FXML
    private void clearAllDevices() {
        if (UIHelper.showConfirmationAlert("Confirm", "Are you sure you want to remove all devices?")) {
            controller.clearAllDevices();
            deviceListBox.getItems().clear();
            updateStatus("🗑️ Cleared all devices.");
        }
    }

    private void updateStatus(String message) {
        statusOutput.appendText(message + "\n");
    }
}