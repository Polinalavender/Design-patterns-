package com.smarthome.model.device;

import java.util.*;

public class SmartAC extends SmartDevice {
    private int temperature;
    private String fanSpeed;
    private boolean energySavingMode;
    private Map<String, String> schedule;

    public SmartAC(String name) {
        super(name);
        this.status = "off";
        this.temperature = 24;
        this.fanSpeed = "medium";
        this.energySavingMode = false;
        this.schedule = new HashMap<>();
    }

    @Override
    public void changeState(String newState) {
        if (!isActive) {
            System.out.println("Device is disconnected");
            return;
        }
        if (List.of("off", "cooling", "heating", "fan mode", "auto").contains(newState.toLowerCase())) {
            this.status = newState.toLowerCase();
            adjustSettingsBasedOnMode();
            notifyObservers("AC mode changed to " + status);
        } else {
            System.out.println("Invalid state for AC.");
        }
    }

    private void adjustSettingsBasedOnMode() {
        if (status.equals("cooling")) {
            temperature = Math.max(16, temperature - 2);
        } else if (status.equals("heating")) {
            temperature = Math.min(30, temperature + 2);
        } else if (status.equals("auto")) {
            adjustTemperatureBasedOnTime();
        }
    }

    public void setTemperature(int temp) {
        if (temp < 16 || temp > 30) {
            System.out.println("Temperature out of range (16-30°C)");
            return;
        }
        this.temperature = temp;
        System.out.println("Temperature set to: " + temp + "°C");
    }

    public void setFanSpeed(String speed) {
        if (!List.of("low", "medium", "high").contains(speed.toLowerCase())) {
            System.out.println("Invalid fan speed. Choose: low, medium, high");
            return;
        }
        this.fanSpeed = speed.toLowerCase();
        System.out.println("Fan speed set to: " + fanSpeed);
    }

    public void toggleEnergySavingMode() {
        this.energySavingMode = !this.energySavingMode;
        System.out.println("Energy-saving mode " + (energySavingMode ? "enabled" : "disabled"));
    }

    public void setSchedule(String time, String mode) {
        if (!List.of("off", "cooling", "heating", "fan mode", "auto").contains(mode.toLowerCase())) {
            System.out.println("Invalid mode for scheduling.");
            return;
        }
        schedule.put(time, mode);
        System.out.println("Scheduled AC to " + mode + " at " + time);
    }

    private void adjustTemperatureBasedOnTime() {
        String currentTime = getCurrentTime();
        if (schedule.containsKey(currentTime)) {
            changeState(schedule.get(currentTime));
        }
    }

    private String getCurrentTime() {
        return new java.text.SimpleDateFormat("HH:mm").format(new Date());
    }
}