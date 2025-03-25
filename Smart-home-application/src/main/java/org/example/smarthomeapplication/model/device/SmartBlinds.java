package com.smarthome.model.device;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SmartBlinds extends SmartDevice {

    private boolean isAutomaticMode;
    private boolean isChildSafetyMode;
    private int batteryLevel;  // Battery level (0-100)
    private Timer autoAdjustTimer;
    private static final int AUTO_ADJUST_INTERVAL = 5 * 60 * 1000;

    public SmartBlinds(String name) {
        super(name);
        this.status = "closed";
        this.isAutomaticMode = true;  // Default to automatic mode
        this.isChildSafetyMode = false;  // Default to safety mode off
        this.batteryLevel = 100;
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

        if (isChildSafetyMode) {
            System.out.println("Child Safety Mode is enabled. Manual operation is disabled.");
            return;
        }

        switch (newState) {
            case "open":
                this.status = "open";
                stopAutoAdjustTimer();
                notifyObservers("Smart Blinds are now open.");
                break;

            case "closed":
                this.status = "closed";
                stopAutoAdjustTimer();
                notifyObservers("Smart Blinds are now closed.");
                break;

            case "half-open":
                this.status = "half-open";
                stopAutoAdjustTimer();
                notifyObservers("Smart Blinds are now half-open.");
                break;

            default:
                System.out.println("Invalid state for Smart Blinds.");
        }
    }

    public void enableAutomaticMode() {
        this.isAutomaticMode = true;
        startAutoAdjustTimer();
        notifyObservers("Smart Blinds are now in automatic mode.");
    }

    public void disableAutomaticMode() {
        this.isAutomaticMode = false;
        stopAutoAdjustTimer();
        notifyObservers("Smart Blinds are now in manual mode.");
    }

    public void toggleChildSafetyMode() {
        isChildSafetyMode = !isChildSafetyMode;
        if (isChildSafetyMode) {
            System.out.println("Child Safety Mode enabled. Manual operation is disabled.");
        } else {
            System.out.println("Child Safety Mode disabled. Manual operation is enabled.");
        }
    }

    private void startAutoAdjustTimer() {
        if (!isAutomaticMode) return;

        if (autoAdjustTimer != null) {
            autoAdjustTimer.cancel();
        }

        autoAdjustTimer = new Timer();
        autoAdjustTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (status.equals("closed")) {
                    changeState("open");
                } else if (status.equals("open")) {
                    changeState("closed");
                }
            }
        }, 0, AUTO_ADJUST_INTERVAL);  // Adjust every 5 minutes
    }

    private void stopAutoAdjustTimer() {
        if (autoAdjustTimer != null) {
            autoAdjustTimer.cancel();
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
            batteryLevel -= 10;
            System.out.println("Battery level drained. Current level: " + batteryLevel + "%");
        }
    }

    public void resetBlindsPosition() {
        System.out.println("Resetting the blinds to default position...");
        this.status = "closed";
        notifyObservers("Smart Blinds position has been reset.");
    }

    public void displayDeviceInfo() {
        System.out.println("Device Name: " + getName());
        System.out.println("Current Status: " + status);
        System.out.println("Automatic Mode: " + isAutomaticMode);
        System.out.println("Child Safety Mode: " + isChildSafetyMode);
        System.out.println("Battery Level: " + batteryLevel + "%");
    }
}
