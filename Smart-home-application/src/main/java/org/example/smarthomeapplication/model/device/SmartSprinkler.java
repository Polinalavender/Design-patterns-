package com.smarthome.model.device;

import java.util.*;

public class SmartSprinkler extends SmartDevice {
    private int wateringDuration;
    private boolean rainSensorEnabled;
    private Map<String, Integer> schedule;
    private List<String> wateringHistory;

    public SmartSprinkler(String name) {
        super(name);
        this.status = "off";
        this.wateringDuration = 10;
        this.rainSensorEnabled = false;
        this.schedule = new HashMap<>();
        this.wateringHistory = new ArrayList<>();
    }

    @Override
    public void changeState(String newState) {
        if (!isActive) {
            System.out.println("Device is disconnected");
            return;
        }
        if (List.of("on", "off", "scheduled").contains(newState.toLowerCase())) {
            this.status = newState.toLowerCase();
            if (status.equals("on")) {
                startWatering();
            }
            notifyObservers("Smart Sprinkler is now " + status);
        } else {
            System.out.println("Invalid state for Smart Sprinkler.");
        }
    }

    private void startWatering() {
        if (rainSensorEnabled && isRaining()) {
            System.out.println("Rain detected. Watering canceled.");
            return;
        }
        System.out.println("Watering started for " + wateringDuration + " minutes.");
        wateringHistory.add(getCurrentTime() + " - Watered for " + wateringDuration + " minutes");
    }

    public void setWateringDuration(int minutes) {
        if (minutes < 1 || minutes > 60) {
            System.out.println("Invalid duration. Choose between 1-60 minutes.");
            return;
        }
        this.wateringDuration = minutes;
        System.out.println("Watering duration set to " + minutes + " minutes.");
    }

    public void toggleRainSensor() {
        this.rainSensorEnabled = !this.rainSensorEnabled;
        System.out.println("Rain sensor " + (rainSensorEnabled ? "enabled" : "disabled"));
    }

    public void setSchedule(String time, int duration) {
        if (duration < 1 || duration > 60) {
            System.out.println("Invalid duration for schedule.");
            return;
        }
        schedule.put(time, duration);
        System.out.println("Scheduled watering at " + time + " for " + duration + " minutes.");
    }

    private void checkSchedule() {
        String currentTime = getCurrentTime();
        if (schedule.containsKey(currentTime)) {
            wateringDuration = schedule.get(currentTime);
            startWatering();
        }
    }

    private String getCurrentTime() {
        return new java.text.SimpleDateFormat("HH:mm").format(new Date());
    }

    private boolean isRaining() {
        return new Random().nextBoolean(); // Simulated rain sensor.
    }
}