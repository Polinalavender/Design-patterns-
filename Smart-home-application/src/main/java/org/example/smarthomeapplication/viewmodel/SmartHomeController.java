package org.example.smarthomeapplication.viewmodel;

import model.device.SmartDevice;
import org.example.smarthomeapplication.Factory.SmartDeviceFactory;

import java.util.HashMap;
import java.util.Map;

public class SmartHomeController {
    private final Map<String, SmartDevice> devices = new HashMap<>();
    private final SmartDeviceFactory factory = new SmartDeviceFactory();

    public void addDevice(String type, String name) {
        if (devices.containsKey(name)) return;
        SmartDevice device = factory.createDevice(type, name);
        devices.put(name, device);
    }

    public void removeDevice(String name) {
        devices.remove(name);
    }

    public void changeDeviceState(String name, String state) {
        SmartDevice device = devices.get(name);
        if (device != null) device.changeState(state);
    }

    public SmartDevice getDevice(String name) {
        return devices.get(name);
    }

    public void clearAllDevices() {
    }
}
