package model.device;

import org.example.smarthomeapplication.user.Observer;

import java.util.ArrayList;
import java.util.List;

public abstract class SmartDevice {
    protected String deviceName;
    protected String status;
    protected boolean isActive;
    protected List<Observer> observers = new ArrayList<>();

    public SmartDevice(String deviceName) {
        this.deviceName = deviceName;
        this.isActive = true;
    }

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    protected void notifyObservers(String message) {
        for (Observer observer : observers) {
            observer.update(deviceName + ": " + message);
        }
    }

    public abstract void changeState(String newState);

    public String getDeviceName() {
        return deviceName;
    }

    public String getStatus() {
        return status;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
        notifyObservers(active ? "Device connected" : "Device disconnected");
    }
}
