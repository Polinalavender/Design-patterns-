package org.example.smarthomeapplication.user;

public class User implements Observer {
    private final String name;

    public User(String name) {
        this.name = name;
    }

    @Override
    public void update(String message) {
        System.out.println("[" + name + "] Received notification: " + message);
    }

    public String getName() {
        return name;
    }
}
