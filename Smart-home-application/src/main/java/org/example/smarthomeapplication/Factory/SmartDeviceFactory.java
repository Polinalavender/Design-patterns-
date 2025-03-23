package org.example.smarthomeapplication.Factory;

import org.example.smarthomeapplication.model.device.*;

public class SmartDeviceFactory {
    public SmartDevice createDevice(String type, String name) {
        return switch (type.toLowerCase()) {
            case "light" -> new SmartLight(name);
            case "thermostat" -> new SmartThermostat(name);
            case "camera" -> new SmartCamera(name);
            case "voice assistant" -> new SmartVoiceAssistant(name);
            default -> throw new IllegalArgumentException("Unknown device type: " + type);
        };
    }
}
