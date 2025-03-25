package com.smarthome.model.device;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SmartGarageDoor extends SmartDevice {

    private boolean isAutomaticMode;
    private String securityPin;
    private int batteryLevel;   (0-100)
    private Timer autoCloseTimer;
    private static final int AUTO_CLOSE_TIME = 10 * 60 * 1000;  

    public SmartGarageDoor(String name, String securityPin) {
        super(name);
        this.status = "closed";
        this.isAutomaticMode = true;  // Default to automatic mode
        this.securityPin = securityPin;
        this.batteryLevel = 100;  // Full battery on initialization
    }

    @Override
    public void changeState(String newState) {
        if (!isActive) {
            System.out.println("Device is disconnected.");
            return;
        }

        if (batteryLevel <= 0) {
            System.out.println("Battery is empty. Please replace the battery.");
            return;
        }

        newState = newState.toLowerCase();

        switch (newState) {
            case "open":
                if (isAutomaticMode) {
                    this.status = "open";
                    startAutoCloseTimer();
                    notifyObservers("Garage Door is now open.");
                } else {
                    System.out.println("Manual mode: Please use the manual controls.");
                }
                break;

            case "closed":
                this.status = "closed";
                stopAutoCloseTimer();
                notifyObservers("Garage Door is now closed.");
                break;

            case "half-open":
                this.status = "half-open";
                startAutoCloseTimer();
                notifyObservers("Garage Door is now half-open.");
                break;

            default:
                System.out.println("Invalid state for Garage Door.");
        }
    }

    public void unlockWithPin(String pin) {
        if (!pin.equals(this.securityPin)) {
            System.out.println("Invalid PIN. Access denied.");
            return;
        }
        System.out.println("Access granted with PIN.");
    }

    public void enableManualMode() {
        isAutomaticMode = false;
        stopAutoCloseTimer();
        notifyObservers("Garage Door is now in manual mode.");
    }

    public void enableAutomaticMode() {
        isAutomaticMode = true;
        if (status.equals("open") || status.equals("half-open")) {
            startAutoCloseTimer();  // Start auto-close timer when back in automatic mode
        }
        notifyObservers("Garage Door is now in automatic mode.");
    }

    private void startAutoCloseTimer() {
        if (autoCloseTimer != null) {
            autoCloseTimer.cancel();  // Cancel any existing auto-close timer
        }

        autoCloseTimer = new Timer();
        autoCloseTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (status.equals("open") || status.equals("half-open")) {
                    changeState("closed");  // Automatically close the door after inactivity
                }
            }
        }, AUTO_CLOSE_TIME);  // Set the timer for automatic closing after the specified time
    }

    private void stopAutoCloseTimer() {
        if (autoCloseTimer != null) {
            autoCloseTimer.cancel();  // Cancel any ongoing auto-close timer
        }
    }

    public void checkBatteryLevel() {
        System.out.println("Current battery level: " + batteryLevel + "%");
        if (batteryLevel < 20) {
            System.out.println("Warning: Battery is low. Please recharge or replace the battery.");
        }
    }

    public void drainBattery() {
        if (batteryLevel > 0) {
            batteryLevel -= 10;  // Drain 10% of the battery
            System.out.println("Battery level drained. Current level: " + batteryLevel + "%");
        }
    }

    public void resetForMaintenance() {
        System.out.println("Entering maintenance mode...");
        // Simulate maintenance actions here (e.g., recalibrating sensors)
        notifyObservers("Garage Door is now in maintenance mode.");
    }

    public void exitMaintenanceMode() {
        System.out.println("Exiting maintenance mode...");
        notifyObservers("Garage Door is back to normal operation.");
    }

    public void displayDeviceInfo() {
        System.out.println("Device Name: " + getName());
        System.out.println("Current Status: " + status);
        System.out.println("Automatic Mode: " + isAutomaticMode);
        System.out.println("Battery Level: " + batteryLevel + "%");
        System.out.println("Security PIN is set.");
    }
}
