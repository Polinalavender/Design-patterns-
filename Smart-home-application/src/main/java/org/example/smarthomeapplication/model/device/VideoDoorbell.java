package com.smarthome.model.device;

import java.util.*;

public class VideoDoorbell extends SmartDevice {
    private boolean motionDetectionEnabled;
    private boolean nightMode;
    private List<String> visitorLog;
    private Map<String, String> accessControl;
    private int batteryLevel;
    private boolean liveFeedActive;

    public VideoDoorbell(String name) {
        super(name);
        this.status = "standby";
        this.motionDetectionEnabled = false;
        this.nightMode = false;
        this.visitorLog = new ArrayList<>();
        this.accessControl = new HashMap<>();
        this.batteryLevel = 100;
        this.liveFeedActive = false;
    }

    @Override
    public void changeState(String newState) {
        if (!isActive) {
            System.out.println("Device is disconnected");
            return;
        }
        this.status = newState;
        notifyObservers("Doorbell status: " + status);
    }

    public void toggleMotionDetection() {
        this.motionDetectionEnabled = !this.motionDetectionEnabled;
        System.out.println("Motion detection " + (motionDetectionEnabled ? "enabled" : "disabled"));
    }

    public void enableNightMode(boolean enable) {
        this.nightMode = enable;
        System.out.println("Night mode " + (nightMode ? "activated" : "deactivated"));
    }

    public void logVisitor(String visitorName) {
        String timestamp = getCurrentTime();
        visitorLog.add(timestamp + " - Visitor: " + visitorName);
        System.out.println("Visitor " + visitorName + " logged at " + timestamp);
    }

    public void showVisitorLog() {
        System.out.println("Visitor Log:");
        for (String log : visitorLog) {
            System.out.println(log);
        }
    }

    public void ringDoorbell() {
        System.out.println("Ding Dong! Someone is at the door.");
        notifyObservers("Doorbell rang.");
        checkBatteryStatus();
    }

    public void grantAccess(String person, String code) {
        accessControl.put(person, code);
        System.out.println("Access granted to " + person);
    }

    public boolean verifyAccess(String person, String enteredCode) {
        if (accessControl.containsKey(person) && accessControl.get(person).equals(enteredCode)) {
            System.out.println("Access verified for " + person);
            return true;
        } else {
            System.out.println("Access denied for " + person);
            return false;
        }
    }

    public void startLiveFeed() {
        if (!isActive) {
            System.out.println("Cannot start live feed. Device is disconnected.");
            return;
        }
        this.liveFeedActive = true;
        System.out.println("Live feed started.");
    }

    public void stopLiveFeed() {
        this.liveFeedActive = false;
        System.out.println("Live feed stopped.");
    }

    private void checkBatteryStatus() {
        batteryLevel -= 5;
        if (batteryLevel <= 20) {
            System.out.println("Warning: Battery level is low (" + batteryLevel + "%)");
        }
    }

    private String getCurrentTime() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}