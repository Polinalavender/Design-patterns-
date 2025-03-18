package com.smarthome.model.device;

import java.util.List;

public class SmartGarageDoor extends SmartDevice {
    public SmartGarageDoor(String name) {
        super(name);
        this.status = "closed";
    }

    @Override
    public void changeState(String newState) {
        if (!isActive) {
            System.out.println("Device is disconnected");
            return;
        }
        if (List.of("open", "closed", "half-open").contains(newState.toLowerCase())) {
            this.status = newState.toLowerCase();
            notifyObservers("Garage Door is now " + status);
        } else {
            System.out.println("Invalid state for Garage Door.");
        }
    }
}
