package org.example.smarthomeapplication.view;

import com.smarthome.util.UIHelper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.device.SmartDevice;
import org.example.smarthomeapplication.viewmodel.SmartHomeController;

import java.util.List;
import java.util.Map;

public class SmartHomeControllerUI {
    @FXML private ComboBox<String> deviceTypeBox;
    @FXML private TextField deviceNameField;
    @FXML private ComboBox<String> deviceListBox;
    @FXML private ComboBox<String> deviceStateBox;
    @FXML private TextArea statusOutput;

    @FXML private Button addDeviceButton;
    @FXML private Button removeDeviceButton;
    @FXML private Button changeStateButton;
    @FXML private Button checkStatusButton;

    private final SmartHomeController controller = new SmartHomeController();

    private final Map<String, List<String>> deviceStates = Map.of(
            "Light", List.of("on", "off", "dimmed", "color mode"),
            "Thermostat", List.of("18°C", "20°C", "22°C", "25°C", "cooling", "heating"),
            "Camera", List.of("on", "off", "recording", "night mode"),
            "Doorbell", List.of("standby", "ringing", "mute"),
            "Smart Lock", List.of("locked", "unlocked", "auto-lock"),
            "Speaker", List.of("playing", "paused", "muted", "volume up", "volume down"),
            "Fan", List.of("off", "low", "medium", "high", "auto"),
            "Garage Door", List.of("open", "closed", "half-open"),
            "Sprinkler", List.of("on", "off", "scheduled"),
            "Blinds", List.of("open", "closed", "half-open")
    );

    @FXML
    private void initialize() {
        deviceTypeBox.getItems().addAll(deviceStates.keySet());
        deviceTypeBox.setOnAction(event -> updateStateOptions());

        addDeviceButton.setOnAction(event -> addDevice());
        removeDeviceButton.setOnAction(event -> removeDevice());
        changeStateButton.setOnAction(event -> changeState());
        checkStatusButton.setOnAction(event -> checkStatus());
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

        controller.addDevice(type, name);
        deviceListBox.getItems().add(name);
        deviceListBox.setValue(name);
        updateStatus("✅ Added " + type + ": " + name);
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
