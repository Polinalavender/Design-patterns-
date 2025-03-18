package model.device;

import device.SmartDevice;

public class SmartLight extends SmartDevice {
    public SmartLight(String name) {
        super(name);
        this.status = "off";
    }

    @Override
    public void changeState(String newState) {
        if (!isActive) {
            System.out.println("Device is disconnected");
            return;
        }
        if (newState.equalsIgnoreCase("on") || newState.equalsIgnoreCase("off")) {
            this.status = newState.toLowerCase();
            notifyObservers("Light is " + status);
        }
    }
}
