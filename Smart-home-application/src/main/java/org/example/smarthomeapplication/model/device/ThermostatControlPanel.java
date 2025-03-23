package org.example.smarthomeapplication.model.device;

import com.smarthome.util.UIHelper;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.smarthomeapplication.viewmodel.SmartHomeController;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ThermostatControlPanel {
    private final SmartHomeController controller;
    private SmartThermostat thermostat;
    private String deviceName;

    // UI Components
    private Text temperatureText;
    private Text modeText;
    private Text humidityText;
    private Text energyText;
    private Circle temperatureIndicator;
    private Slider temperatureSlider;
    private ToggleGroup modeToggleGroup;
    private ToggleButton heatingButton;
    private ToggleButton coolingButton;
    private ToggleButton offButton;
    private ToggleButton autoButton;
    private Timeline simulationTimeline;

    // Chart components
    private LineChart<Number, Number> temperatureChart;
    private XYChart.Series<Number, Number> temperatureSeries;
    private XYChart.Series<Number, Number> targetSeries;
    private int timeCounter = 0;
    private final List<Double> temperatureHistory = new ArrayList<>();
    private final List<Double> targetHistory = new ArrayList<>();

    public ThermostatControlPanel(SmartHomeController controller) {
        this.controller = controller;
    }

    public void showThermostatControlPanel(String deviceName) {
        this.deviceName = deviceName;
        SmartDevice device = controller.getDevice(deviceName);

        if (!(device instanceof SmartThermostat)) {
            UIHelper.showErrorAlert("Device Error", "The selected device is not a thermostat");
            return;
        }

        this.thermostat = (SmartThermostat) device;

        // Create the stage
        Stage stage = new Stage();
        stage.setTitle("Thermostat Control Panel - " + deviceName);

        // Create the main layout
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: #f5f5f5;");

        // Add the components
        mainLayout.setTop(createHeaderSection());
        mainLayout.setCenter(createCenterSection());
        mainLayout.setRight(createControlsSection());
        mainLayout.setBottom(createFooterSection());

        // Create the scene
        Scene scene = new Scene(mainLayout, 800, 600);
        stage.setScene(scene);

        // Start simulation timeline
        startSimulation();

        // Stop simulation when window is closed
        stage.setOnCloseRequest(event -> {
            stopSimulation();
        });

        stage.show();
    }

    private VBox createHeaderSection() {
        // Create device name header
        Label deviceNameLabel = new Label(deviceName);
        deviceNameLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        deviceNameLabel.setStyle("-fx-text-fill: #2c3e50;");

        // Create the temperature display
        temperatureText = new Text(formatTemperature(thermostat.getCurrentTemperature()));
        temperatureText.setFont(Font.font("System", FontWeight.BOLD, 48));

        // Create the temperature indicator
        temperatureIndicator = new Circle(30);
        updateTemperatureIndicator();
        temperatureIndicator.setEffect(new DropShadow(10, Color.GRAY));

        // Create the mode text
        modeText = new Text("Mode: " + thermostat.getMode().toUpperCase());
        modeText.setFont(Font.font("System", FontWeight.NORMAL, 16));

        // Humidity and energy usage
        humidityText = new Text("Humidity: " + formatValue(thermostat.getHumidityLevel()) + "%");
        humidityText.setFont(Font.font("System", FontWeight.NORMAL, 14));

        energyText = new Text("Energy Usage: " + formatValue(thermostat.getEnergyUsage()) + " kWh");
        energyText.setFont(Font.font("System", FontWeight.NORMAL, 14));

        // Layout
        HBox tempAndIndicator = new HBox(20, temperatureIndicator, temperatureText);
        tempAndIndicator.setAlignment(Pos.CENTER);

        VBox statusInfo = new VBox(5, modeText, humidityText, energyText);
        statusInfo.setAlignment(Pos.CENTER);

        VBox header = new VBox(15, deviceNameLabel, tempAndIndicator, statusInfo);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 20, 0));
        header.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

        return header;
    }

    private VBox createCenterSection() {
        // Create temperature chart
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Time (seconds)");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Temperature (°C)");
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(10);
        yAxis.setUpperBound(35);
        yAxis.setTickUnit(5);

        temperatureChart = new LineChart<>(xAxis, yAxis);
        temperatureChart.setTitle("Temperature Monitoring");
        temperatureChart.setCreateSymbols(false);
        temperatureChart.setAnimated(false);

        // Create the data series
        temperatureSeries = new XYChart.Series<>();
        temperatureSeries.setName("Current Temperature");

        targetSeries = new XYChart.Series<>();
        targetSeries.setName("Target Temperature");

        temperatureChart.getData().addAll(temperatureSeries, targetSeries);

        // Add initial data points
        addDataPoint();

        // Layout
        VBox centerSection = new VBox(20, temperatureChart);
        centerSection.setPadding(new Insets(20, 0, 0, 0));

        return centerSection;
    }

    private VBox createControlsSection() {
        // Temperature slider
        temperatureSlider = new Slider(10, 32, thermostat.getTargetTemperature());
        temperatureSlider.setShowTickLabels(true);
        temperatureSlider.setShowTickMarks(true);
        temperatureSlider.setMajorTickUnit(5);
        temperatureSlider.setMinorTickCount(4);
        temperatureSlider.setBlockIncrement(1);

        // Target temperature label
        Label targetTempLabel = new Label("Target Temperature: " + formatValue(thermostat.getTargetTemperature()) + "°C");
        targetTempLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));

        // Update target temperature when slider changes
        temperatureSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double targetTemp = Math.round(newVal.doubleValue() * 10) / 10.0;
            targetTempLabel.setText("Target Temperature: " + formatValue(targetTemp) + "°C");
        });

        Button setTempButton = new Button("Set Temperature");
        setTempButton.setOnAction(e -> {
            double targetTemp = Math.round(temperatureSlider.getValue() * 10) / 10.0;
            controller.changeDeviceState(deviceName, "target:" + targetTemp);
            updateUI();
        });

        // Mode toggle buttons
        modeToggleGroup = new ToggleGroup();

        heatingButton = new ToggleButton("Heating");
        heatingButton.setToggleGroup(modeToggleGroup);
        heatingButton.setPrefWidth(100);

        coolingButton = new ToggleButton("Cooling");
        coolingButton.setToggleGroup(modeToggleGroup);
        coolingButton.setPrefWidth(100);

        autoButton = new ToggleButton("Auto");
        autoButton.setToggleGroup(modeToggleGroup);
        autoButton.setPrefWidth(100);

        offButton = new ToggleButton("Off");
        offButton.setToggleGroup(modeToggleGroup);
        offButton.setPrefWidth(100);

        // Select the current mode
        updateModeButtons();

        // Set action handlers for mode buttons
        heatingButton.setOnAction(e -> {
            if (heatingButton.isSelected()) {
                controller.changeDeviceState(deviceName, "heating");
                updateUI();
            }
        });

        coolingButton.setOnAction(e -> {
            if (coolingButton.isSelected()) {
                controller.changeDeviceState(deviceName, "cooling");
                updateUI();
            }
        });

        autoButton.setOnAction(e -> {
            if (autoButton.isSelected()) {
                controller.changeDeviceState(deviceName, "auto");
                updateUI();
            }
        });

        offButton.setOnAction(e -> {
            if (offButton.isSelected()) {
                controller.changeDeviceState(deviceName, "off");
                updateUI();
            }
        });

        // Layout for mode buttons
        HBox modeButtonsRow1 = new HBox(10, heatingButton, coolingButton);
        HBox modeButtonsRow2 = new HBox(10, autoButton, offButton);
        VBox modeButtons = new VBox(10, modeButtonsRow1, modeButtonsRow2);
        modeButtons.setAlignment(Pos.CENTER);

        Label operationModeLabel = new Label("Operation Mode");
        operationModeLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        // Generate Weekly Report Button
        Button reportButton = new Button("Generate Weekly Report");
        reportButton.setMaxWidth(Double.MAX_VALUE);
        reportButton.setOnAction(e -> generateWeeklyReport());

        // Layout
        VBox controlsSection = new VBox(15,
                new Label("Temperature Control"),
                temperatureSlider,
                targetTempLabel,
                setTempButton,
                new Separator(),
                operationModeLabel,
                modeButtons,
                new Separator(),
                reportButton);

        controlsSection.setPadding(new Insets(20));
        controlsSection.setStyle("-fx-background-color: #e8e8e8; -fx-background-radius: 5;");
        controlsSection.setPrefWidth(250);

        return controlsSection;
    }

    private HBox createFooterSection() {
        // Status message
        Label statusLabel = new Label("Simulation is running every 3 seconds...");
        statusLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        statusLabel.setStyle("-fx-text-fill: #7f8c8d;");

        Button resetButton = new Button("Reset Simulation");
        resetButton.setOnAction(e -> resetSimulation());

        HBox footer = new HBox(20, statusLabel, resetButton);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(20, 0, 0, 0));

        return footer;
    }

    private void startSimulation() {
        simulationTimeline = new Timeline(
                new KeyFrame(Duration.seconds(3), event -> {
                    // Simulate temperature change
                    thermostat.simulateTemperatureChanges();

                    // Update UI
                    Platform.runLater(this::updateUI);

                    // Add data point to chart
                    Platform.runLater(this::addDataPoint);
                })
        );
        simulationTimeline.setCycleCount(Timeline.INDEFINITE);
        simulationTimeline.play();
    }

    private void stopSimulation() {
        if (simulationTimeline != null) {
            simulationTimeline.stop();
        }
    }

    private void resetSimulation() {
        timeCounter = 0;
        temperatureHistory.clear();
        targetHistory.clear();
        temperatureSeries.getData().clear();
        targetSeries.getData().clear();
        addDataPoint();
    }

    private void addDataPoint() {
        // Get current values
        double currentTemp = thermostat.getCurrentTemperature();
        double targetTemp = thermostat.getTargetTemperature();

        // Add to history
        temperatureHistory.add(currentTemp);
        targetHistory.add(targetTemp);

        // Add to chart
        temperatureSeries.getData().add(new XYChart.Data<>(timeCounter, currentTemp));
        targetSeries.getData().add(new XYChart.Data<>(timeCounter, targetTemp));

        // Limit chart data to last 50 points for performance
        if (temperatureSeries.getData().size() > 50) {
            temperatureSeries.getData().remove(0);
            targetSeries.getData().remove(0);
        }

        timeCounter += 3;  // 3 seconds between updates
    }

    private void updateUI() {
        // Update temperature text and indicator
        temperatureText.setText(formatTemperature(thermostat.getCurrentTemperature()));
        updateTemperatureIndicator();

        // Update mode text
        modeText.setText("Mode: " + thermostat.getMode().toUpperCase());

        // Update other stats
        humidityText.setText("Humidity: " + formatValue(thermostat.getHumidityLevel()) + "%");
        energyText.setText("Energy Usage: " + formatValue(thermostat.getEnergyUsage()) + " kWh");

        // Update mode buttons
        updateModeButtons();

        // Update target temperature slider
        temperatureSlider.setValue(thermostat.getTargetTemperature());
    }

    private void updateTemperatureIndicator() {
        double temp = thermostat.getCurrentTemperature();

        // Color based on temperature range
        Color color;
        if (temp < 16) {
            color = Color.BLUE;  // Cold
        } else if (temp < 19) {
            color = Color.CORNFLOWERBLUE;  // Cool
        } else if (temp < 22) {
            color = Color.GREEN;  // Comfortable
        } else if (temp < 25) {
            color = Color.YELLOW;  // Warm
        } else if (temp < 28) {
            color = Color.ORANGE;  // Hot
        } else {
            color = Color.RED;  // Very hot
        }

        temperatureIndicator.setFill(color);
    }

    private void updateModeButtons() {
        String mode = thermostat.getMode().toLowerCase();

        heatingButton.setSelected(mode.equals("heating"));
        coolingButton.setSelected(mode.equals("cooling"));
        autoButton.setSelected(mode.equals("auto") || mode.equals("maintaining"));
        offButton.setSelected(mode.equals("off"));

        // Highlight the active mode button
        String selectedStyle = "-fx-background-color: #3498db; -fx-text-fill: white;";
        String normalStyle = "";

        heatingButton.setStyle(heatingButton.isSelected() ? selectedStyle : normalStyle);
        coolingButton.setStyle(coolingButton.isSelected() ? selectedStyle : normalStyle);
        autoButton.setStyle(autoButton.isSelected() ? selectedStyle : normalStyle);
        offButton.setStyle(offButton.isSelected() ? selectedStyle : normalStyle);
    }

    private void generateWeeklyReport() {
        String report = thermostat.generateWeeklyReport();

        // Create a report dialog
        Stage reportStage = new Stage();
        reportStage.setTitle("Weekly Thermostat Report - " + deviceName);

        TextArea reportArea = new TextArea(report);
        reportArea.setEditable(false);
        reportArea.setPrefRowCount(15);
        reportArea.setPrefWidth(400);
        reportArea.setFont(Font.font("Courier New", 14));

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> reportStage.close());

        VBox layout = new VBox(20, reportArea, closeButton);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout);
        reportStage.setScene(scene);
        reportStage.show();
    }

    private String formatTemperature(double temp) {
        DecimalFormat df = new DecimalFormat("0.0");
        return df.format(temp) + "°C";
    }

    private String formatValue(double value) {
        DecimalFormat df = new DecimalFormat("0.0");
        return df.format(value);
    }
}
