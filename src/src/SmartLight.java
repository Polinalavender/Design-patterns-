public class SmartLight extends SmartDevice {
    public SmartLight(String deviceName) {
        super(deviceName);
    }

    @Override
    public void changeState(String newState) {
        this.status = newState.equalsIgnoreCase("ON") ? "ON" : "OFF";
        notifyObservers();
    }
}
