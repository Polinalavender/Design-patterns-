package org.example.smarthomeapplication.model.device;

import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

import com.smarthome.util.UIHelper;
import org.example.smarthomeapplication.viewmodel.SmartHomeController;

public class ThermostatControlPanel {

    // Thermostat control UI elements
    private Slider temperatureSlider;
    private Label temperatureLabel;
    private Label currentTempLabel;
    private Label humidityLabel;
    private Label energyUsageLabel;
    private Label modeLabel;
    private Button coolingButton;
    private Button heatingButton;
    private Button autoButton;
    private Button offButton;
    private Circle temperatureIndicator;
    private Timeline simulationTimeline;

    private final SmartHomeController controller;

    public ThermostatControlPanel(SmartHomeController controller) {
        this.controller = controller;
    }

    public void showThermostatControlPanel(String deviceName) {
        SmartDevice device = controller.getDevice(deviceName);
        if (!(device instanceof SmartThermostat thermostat)) {
            UIHelper.showErrorAlert("Device Error", "Selected device is not a thermostat");
            return;
        }

        Stage stage = new Stage();
        stage.setTitle("Thermostat Control - " + deviceName);

        // Create temperature display
        temperatureIndicator = new Circle(80);
        updateTemperatureIndicator(thermostat.getCurrentTemperature());

        temperatureLabel = new Label(String.format("%.1f°C", thermostat.getCurrentTemperature()));
        temperatureLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        temperatureLabel.setTextFill(Color.WHITE);

        StackPane temperatureDisplay = new StackPane(temperatureIndicator, temperatureLabel);

        // Create stat displays
        currentTempLabel = new Label(String.format("Current: %.1f°C", thermostat.getCurrentTemperature()));
        currentTempLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));

        modeLabel = new Label("Mode: " + thermostat.getMode().toUpperCase());
        modeLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));

        humidityLabel = new Label(String.format("Humidity: %.1f%%", thermostat.getHumidityLevel()));
        humidityLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));

        energyUsageLabel = new Label(String.format("Energy: %.2f kWh", thermostat.getEnergyUsage()));
        energyUsageLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));

        VBox statsBox = new VBox(5, currentTempLabel, modeLabel, humidityLabel, energyUsageLabel);
        statsBox.setAlignment(Pos.CENTER_LEFT);
        statsBox.setPadding(new Insets(10));

        HBox displayBox = new HBox(30, temperatureDisplay, statsBox);
        displayBox.setAlignment(Pos.CENTER);
        displayBox.setPadding(new Insets(20));

        // Create temperature slider
        temperatureSlider = new Slider(16, 28, thermostat.getTargetTemperature());
        temperatureSlider.setShowTickLabels(true);
        temperatureSlider.setShowTickMarks(true);
        temperatureSlider.setMajorTickUnit(2);
        temperatureSlider.setMinorTickCount(1);
        temperatureSlider.setBlockIncrement(0.5);

        Label targetTempLabel = new Label("Target Temperature:");
        Label tempValueLabel = new Label(String.format("%.1f°C", thermostat.getTargetTemperature()));

        temperatureSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double roundedValue = Math.round(newVal.doubleValue() * 2) / 2.0; // Round to nearest 0.5
            tempValueLabel.setText(String.format("%.1f°C", roundedValue));
        });

        Button applyTempButton = new Button("Set Temperature");
        applyTempButton.setOnAction(e -> {
            double value = Math.round(temperatureSlider.getValue() * 2) / 2.0;
            controller.changeDeviceState(deviceName, "target:" + value);
            updateUIFromThermostat(thermostat);
        });

        HBox tempControlBox = new HBox(10, targetTempLabel, temperatureSlider, tempValueLabel, applyTempButton);
        tempControlBox.setAlignment(Pos.CENTER);

        // Create mode buttons
        coolingButton = new Button("Cooling");
        coolingButton.setStyle("-fx-base: lightblue;");
        coolingButton.setOnAction(e -> {
            controller.changeDeviceState(deviceName, "cooling");
            updateUIFromThermostat(thermostat);
        });

        heatingButton = new Button("Heating");
        heatingButton.setStyle("-fx-base: lightsalmon;");
        heatingButton.setOnAction(e -> {
            controller.changeDeviceState(deviceName, "heating");
            updateUIFromThermostat(thermostat);
        });

        autoButton = new Button("Auto");
        autoButton.setStyle("-fx-base: lightgreen;");
        autoButton.setOnAction(e -> {
            controller.changeDeviceState(deviceName, "auto");
            updateUIFromThermostat(thermostat);
        });

        offButton = new Button("Off");
        offButton.setStyle("-fx-base: lightgray;");
        offButton.setOnAction(e -> {
            controller.changeDeviceState(deviceName, "off");
            updateUIFromThermostat(thermostat);
        });

        Button statusButton = new Button("Check Status");
        statusButton.setOnAction(e -> {
            controller.changeDeviceState(deviceName, "current temperature");
            updateUIFromThermostat(thermostat);
        });

        Button reportButton = new Button("Weekly Report");
        reportButton.setOnAction(e -> {
            UIHelper.showInfoAlert("Weekly Report", thermostat.generateWeeklyReport());
        });

        HBox modeButtonsBox = new HBox(10, coolingButton, heatingButton, autoButton, offButton);
        modeButtonsBox.setAlignment(Pos.CENTER);

        HBox statusButtonsBox = new HBox(10, statusButton, reportButton);
        statusButtonsBox.setAlignment(Pos.CENTER);

        // Create main layout
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.getChildren().addAll(
                displayBox,
                tempControlBox,
                modeButtonsBox,
                statusButtonsBox
        );
        mainLayout.setAlignment(Pos.CENTER);

        // Start simulation timeline
        startSimulation(thermostat, deviceName);

        // Set scene
        Scene scene = new Scene(mainLayout, 600, 400);
        stage.setScene(scene);

        // Handle close request
        stage.setOnCloseRequest(e -> {
            if (simulationTimeline != null) {
                simulationTimeline.stop();
            }
        });

        stage.show();
    }

    private void startSimulation(SmartThermostat thermostat, String deviceName) {
        simulationTimeline = new Timeline(
                new KeyFrame(Duration.seconds(3), e -> {
                    thermostat.simulateTemperatureChanges();
                    updateUIFromThermostat(thermostat);
                })
        );
        simulationTimeline.setCycleCount(Timeline.INDEFINITE);
        simulationTimeline.play();
    }

    private void updateUIFromThermostat(SmartThermostat thermostat) {
        // Update all UI elements
        temperatureLabel.setText(String.format("%.1f°C", thermostat.getCurrentTemperature()));
        currentTempLabel.setText(String.format("Current: %.1f°C", thermostat.getCurrentTemperature()));
        modeLabel.setText("Mode: " + thermostat.getMode().toUpperCase());
        humidityLabel.setText(String.format("Humidity: %.1f%%", thermostat.getHumidityLevel()));
        energyUsageLabel.setText(String.format("Energy: %.2f kWh", thermostat.getEnergyUsage()));

        updateTemperatureIndicator(thermostat.getCurrentTemperature());

        // Highlight active mode button
        String currentMode = thermostat.getMode();
        coolingButton.setStyle("-fx-base: lightblue;");
        heatingButton.setStyle("-fx-base: lightsalmon;");
        autoButton.setStyle("-fx-base: lightgreen;");
        offButton.setStyle("-fx-base: lightgray;");

        // Add darker highlight to active button
        switch (currentMode) {
            case "cooling" -> coolingButton.setStyle("-fx-base: deepskyblue; -fx-font-weight: bold;");
            case "heating" -> heatingButton.setStyle("-fx-base: salmon; -fx-font-weight: bold;");
            case "auto", "maintaining" -> autoButton.setStyle("-fx-base: limegreen; -fx-font-weight: bold;");
            case "off" -> offButton.setStyle("-fx-base: darkgray; -fx-font-weight: bold;");
        }
    }

    private void updateTemperatureIndicator(double temperature) {
        // Color based on temperature
        Color color;
        if (temperature <= 18) {
            color = Color.BLUE;
        } else if (temperature <= 22) {
            color = Color.GREEN;
        } else if (temperature <= 25) {
            color = Color.YELLOW;
        } else {
            color = Color.RED;
        }

        temperatureIndicator.setFill(color);
    }
}