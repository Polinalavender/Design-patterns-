import java.util.ArrayList;
import java.util.List;

public class SmartHomeController {
    private final List<SmartDevice> devices = new ArrayList<>();
    public void addDevice(String type, String name) {
        SmartDevice device = SmartDeviceFactory.createDevice(type, name);
        devices.add(device);
        System.out.println(name + " (" + type + ") added to system.");
    }

    public void removeDevice(String name) {
        devices.removeIf(device -> device.deviceName.equals(name));
        System.out.println(name + " removed from system.");
    }

    public void changeDeviceState(String name, String newState) {
        for (SmartDevice device : devices) {
            if (device.deviceName.equals(name)) {
                device.changeState(newState);
                return;
            }
        }
        System.out.println("Device not found: " + name);
    }

    public void subscribeUser(String deviceName, User user) {
        for (SmartDevice device : devices) {
            if (device.deviceName.equals(deviceName)) {
                device.addObserver(user);
                System.out.println(user + " subscribed to " + deviceName);
                return;
            }
        }
        System.out.println("Device not found: " + deviceName);
    }
}
