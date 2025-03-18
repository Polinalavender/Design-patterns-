package com.smarthome.model.device;

import java.util.List;

public class SmartBlinds extends SmartDevice {
    public SmartBlinds(String name) {
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
            notifyObservers("Smart Blinds are now " + status);
        } else {
            System.out.println("Invalid state for Smart Blinds.");
        }
    }
}
