package org.example.smarthomeapplication.model.device;

import org.example.smarthomeapplication.viewmodel.SmartHomeController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class SmartThermostat extends SmartDevice {
    private double currentTemperature;
    private double targetTemperature;
    private String mode; // "off", "cooling", "heating", "auto"
    private double energyUsage;
    private double humidityLevel;
    private LocalDateTime lastModeChange;
    private static final Random random = new Random();

    // Temperature change rates in degrees per minute
    private static final double HEATING_RATE = 0.5;
    private static final double COOLING_RATE = 0.4;
    private static final double NATURAL_CHANGE_RATE = 0.1;

    public SmartThermostat(String name) {
        super(name);
        this.currentTemperature = 20.0; // Default starting at 20°C
        this.targetTemperature = 20.0;
        this.mode = "off";
        this.status = "20.0°C | OFF";
        this.energyUsage = 0.0;
        this.humidityLevel = 45.0; // Default 45% humidity
        this.lastModeChange = LocalDateTime.now();
    }

    @Override
    public void changeState(String newState) {
        if (!isActive) {
            notifyObservers("Device is disconnected");
            return;
        }

        String originalMode = this.mode;
        LocalDateTime now = LocalDateTime.now();

        // Calculate energy usage since last mode change
        if (!originalMode.equals("off")) {
            double minutesInMode = lastModeChange.until(now, java.time.temporal.ChronoUnit.SECONDS) / 60.0;
            energyUsage += calculateEnergyUsage(originalMode, minutesInMode);
        }

        // Parse new state
        if (newState.startsWith("target:")) {
            try {
                double newTarget = Double.parseDouble(newState.substring(7));
                setTargetTemperature(newTarget);
                notifyObservers("Target temperature set to " + String.format("%.1f°C", targetTemperature));
            } catch (NumberFormatException e) {
                notifyObservers("Invalid temperature format");
            }
            return;
        }

        switch (newState.toLowerCase()) {
            case "current temperature" -> {
                // Just display current status without changing mode
                String tempInfo = String.format("Current temperature: %.1f°C, Target: %.1f°C, Mode: %s",
                        currentTemperature, targetTemperature, mode.toUpperCase());
                String humidityInfo = String.format("Humidity: %.1f%%", humidityLevel);
                String energyInfo = String.format("Energy usage: %.2f kWh", energyUsage);
                notifyObservers(tempInfo + "\n" + humidityInfo + "\n" + energyInfo);
            }
            case "cooling" -> {
                mode = "cooling";
                lastModeChange = now;
                simulateTemperatureChanges();
                updateStatus();
                notifyObservers("Cooling mode activated. Target: " + String.format("%.1f°C", targetTemperature));
            }
            case "heating" -> {
                mode = "heating";
                lastModeChange = now;
                simulateTemperatureChanges();
                updateStatus();
                notifyObservers("Heating mode activated. Target: " + String.format("%.1f°C", targetTemperature));
            }
            case "auto" -> {
                mode = "auto";
                lastModeChange = now;
                decideAutoMode();
                updateStatus();
                notifyObservers("Auto mode activated. System will maintain " + String.format("%.1f°C", targetTemperature));
            }
            case "off" -> {
                mode = "off";
                lastModeChange = now;
                updateStatus();
                notifyObservers("Thermostat turned off");
            }
            default -> {
                try {
                    // Try to parse the input as a direct temperature setting
                    double newTemp = Double.parseDouble(newState.replace("°C", "").trim());
                    setTargetTemperature(newTemp);
                    notifyObservers("Target temperature set to " + String.format("%.1f°C", targetTemperature));
                } catch (NumberFormatException e) {
                    notifyObservers("Unknown command: " + newState);
                }
            }
        }
    }

    public void openControlPanel(SmartHomeController controller) {
        ThermostatControlPanel controlPanel = new ThermostatControlPanel(controller);
        controlPanel.showThermostatControlPanel(this.getDeviceName());
    }

    public void setTargetTemperature(double target) {
        // Limit to reasonable range (10-32°C)
        if (target < 10) target = 10;
        if (target > 32) target = 32;

        this.targetTemperature = target;

        // If in auto mode, decide what to do based on new target
        if (mode.equals("auto")) {
            decideAutoMode();
        }

        updateStatus();
    }

    private void decideAutoMode() {
        if (currentTemperature < targetTemperature - 0.5) {
            // Need heating
            mode = "heating";
            notifyObservers("Auto: Starting heating to reach " + String.format("%.1f°C", targetTemperature));
        } else if (currentTemperature > targetTemperature + 0.5) {
            // Need cooling
            mode = "cooling";
            notifyObservers("Auto: Starting cooling to reach " + String.format("%.1f°C", targetTemperature));
        } else {
            // Temperature is within target range
            mode = "maintaining";
            notifyObservers("Auto: Temperature at desired level, maintaining " + String.format("%.1f°C", targetTemperature));
        }
    }

    public void simulateTemperatureChanges() {
        // Simulate temperature change based on current mode
        double tempChange = 0.0;
        double humidityChange = 0.0;

        switch (mode) {
            case "cooling" -> {
                // Cooling decreases temperature and humidity
                tempChange = -COOLING_RATE - (random.nextDouble() * 0.2);
                humidityChange = -1.0 - (random.nextDouble() * 0.5);
            }
            case "heating" -> {
                // Heating increases temperature and decreases humidity
                tempChange = HEATING_RATE + (random.nextDouble() * 0.2);
                humidityChange = -1.5 - (random.nextDouble() * 0.5);
            }
            case "off", "maintaining" -> {
                // Slight drift toward ambient temperature (assumed to be 22°C for this example)
                double ambientTemp = 22.0;
                tempChange = (ambientTemp - currentTemperature) * NATURAL_CHANGE_RATE;
                humidityChange = random.nextDouble() * 0.6 - 0.3; // Small random changes
            }
        }

        currentTemperature += tempChange;
        humidityLevel += humidityChange;

        // Ensure values are within reasonable bounds
        if (humidityLevel < 20) humidityLevel = 20;
        if (humidityLevel > 70) humidityLevel = 70;

        // Check if target temperature has been reached
        if (mode.equals("cooling") && currentTemperature <= targetTemperature) {
            notifyObservers("Target temperature reached. Maintaining " + String.format("%.1f°C", targetTemperature));
            mode = "maintaining";
        } else if (mode.equals("heating") && currentTemperature >= targetTemperature) {
            notifyObservers("Target temperature reached. Maintaining " + String.format("%.1f°C", targetTemperature));
            mode = "maintaining";
        }

        updateStatus();
    }

    private double calculateEnergyUsage(String mode, double minutes) {
        // Calculate energy usage in kWh based on mode and time
        double kWh = 0;
        switch (mode) {
            case "cooling" -> kWh = 2.5 * (minutes / 60.0); // 2.5 kWh per hour for cooling
            case "heating" -> kWh = 1.8 * (minutes / 60.0); // 1.8 kWh per hour for heating
            case "maintaining" -> kWh = 0.5 * (minutes / 60.0); // 0.5 kWh per hour for maintaining
        }
        return kWh;
    }

    private void updateStatus() {
        String modeDisplay = mode.toUpperCase();
        this.status = String.format("%.1f°C | %s", currentTemperature, modeDisplay);
    }

    public double getCurrentTemperature() {
        return currentTemperature;
    }

    public double getTargetTemperature() {
        return targetTemperature;
    }

    public String getMode() {
        return mode;
    }

    public double getEnergyUsage() {
        return energyUsage;
    }

    public double getHumidityLevel() {
        return humidityLevel;
    }

    // For weekly simulation
    public String generateWeeklyReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== Weekly Thermostat Report ===\n");
        report.append(String.format("Total Energy Usage: %.2f kWh\n", energyUsage));
        report.append(String.format("Estimated Cost: $%.2f\n", energyUsage * 0.15)); // Assuming $0.15 per kWh
        report.append(String.format("Average Temperature: %.1f°C\n", currentTemperature));
        report.append(String.format("Average Humidity: %.1f%%\n", humidityLevel));

        // Calculate energy saving recommendations
        double potentialSavings = 0;
        if (mode.equals("cooling") && targetTemperature < 24) {
            potentialSavings += (24 - targetTemperature) * 0.1 * 7; // Potential daily savings * 7 days
            report.append("Recommendation: Increasing cooling temperature to 24°C could save approximately ");
            report.append(String.format("$%.2f per week\n", potentialSavings));
        } else if (mode.equals("heating") && targetTemperature > 20) {
            potentialSavings += (targetTemperature - 20) * 0.12 * 7; // Potential daily savings * 7 days
            report.append("Recommendation: Decreasing heating temperature to 20°C could save approximately ");
            report.append(String.format("$%.2f per week\n", potentialSavings));
        }

        return report.toString();
    }
}