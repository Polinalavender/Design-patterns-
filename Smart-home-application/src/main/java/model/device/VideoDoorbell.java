package model.device;

public class VideoDoorbell extends SmartDevice {
    public VideoDoorbell(String name) {
        super(name);
        this.status = "standby";
    }

    @Override
    public void changeState(String newState) {
        if (!isActive) {
            System.out.println("Device is disconnected");
            return;
        }
        this.status = newState;
        notifyObservers("Doorbell status: " + status);
    }
}
