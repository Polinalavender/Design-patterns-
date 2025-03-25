package com.smarthome.model.device;

import java.util.List;

public class SmartSpeaker extends SmartDevice {
    public SmartSpeaker(String name) {
        super(name);
        this.status = "paused";
    }

    @Override
    public void changeState(String newState) {
        if (!isActive) {
            System.out.println("Device is disconnected");
            return;
        }
        if (List.of("playing", "paused", "muted", "volume up", "volume down").contains(newState.toLowerCase())) {
            this.status = newState.toLowerCase();
            notifyObservers("Smart Speaker is now " + status);
        } else {
            System.out.println("Invalid state for Smart Speaker.");
        }
    }
}
