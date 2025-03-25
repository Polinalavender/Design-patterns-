package com.smarthome.model.device;

import java.util.List;

public class SmartSprinkler extends SmartDevice {
    public SmartSprinkler(String name) {
        super(name);
        this.status = "off";
    }

    @Override
    public void changeState(String newState) {
        if (!isActive) {
            System.out.println("Device is disconnected");
            return;
        }
        if (List.of("on", "off", "scheduled").contains(newState.toLowerCase())) {
            this.status = newState.toLowerCase();
            notifyObservers("Smart Sprinkler is now " + status);
        } else {
            System.out.println("Invalid state for Smart Sprinkler.");
        }
    }
}
