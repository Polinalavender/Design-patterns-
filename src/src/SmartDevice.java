import java.util.ArrayList;
import java.util.List;

public abstract class SmartDevice {
        protected String deviceName;
        protected String status;
        private final List<Observer> observers = new ArrayList<>();

        public SmartDevice(String deviceName) {
            this.deviceName = deviceName;
            this.status = "OFF";
        }

        public abstract void changeState(String newState);

        public void addObserver(Observer observer) {
            observers.add(observer);
        }

        public void removeObserver(Observer observer) {
            observers.remove(observer);
        }

        protected void notifyObservers() {
            for (Observer observer : observers) {
                observer.update(deviceName + " state changed to " + status);
            }
        }
}
