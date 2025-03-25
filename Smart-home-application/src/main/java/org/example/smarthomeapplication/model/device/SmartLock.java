package com.smarthome.model.device;

import java.util.List;

public class SmartLock extends SmartDevice {
    public SmartLock(String name) {
        super(name);
        this.status = "locked";
    }

    @Override
    public void changeState(String newState) {
        if (!isActive) {
            System.out.println("Device is disconnected");
            return;
        }
        if (List.of("locked", "unlocked", "auto-lock").contains(newState.toLowerCase())) {
            this.status = newState.toLowerCase();
            notifyObservers("Smart Lock is now " + status);
        } else {
            System.out.println("Invalid state for Smart Lock.");
        }
    }
}
