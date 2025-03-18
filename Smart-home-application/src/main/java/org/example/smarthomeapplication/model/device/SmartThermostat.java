package org.example.smarthomeapplication.model.device;

public class SmartThermostat extends SmartDevice {
    public SmartThermostat(String name) {
        super(name);
        this.status = "20°C";
    }

    @Override
    public void changeState(String newState) {
        if (!isActive) {
            System.out.println("Device is disconnected");
            return;
        }
        this.status = newState;
        notifyObservers("Temperature set to " + status);
    }
}
