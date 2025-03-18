package com.smarthome.model.device;

import java.util.List;

public class SmartFan extends SmartDevice {
    public SmartFan(String name) {
        super(name);
        this.status = "off";
    }

    @Override
    public void changeState(String newState) {
        if (!isActive) {
            System.out.println("Device is disconnected");
            return;
        }
        if (List.of("off", "low", "medium", "high", "auto").contains(newState.toLowerCase())) {
            this.status = newState.toLowerCase();
            notifyObservers("Smart Fan speed is set to " + status);
        } else {
            System.out.println("Invalid state for Smart Fan.");
        }
    }
}
