package org.example.smarthomeapplication.model.device;

import org.example.smarthomeapplication.user.Observer;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SmartVoiceAssistant extends SmartDevice {
    private String listeningMode; // "active", "passive", "muted"
    private int volume;
    private List<String> supportedCommands;
    private Map<String, String> commandHistory;
    private String activeConversation;
    private boolean isProcessingCommand;

    public SmartVoiceAssistant(String deviceName) {
        super(deviceName);
        this.listeningMode = "passive";
        this.volume = 50;
        this.status = "idle";
        this.supportedCommands = new ArrayList<>();
        this.commandHistory = new LinkedHashMap<>();
        this.activeConversation = "";
        this.isProcessingCommand = false;

        // default supported commands
        supportedCommands.add("lights");
        supportedCommands.add("temperature");
        supportedCommands.add("music");
        supportedCommands.add("camera");
        supportedCommands.add("security");
        supportedCommands.add("weather");
        supportedCommands.add("news");
        supportedCommands.add("time");
        supportedCommands.add("reminders");
        supportedCommands.add("routines");
    }

    @Override
    public void changeState(String newState) {
        switch (newState.toLowerCase()) {
            case "listening":
                listeningMode = "active";
                status = "listening";
                notifyObservers("Now actively listening for commands");
                break;
            case "passive":
                listeningMode = "passive";
                status = "idle";
                notifyObservers("Switched to passive listening mode");
                break;
            case "mute":
                listeningMode = "muted";
                status = "muted";
                notifyObservers("Voice assistant muted");
                break;
            default:
                if (newState.startsWith("volume:")) {
                    try {
                        int newVolume = Integer.parseInt(newState.substring(7));
                        if (newVolume >= 0 && newVolume <= 100) {
                            volume = newVolume;
                            notifyObservers("Volume set to " + volume + "%");
                        } else {
                            notifyObservers("Volume must be between 0 and 100");
                        }
                    } catch (NumberFormatException e) {
                        notifyObservers("Invalid volume format");
                    }
                } else if (newState.startsWith("command:")) {
                    String command = newState.substring(8);
                    processCommand(command);
                } else {
                    notifyObservers("Unknown state: " + newState);
                }
                break;
        }
    }

    public void processCommand(String command) {
        if (listeningMode.equals("muted")) {
            notifyObservers("Cannot process command while muted");
            return;
        }

        isProcessingCommand = true;
        status = "processing";
        notifyObservers("Processing command: " + command);

        // Record in command history
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        commandHistory.put(timestamp, command);

        // Process the command (simulation)
        String response = interpretCommand(command);
        activeConversation = command + "\n" + response;

        // Simulate processing delay and then provide the response
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                notifyObservers("Response: " + response);
                status = listeningMode.equals("active") ? "listening" : "idle";
                isProcessingCommand = false;
            }
        }, 800); // 800ms delay to simulate processing
    }

    private String interpretCommand(String command) {
        command = command.toLowerCase();

        if (command.contains("light") || command.contains("lamp")) {
            return "I'll control your lights. Which room would you like to adjust?";
        } else if (command.contains("temperature") || command.contains("thermostat") || command.contains("heat")) {
            return "Current temperature is 22°C. Would you like me to adjust it?";
        } else if (command.contains("music") || command.contains("play") || command.contains("song")) {
            return "Playing your favorite playlist on the living room speakers.";
        } else if (command.contains("camera") || command.contains("security") || command.contains("monitoring")) {
            return "Accessing security cameras. All systems are normal.";
        } else if (command.contains("weather")) {
            return "Today's forecast shows partly cloudy skies with a high of 24°C.";
        } else if (command.contains("time")) {
            return "The current time is " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        } else if (command.contains("reminder") || command.contains("schedule")) {
            return "I've set a reminder for you. What time would you like me to remind you?";
        } else if (command.contains("routine") || command.contains("scene")) {
            return "Would you like to activate your morning, evening, or away routine?";
        } else if (command.contains("thank")) {
            return "You're welcome! Is there anything else I can help with?";
        } else if (command.contains("hello") || command.contains("hi ")) {
            return "Hello! How can I assist you today?";
        } else {
            return "I'm sorry, I didn't understand that command. Can you try again?";
        }
    }

    public void addSupportedCommand(String command) {
        supportedCommands.add(command);
        notifyObservers("Added new supported command: " + command);
    }

    public List<String> getSupportedCommands() {
        return new ArrayList<>(supportedCommands);
    }

    public Map<String, String> getCommandHistory() {
        return new LinkedHashMap<>(commandHistory);
    }

    public void clearCommandHistory() {
        commandHistory.clear();
        notifyObservers("Command history cleared");
    }

    public String getListeningMode() {
        return listeningMode;
    }

    public int getVolume() {
        return volume;
    }

    public String getActiveConversation() {
        return activeConversation;
    }

    public boolean isProcessingCommand() {
        return isProcessingCommand;
    }
}