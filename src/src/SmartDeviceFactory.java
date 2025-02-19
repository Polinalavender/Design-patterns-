public class SmartDeviceFactory {
    public static SmartDevice createDevice(String type, String name) {
        return switch (type.toLowerCase()) {
            case "light" -> new SmartLight(name);
            case "thermostat" -> new SmartThermostat(name);
            case "camera" -> new SmartCamera(name);
            default -> throw new IllegalArgumentException("Unknown device type: " + type);
        };
    }
}
