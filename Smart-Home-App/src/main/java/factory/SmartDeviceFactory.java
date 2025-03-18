package factory;
import device.SmartDevice;

public class SmartDeviceFactory {
    public device.SmartDevice createDevice(String type, String name) {
        return switch (type.toLowerCase()) {
            case "light" -> new device.SmartLight(name);
            case "thermostat" -> new device.SmartThermostat(name);
            case "camera" -> new device.SmartCamera(name);
            case "doorbell" -> new device.SmartDoorbell(name);
            default -> throw new IllegalArgumentException("Unknown device type: " + type);
        };
    }
}

