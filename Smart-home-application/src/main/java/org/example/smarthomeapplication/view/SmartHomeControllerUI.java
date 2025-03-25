package org.example.smarthomeapplication.view;

import com.smarthome.util.UIHelper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.util.Duration;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.scene.paint.Color;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Glow;
import javafx.application.Platform;
import javafx.scene.shape.Circle;

import org.example.smarthomeapplication.model.device.*;
import org.example.smarthomeapplication.viewmodel.SmartHomeController;
import org.example.smarthomeapplication.user.User;
import org.example.smarthomeapplication.user.Observer;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SmartHomeControllerUI implements Observer {
    @FXML
    private ComboBox<String> deviceTypeBox;
    @FXML
    private TextField deviceNameField;
    @FXML
    private ComboBox<String> deviceListBox;
    @FXML
    private ComboBox<String> deviceStateBox;
    @FXML
    private TextArea statusOutput;

    @FXML
    private Button addDeviceButton;
    @FXML
    private Button removeDeviceButton;
    @FXML
    private Button changeStateButton;
    @FXML
    private Button checkStatusButton;
    @FXML
    private Button takePhotoButton;
    @FXML
    private Button galleryButton;

    // Voice Assistant specific fields
    @FXML
    private Circle statusIndicator;
    @FXML
    private Label statusLabel;
    @FXML
    private VBox animatedWaveform;
    @FXML
    private HBox barContainer;
    @FXML
    private StackPane pulseContainer;
    @FXML
    private Circle pulseCircle;
    @FXML
    private Slider volumeSlider;
    @FXML
    private ToggleButton listeningToggle;
    @FXML
    private ToggleButton muteToggle;
    @FXML
    private Button historyButton;
    @FXML
    private TextArea conversationArea;
    @FXML
    private TextField commandInput;
    @FXML
    private Button sendButton;

    // UI controls for light
    @FXML
    private Slider brightnessSlider;
    @FXML
    private ComboBox<String> colorSelector;
    @FXML
    private Button applyLightSettingsButton;
    @FXML
    private StackPane lightPreviewPane;

    private Region lightPreviewRegion;
    private ColorAdjust colorEffect = new ColorAdjust();
    private Glow glowEffect = new Glow();

    private final SmartHomeController controller = new SmartHomeController();
    private final User currentUser = new User("Default User");
    private String lastPhotoTaken = null;
    private SmartVoiceAssistant currentAssistant;
    private Timeline waveformAnimation;
    private Timeline pulseAnimation;

    private final Map<String, List<String>> deviceStates = Map.of(
            "Light", List.of("on", "off"),
            "Thermostat", List.of("current temperature", "cooling", "heating"),
            "Camera", List.of("on", "off", "recording", "night mode"),
            "Voice Assistant", List.of("standby", "ringing", "mute")
    );

    private final List<String> lightColors = List.of("white", "red", "blue", "pink", "green", "yellow", "purple", "orange");

    @FXML
    private Button thermostatControlButton;

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

        // Initialize smart light controls if they exist in the FXML
        if (brightnessSlider != null) {
            brightnessSlider.setMin(0);
            brightnessSlider.setMax(100);
            brightnessSlider.setValue(100);
            brightnessSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                    updateLightPreview());
        }

        if (colorSelector != null) {
            colorSelector.getItems().addAll(lightColors);
            colorSelector.setValue("white");
            colorSelector.setOnAction(event -> updateLightPreview());
        }

        if (applyLightSettingsButton != null) {
            applyLightSettingsButton.setOnAction(event -> applyLightSettings());
        }

        if (lightPreviewPane != null) {
            lightPreviewRegion = new Region();
            lightPreviewRegion.setMinSize(150, 150);
            lightPreviewRegion.setPrefSize(150, 150);
            lightPreviewRegion.setMaxSize(150, 150);
            lightPreviewRegion.setStyle("-fx-background-color: white; -fx-background-radius: 75;");

            // Set up the initial effects
            glowEffect.setLevel(0.5);
            lightPreviewRegion.setEffect(glowEffect);

            lightPreviewPane.getChildren().add(lightPreviewRegion);
            lightPreviewPane.setAlignment(Pos.CENTER);

            updateLightPreview();

            if (thermostatControlButton != null) {
                thermostatControlButton.setOnAction(event -> openThermostatPanel());
                thermostatControlButton.setVisible(false);
            }
        }

        // Set listener for device selection to update controls
        deviceListBox.setOnAction(event -> updateDeviceSpecificControls());

        // Initialize voice assistant controls
        initializeVoiceAssistantControls();
        setupWaveformBars();
        setupAnimations();
    }

    private void updateDeviceSpecificControls() {
        String deviceName = deviceListBox.getValue();
        if (deviceName == null) {
            // Disable all light-specific controls when no device is selected
            if (brightnessSlider != null) {
                brightnessSlider.setVisible(false);
                brightnessSlider.setDisable(true);
            }
            if (colorSelector != null) {
                colorSelector.setVisible(false);
                colorSelector.setDisable(true);
            }
            if (applyLightSettingsButton != null) {
                applyLightSettingsButton.setVisible(false);
                applyLightSettingsButton.setDisable(true);
            }
            if (lightPreviewPane != null) {
                lightPreviewPane.setVisible(false);
                lightPreviewPane.setDisable(true);
            }
            return;
        }

        SmartDevice device = null;
        boolean isThermostatDevice = device instanceof SmartThermostat;
        if (thermostatControlButton != null) thermostatControlButton.setVisible(isThermostatDevice);

        device = controller.getDevice(deviceName);
        if (device == null) return;

        boolean isLightDevice = device instanceof SmartLight;

        // Show/hide and enable/disable light-specific controls
        if (brightnessSlider != null) {
            brightnessSlider.setVisible(isLightDevice);
            brightnessSlider.setDisable(!isLightDevice);
        }
        if (colorSelector != null) {
            colorSelector.setVisible(isLightDevice);
            colorSelector.setDisable(!isLightDevice);
        }
        if (applyLightSettingsButton != null) {
            applyLightSettingsButton.setVisible(isLightDevice);
            applyLightSettingsButton.setDisable(!isLightDevice);
        }
        if (lightPreviewPane != null) {
            lightPreviewPane.setVisible(isLightDevice);
            lightPreviewPane.setDisable(!isLightDevice);
        }

        // If it's a light device, update the light controls
        if (isLightDevice && device instanceof SmartLight light) {
            if (brightnessSlider != null) brightnessSlider.setValue(light.getBrightness());
            if (colorSelector != null) {
                String currentColor = light.getColor();
                if (lightColors.contains(currentColor)) {
                    colorSelector.setValue(currentColor);
                }
            }
            updateLightPreview();
        }

        // Check if the selected device is a voice assistant
        device = controller.getDevice(deviceListBox.getValue());
        boolean isVoiceAssistant = device instanceof SmartVoiceAssistant;

        // Show/hide voice assistant-specific controls
        if (statusIndicator != null) statusIndicator.setVisible(isVoiceAssistant);
        if (statusLabel != null) statusLabel.setVisible(isVoiceAssistant);
        if (volumeSlider != null) volumeSlider.setVisible(isVoiceAssistant);
        if (listeningToggle != null) listeningToggle.setVisible(isVoiceAssistant);
        if (muteToggle != null) muteToggle.setVisible(isVoiceAssistant);
        if (historyButton != null) historyButton.setVisible(isVoiceAssistant);
        if (conversationArea != null) conversationArea.setVisible(isVoiceAssistant);
        if (commandInput != null) commandInput.setVisible(isVoiceAssistant);
        if (sendButton != null) sendButton.setVisible(isVoiceAssistant);

        // Update the current assistant and controls
        if (isVoiceAssistant) {
            currentAssistant = (SmartVoiceAssistant) device;
            updateAssistantDisplay();
        } else {
            currentAssistant = null;
            resetAssistantDisplay();
        }
    }

    private void initializeVoiceAssistantControls() {
        // Volume slider action
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (currentAssistant != null) {
                currentAssistant.changeState("volume:" + newVal.intValue());
            }
        });

        // Listening toggle action
        listeningToggle.setOnAction(event -> {
            if (currentAssistant != null) {
                if (listeningToggle.isSelected()) {
                    currentAssistant.changeState("listening");
                    muteToggle.setSelected(false);
                    startAnimations();
                } else {
                    currentAssistant.changeState("passive");
                    stopAnimations();
                }
            } else {
                updateStatus("No voice assistant selected.");
                listeningToggle.setSelected(false);
            }
        });

        // Mute toggle action
        muteToggle.setOnAction(event -> {
            if (currentAssistant != null) {
                if (muteToggle.isSelected()) {
                    currentAssistant.changeState("mute");
                    listeningToggle.setSelected(false);
                    stopAnimations();
                } else {
                    currentAssistant.changeState("passive");
                }
            } else {
                updateStatus("No voice assistant selected.");
                muteToggle.setSelected(false);
            }
        });

        // History button action
        historyButton.setOnAction(event -> {
            if (currentAssistant != null) {
                displayCommandHistory();
            } else {
                updateStatus("No voice assistant selected.");
            }
        });

        // Send button action
        sendButton.setOnAction(event -> sendCommand());
        commandInput.setOnAction(event -> sendCommand());
    }

    private void setupWaveformBars() {
        barContainer.getChildren().clear();

        for (int i = 0; i < 8; i++) {
            Region bar = new Pane();
            bar.setPrefSize(5, 5);
            bar.setMaxWidth(5);
            bar.setMinWidth(5);
            bar.setPrefHeight(5);
            bar.setStyle("-fx-background-color: #3498db; -fx-background-radius: 2;");

            HBox.setMargin(bar, new javafx.geometry.Insets(0, 2, 0, 2));

            barContainer.getChildren().add(bar);
        }
    }

    private void setupAnimations() {
        waveformAnimation = new Timeline();
        waveformAnimation.setCycleCount(Timeline.INDEFINITE);

        Random random = new Random();

        KeyFrame waveKeyFrame = new KeyFrame(Duration.millis(500), event -> {
            for (int i = 0; i < barContainer.getChildren().size(); i++) {
                Region bar = (Region) barContainer.getChildren().get(i);
                double newHeight = 5 + random.nextInt(30);
                bar.setPrefHeight(newHeight);
            }
        });

        waveformAnimation.getKeyFrames().add(waveKeyFrame);

        pulseAnimation = new Timeline();
        pulseAnimation.setCycleCount(Timeline.INDEFINITE);

        KeyFrame pulseGrow = new KeyFrame(
                Duration.millis(1000),
                new KeyValue(pulseCircle.radiusProperty(), 30)
        );

        KeyFrame pulseShrink = new KeyFrame(
                Duration.millis(2000),
                new KeyValue(pulseCircle.radiusProperty(), 25)
        );

        pulseAnimation.getKeyFrames().addAll(pulseGrow, pulseShrink);
    }

    private void startAnimations() {
        waveformAnimation.play();
        pulseAnimation.play();
    }

    private void stopAnimations() {
        waveformAnimation.stop();
        pulseAnimation.stop();

        barContainer.getChildren().forEach(node -> {
            if (node instanceof Region) {
                ((Region) node).setPrefHeight(5);
            }
        });
        pulseCircle.setRadius(25);
    }

    private void sendCommand() {
        String command = commandInput.getText().trim();
        if (command.isEmpty()) return;

        if (currentAssistant != null) {
            currentAssistant.changeState("command:" + command);
            commandInput.clear();

            Timeline processingAnimation = new Timeline(
                    new KeyFrame(Duration.ZERO, e -> {
                        statusIndicator.setFill(Color.web("#3498db")); // Blue for processing
                        startAnimations();
                    }),
                    new KeyFrame(Duration.millis(800), e -> {
                        if (currentAssistant.getListeningMode().equals("active")) {
                            statusIndicator.setFill(Color.web("#27ae60")); // Back to green
                        } else {
                            statusIndicator.setFill(Color.web("#f39c12")); // Back to orange
                            stopAnimations();
                        }
                    })
            );
            processingAnimation.play();
        } else {
            updateStatus("No voice assistant selected.");
        }
    }

    private void displayCommandHistory() {
        if (currentAssistant == null) return;

        Map<String, String> history = currentAssistant.getCommandHistory();
        StringBuilder historyText = new StringBuilder("Command History:\n\n");

        for (Map.Entry<String, String> entry : history.entrySet()) {
            historyText.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Command History");
        dialog.setHeaderText("Command history for " + currentAssistant.getDeviceName());

        TextArea textArea = new TextArea(historyText.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefWidth(400);
        textArea.setPrefHeight(300);

        dialog.getDialogPane().setContent(textArea);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void updateAssistantDisplay() {
        volumeSlider.setValue(currentAssistant.getVolume());

        switch (currentAssistant.getListeningMode()) {
            case "active":
                listeningToggle.setSelected(true);
                muteToggle.setSelected(false);
                statusIndicator.setFill(Color.web("#27ae60")); // Green
                statusLabel.setText("Status: listening");
                startAnimations();
                break;
            case "passive":
                listeningToggle.setSelected(false);
                muteToggle.setSelected(false);
                statusIndicator.setFill(Color.web("#f39c12")); // Orange/Yellow
                statusLabel.setText("Status: idle");
                stopAnimations();
                break;
            case "muted":
                listeningToggle.setSelected(false);
                muteToggle.setSelected(true);
                statusIndicator.setFill(Color.web("#e74c3c")); // Red
                statusLabel.setText("Status: muted");
                stopAnimations();
                break;
        }

        conversationArea.setText(currentAssistant.getActiveConversation());
    }

    private void resetAssistantDisplay() {
        listeningToggle.setSelected(false);
        muteToggle.setSelected(false);
        statusIndicator.setFill(Color.LIGHTGRAY);
        statusLabel.setText("Status: no device");
        conversationArea.clear();
        stopAnimations();
    }

    private void updateLightPreview() {
        if (lightPreviewRegion == null) return;

        double brightness = 0.0;
        if (brightnessSlider != null) {
            brightness = brightnessSlider.getValue() / 100.0;
        }

        String color = "white";
        if (colorSelector != null) {
            color = colorSelector.getValue();
        }

        // Apply color
        Color jfxColor;
        switch (color) {
            case "red" -> jfxColor = Color.RED;
            case "blue" -> jfxColor = Color.BLUE;
            case "pink" -> jfxColor = Color.PINK;
            case "green" -> jfxColor = Color.GREEN;
            case "yellow" -> jfxColor = Color.YELLOW;
            case "purple" -> jfxColor = Color.PURPLE;
            case "orange" -> jfxColor = Color.ORANGE;
            default -> jfxColor = Color.WHITE;
        }

        // Convert to RGB hex code for CSS
        String colorHex = String.format("#%02X%02X%02X",
                (int)(jfxColor.getRed() * 255),
                (int)(jfxColor.getGreen() * 255),
                (int)(jfxColor.getBlue() * 255));

        // Apply brightness
        glowEffect.setLevel(brightness * 0.8);

        // Update the light preview style
        lightPreviewRegion.setStyle("-fx-background-color: " + colorHex + "; -fx-background-radius: 75;");

        // Simulate dimming - make entire view darker when brightness is lower
        String deviceName = deviceListBox.getValue();
        if (deviceName != null) {
            SmartDevice device = controller.getDevice(deviceName);
            if (device instanceof SmartLight light && light.getStatus().equals("on")) {
                applyLightEffectToScreen(jfxColor, brightness);
            }
        }
    }

    private void applyLightEffectToScreen(Color color, double brightness) {
        // Create a global screen effect to simulate the light's effect on the screen
        // This is simulated here - in a real app this would affect the entire application

        if (lightPreviewPane != null && lightPreviewPane.getScene() != null) {
            ColorAdjust colorAdjust = new ColorAdjust();

            // Adjust hue to simulate color
            if (color == Color.RED) {
                colorAdjust.setHue(0.8);
            } else if (color == Color.BLUE) {
                colorAdjust.setHue(-0.7);
            } else if (color == Color.GREEN) {
                colorAdjust.setHue(0.4);
            } else if (color == Color.YELLOW) {
                colorAdjust.setHue(0.2);
            } else if (color == Color.PINK) {
                colorAdjust.setHue(0.9);
            } else if (color == Color.PURPLE) {
                colorAdjust.setHue(-0.4);
            } else if (color == Color.ORANGE) {
                colorAdjust.setHue(0.1);
            }

            // Apply brightness
            colorAdjust.setBrightness(brightness - 0.5);

            // This applies to the light preview pane only for demonstration
            lightPreviewPane.setEffect(colorAdjust);
        }
    }

    private void applyLightSettings() {
        String deviceName = deviceListBox.getValue();
        if (deviceName == null) {
            UIHelper.showErrorAlert("Selection Error", "Please select a light device.");
            return;
        }

        SmartDevice device = controller.getDevice(deviceName);
        if (!(device instanceof SmartLight)) {
            UIHelper.showErrorAlert("Device Type Error", "Selected device is not a light.");
            return;
        }

        // Apply brightness
        if (brightnessSlider != null) {
            int brightness = (int) brightnessSlider.getValue();
            controller.changeDeviceState(deviceName, "brightness:" + brightness);
        }

        // Apply color
        if (colorSelector != null) {
            String color = colorSelector.getValue();
            controller.changeDeviceState(deviceName, "color:" + color);
        }

        // Make sure light is on to see effects
        String currentStatus = device.getStatus().toLowerCase();
        if (!currentStatus.startsWith("on")) {
            controller.changeDeviceState(deviceName, "on");
        }

        updateStatus("💡 Applied light settings: Brightness: " +
                (int)brightnessSlider.getValue() + "%, Color: " +
                colorSelector.getValue());
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

        // If the notification is about a light change, update the preview
        if (message.contains("Light is") || message.contains("Brightness changed") ||
                message.contains("Color changed")) {
            javafx.application.Platform.runLater(() -> {
                updateDeviceSpecificControls();
            });
        }

        Platform.runLater(() -> {
            // Update assistant display if the current assistant sent the update
            if (currentAssistant != null) {
                updateAssistantDisplay();
            }
        });
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

        // Update device-specific controls
        updateDeviceSpecificControls();

        device = controller.getDevice(deviceListBox.getValue());
        if (device instanceof SmartVoiceAssistant) {
            currentAssistant = (SmartVoiceAssistant) device;
            updateAssistantDisplay();
        }
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

        // Update device-specific controls after state change
        updateDeviceSpecificControls();
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

        // Additional info for lights
        if (device instanceof SmartLight light) {
            updateStatus("💡 Brightness: " + light.getBrightness() + "%" +
                    "\n🎨 Color: " + light.getColor());
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

        if (camera.takePhoto()) {
            updateStatus("📸 Photo taken with " + name);
        } else {
            updateStatus("❌ Failed to take photo. Make sure camera is in recording mode.");
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

        try {
            for (String photoName : photos) {
                String photoPath = camera.getPhotoPath(photoName);
                File photoFile = new File(photoPath);

                if (photoFile.exists()) {
                    // Create thumbnail
                    Image image = new Image(new FileInputStream(photoFile), 200, 150, true, true);
                    ImageView imageView = new ImageView(image);

                    // Add some styling
                    imageView.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);");

                    // Create a label with the photo name
                    Label photoLabel = new Label(photoName);

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

        // Create close button
        Button closeButton = new Button("Close Gallery");
        closeButton.setOnAction(event -> galleryStage.close());

        // Create button container
        HBox buttonBox = new HBox(10, deleteAllButton, closeButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10));

        // Add everything to the main layout
        VBox mainLayout = new VBox(10, scrollPane, buttonBox);

        // Set the scene and show the stage
        Scene galleryScene = new Scene(mainLayout, 700, 500);
        galleryStage.setScene(galleryScene);
        galleryStage.show();
    }

    @FXML
    private void clearAllDevices() {
        if (UIHelper.showConfirmationAlert("Confirm", "Are you sure you want to remove all devices?")) {
            controller.clearAllDevices();
            deviceListBox.getItems().clear();
            updateStatus("🗑️ Cleared all devices.");
        }
    }

    public void openThermostatPanel() {
        String deviceName = deviceListBox.getValue();
        if (deviceName == null) {
            UIHelper.showErrorAlert("Selection Error", "Please select a thermostat device.");
            return;
        }

        SmartDevice device = controller.getDevice(deviceName);
        if (!(device instanceof SmartThermostat)) {
            UIHelper.showErrorAlert("Device Type Error", "Selected device is not a thermostat.");
            return;
        }

        ThermostatControlPanel panel = new ThermostatControlPanel(controller);
        panel.showThermostatControlPanel(deviceName);
    }

    private void updateStatus(String message) {
        statusOutput.appendText(message + "\n");
    }
}
