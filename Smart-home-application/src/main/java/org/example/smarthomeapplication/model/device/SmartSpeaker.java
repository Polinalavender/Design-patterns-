package com.smarthome.model.device;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SmartSpeaker extends SmartDevice {

    private int volumeLevel;
    private List<String> songQueue;
    private String currentSong;


    public SmartSpeaker(String name) {
        super(name);
        this.status = "paused";
        this.volumeLevel = 50;  // Default volume at 50
        this.songQueue = new ArrayList<>();
        this.currentSong = "None";  // No song is playing initially
    }


    @Override
    public void changeState(String newState) {
        if (!isActive) {
            System.out.println("Device is disconnected.");
            return;
        }

        newState = newState.toLowerCase();

        switch (newState) {
            case "playing":
                if (songQueue.isEmpty()) {
                    System.out.println("No songs in the queue. Please add songs first.");
                } else {
                    this.status = newState;
                    this.currentSong = songQueue.get(0);
                    songQueue.remove(0);
                    notifyObservers("Smart Speaker is now playing: " + currentSong);
                }
                break;

            case "paused":
                this.status = newState;
                notifyObservers("Smart Speaker is now paused.");
                break;

            case "muted":
                this.status = newState;
                this.volumeLevel = 0;
                notifyObservers("Smart Speaker is now muted.");
                break;

            case "volume up":
                if (volumeLevel < 100) {
                    volumeLevel += 5;  // Increase volume in increments of 5
                    this.status = "volume up";
                    notifyObservers("Smart Speaker volume increased to: " + volumeLevel);
                } else {
                    System.out.println("Volume is already at maximum.");
                }
                break;

            case "volume down":
                if (volumeLevel > 0) {
                    volumeLevel -= 5;  // Decrease volume in increments of 5
                    this.status = "volume down";
                    notifyObservers("Smart Speaker volume decreased to: " + volumeLevel);
                } else {
                    System.out.println("Volume is already at minimum.");
                }
                break;

            default:
                System.out.println("Invalid state for Smart Speaker.");
        }
    }

    // add a song to the queue
    public void addSongToQueue(String song) {
        if (!isActive) {
            System.out.println("Device is disconnected.");
            return;
        }

        if (song == null || song.isEmpty()) {
            System.out.println("Invalid song name.");
            return;
        }

        songQueue.add(song);
        notifyObservers("Song \"" + song + "\" has been added to the queue.");
    }

    //skip to the next song
    public void skipSong() {
        if (!isActive) {
            System.out.println("Device is disconnected.");
            return;
        }

        if (status.equals("paused") || status.equals("playing")) {
            if (songQueue.isEmpty()) {
                System.out.println("No more songs in the queue.");
            } else {
                this.currentSong = songQueue.get(0);
                songQueue.remove(0);
                notifyObservers("Skipping to next song: " + currentSong);
            }
        } else {
            System.out.println("Cannot skip song while device is in " + status + " state.");
        }
    }


    public void randomFailure() {
        if (new Random().nextInt(10) < 3) {  // 30% chance of failure
            isActive = false;
            System.out.println("Smart Speaker has encountered an error and is disconnected.");
        }
    }

    
    public void repairDevice() {
        if (!isActive) {
            isActive = true;
            System.out.println("Smart Speaker has been repaired and is now reconnected.");
        } else {
            System.out.println("Device is already connected.");
        }
    }

    // Method to display device info (including volume and song queue)
    public void displayDeviceInfo() {
        System.out.println("Device Name: " + getName());
        System.out.println("Current Status: " + status);
        System.out.println("Current Volume Level: " + volumeLevel);
        System.out.println("Current Song Playing: " + currentSong);
        System.out.println("Songs in Queue: " + songQueue);
    }
}
