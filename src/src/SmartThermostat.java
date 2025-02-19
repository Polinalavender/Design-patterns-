public class SmartThermostat extends SmartDevice {
    public SmartThermostat(String deviceName) {
        super(deviceName);
    }

    @Override
    public void changeState(String newState) {
        this.status = newState + "°C";
        notifyObservers();
    }
}
