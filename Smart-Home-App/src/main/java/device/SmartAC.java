package com.smarthome.model.device;

import java.util.List;

public class SmartAC extends SmartDevice {
    public SmartAC(String name) {
        super(name);
        this.status = "off";
    }

    @Override
    public void changeState(String newState) {
        if (!isActive) {
            System.out.println("Device is disconnected");
            return;
        }
        if (List.of("off", "cooling", "heating", "fan mode", "auto").contains(newState.toLowerCase())) {
            this.status = newState.toLowerCase();
            notifyObservers("AC mode changed to " + status);
        } else {
            System.out.println("Invalid state for AC.");
        }
    }
}
