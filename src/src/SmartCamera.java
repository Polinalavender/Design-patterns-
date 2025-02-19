public class SmartCamera extends SmartDevice {
    public SmartCamera(String deviceName) {
        super(deviceName);
    }

    @Override
    public void changeState(String newState) {
        this.status = newState.equalsIgnoreCase("RECORDING") ? "RECORDING" : "IDLE";
        notifyObservers();
    }
}
