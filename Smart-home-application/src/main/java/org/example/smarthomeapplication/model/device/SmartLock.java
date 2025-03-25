package com.smarthome.model.device;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class SmartLock extends SmartDevice {

    private String pinCode;  
    private Map<String, String> userPins;
    private boolean isAutoLockEnabled;
    private Timer autoLockTimer;
    private int batteryLevel;

    public SmartLock(String name, String pinCode) {
        super(name);
        this.status = "locked";
        this.pinCode = pinCode;
        this.userPins = new HashMap<>();
        this.userPins.put("admin", pinCode);
        this.isAutoLockEnabled = true;
        this.batteryLevel = 100;
    }

    @Override
    public void changeState(String newState) {
        if (!isActive) {
            System.out.println("Device is disconnected.");
            return;
        }

        newState = newState.toLowerCase();

        switch (newState) {
            case "locked":
                this.status = newState;
                if (isAutoLockEnabled) {
                    stopAutoLockTimer();
                }
                notifyObservers("Smart Lock is now locked.");
                break;

            case "unlocked":
                this.status = newState;
                if (isAutoLockEnabled) {
                    startAutoLockTimer();
                }
                notifyObservers("Smart Lock is now unlocked.");
                break;

            case "auto-lock":
                this.isAutoLockEnabled = !this.isAutoLockEnabled;
                if (isAutoLockEnabled) {
                    startAutoLockTimer();
                    notifyObservers("Auto-lock is now enabled.");
                } else {
                    stopAutoLockTimer();
                    notifyObservers("Auto-lock is now disabled.");
                }
                break;

            default:
                System.out.println("Invalid state for Smart Lock.");
        }
    }


    public void unlockWithPin(String pin) {
        if (!isActive) {
            System.out.println("Device is disconnected.");
            return;
        }

        if (pin.equals(this.pinCode) || userPins.containsValue(pin)) {
            this.status = "unlocked";
            if (isAutoLockEnabled) {
                startAutoLockTimer();
            }
            notifyObservers("Smart Lock is now unlocked by PIN.");
        } else {
            System.out.println("Invalid PIN.");
        }
    }


    public void addUser(String username, String pin) {
        if (!isActive) {
            System.out.println("Device is disconnected.");
            return;
        }

        if (userPins.containsKey(username)) {
            System.out.println("User already exists.");
        } else {
            userPins.put(username, pin);
            notifyObservers("User " + username + " added with a new PIN.");
        }
    }


    public void removeUser(String username) {
        if (!isActive) {
            System.out.println("Device is disconnected.");
            return;
        }

        if (userPins.containsKey(username)) {
            userPins.remove(username);
            notifyObservers("User " + username + " has been removed.");
        } else {
            System.out.println("User not found.");
        }
    }


    private void startAutoLockTimer() {
        if (autoLockTimer != null) {
            autoLockTimer.cancel();  // Cancel any existing timer
        }

        autoLockTimer = new Timer();
        autoLockTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (status.equals("unlocked")) {
                    changeState("locked");
                }
            }
        }, 5 * 60 * 1000);
    }


    private void stopAutoLockTimer() {
        if (autoLockTimer != null) {
            autoLockTimer.cancel();
        }
    }

    // Simulate the battery drain over time
    public void drainBattery() {
        if (batteryLevel > 0) {
            batteryLevel -= 5;
            System.out.println("Battery level is now: " + batteryLevel + "%");
        } else {
            System.out.println("Battery is empty! Please replace the battery.");
        }
    }

    // Check the battery level
    public void checkBatteryLevel() {
        System.out.println("Battery level: " + batteryLevel + "%");
    }

    // Emergency override to unlock the door
    public void emergencyUnlock() {
        if (!isActive) {
            System.out.println("Device is disconnected.");
            return;
        }

        // Forcefully unlock the lock (bypass PIN and other restrictions)
        this.status = "unlocked";
        stopAutoLockTimer();
        notifyObservers("Emergency override: Smart Lock is now unlocked.");
    }


    public void displayDeviceInfo() {
        System.out.println("Device Name: " + getName());
        System.out.println("Current Status: " + status);
        System.out.println("Auto-Lock Enabled: " + isAutoLockEnabled);
        System.out.println("Battery Level: " + batteryLevel + "%");
        System.out.println("Registered Users: " + userPins.keySet());
    }
}
