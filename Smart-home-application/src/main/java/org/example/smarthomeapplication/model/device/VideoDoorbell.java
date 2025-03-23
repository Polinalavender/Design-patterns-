package org.example.smarthomeapplication.model.device;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class VideoDoorbell extends SmartDevice {
    private boolean isRinging;
    private boolean isMuted;
    private int volume;
    private String lastVisitor;
    private List<String> visitorHistory;
    private boolean motionDetection;
    private boolean nightVision;
    private String ringtoneStyle;

    public VideoDoorbell(String name) {
        super(name);
        this.status = "standby";
        this.isRinging = false;
        this.isMuted = false;
        this.volume = 75; // Default volume (0-100)
        this.lastVisitor = "None";
        this.visitorHistory = new ArrayList<>();
        this.motionDetection = true;
        this.nightVision = false;
        this.ringtoneStyle = "standard";
    }

    @Override
    public void changeState(String newState) {
        if (!isActive) {
            System.out.println("Device is disconnected");
            return;
        }

        // Process the state change based on the request
        switch (newState.toLowerCase()) {
            case "standby":
                this.status = "standby";
                this.isRinging = false;
                notifyObservers("Doorbell is now in standby mode");
                break;

            case "ringing":
                this.status = "ringing";
                this.isRinging = true;
                recordVisit("Someone");
                if (!isMuted) {
                    playRingtone();
                }
                notifyObservers("Doorbell is ringing! Visitor detected");
                break;

            case "answered":
                this.status = "answered";
                this.isRinging = false;
                notifyObservers("Doorbell call answered");
                break;

            case "mute":
                this.isMuted = true;
                notifyObservers("Doorbell has been muted");
                break;

            case "unmute":
                this.isMuted = false;
                notifyObservers("Doorbell has been unmuted");
                break;

            case "motion_detected":
                handleMotionDetection();
                break;

            default:
                // Check if it's a volume change command
                if (newState.startsWith("volume:")) {
                    try {
                        int newVolume = Integer.parseInt(newState.substring(7));
                        if (newVolume >= 0 && newVolume <= 100) {
                            this.volume = newVolume;
                            notifyObservers("Doorbell volume set to " + newVolume + "%");
                        } else {
                            notifyObservers("Invalid volume level. Use 0-100");
                        }
                    } catch (NumberFormatException e) {
                        notifyObservers("Invalid volume format");
                    }
                }
                // Check if it's a ringtone change command
                else if (newState.startsWith("ringtone:")) {
                    String style = newState.substring(9);
                    setRingtoneStyle(style);
                }
                // Check if it's a night vision toggle
                else if (newState.equals("night_vision_on")) {
                    this.nightVision = true;
                    notifyObservers("Night vision enabled");
                }
                else if (newState.equals("night_vision_off")) {
                    this.nightVision = false;
                    notifyObservers("Night vision disabled");
                }
                // Toggle motion detection
                else if (newState.equals("motion_detection_on")) {
                    this.motionDetection = true;
                    notifyObservers("Motion detection enabled");
                }
                else if (newState.equals("motion_detection_off")) {
                    this.motionDetection = false;
                    notifyObservers("Motion detection disabled");
                }
                else {
                    // For any other state, just update the status
                    this.status = newState;
                    notifyObservers("Doorbell status changed to: " + status);
                }
                break;
        }
    }

    private void playRingtone() {
        // Simulate playing different ringtone styles
        String sound;
        switch (ringtoneStyle.toLowerCase()) {
            case "classic":
                sound = "DING DONG";
                break;
            case "modern":
                sound = "BUZZ BUZZ";
                break;
            case "festive":
                sound = "JINGLE JINGLE";
                break;
            case "dog_bark":
                sound = "WOOF WOOF";
                break;
            case "disco":
                sound = "DJ DOORBELL IN THE HOUSE";
                break;
            default:
                sound = "DING DONG";
                break;
        }

        notifyObservers("🔔 " + sound + " (Volume: " + volume + "%)");
    }

    private void handleMotionDetection() {
        if (!motionDetection) {
            return;
        }

        this.status = "motion_detected";

        // Check if night vision should be activated automatically
        boolean isDark = isNightTime();
        if (isDark && !nightVision) {
            nightVision = true;
            notifyObservers("Low light detected - Night vision automatically enabled");
        }

        notifyObservers("Motion detected near doorbell");

        // Simulate a photo or video being taken
        captureEvent();
    }

    private boolean isNightTime() {
        // Simple simulation - consider 7PM to 6AM as night time
        int hour = LocalDateTime.now().getHour();
        return hour >= 19 || hour < 6;
    }

    private void captureEvent() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String eventId = "event_" + System.currentTimeMillis();

        notifyObservers("Security event recorded: " + eventId + " at " + timestamp +
                (nightVision ? " (Night Vision)" : ""));
    }

    private void recordVisit(String visitor) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.lastVisitor = visitor + " at " + timestamp;
        this.visitorHistory.add(lastVisitor);

        // Keep history to a reasonable size
        if (visitorHistory.size() > 20) {
            visitorHistory.remove(0);
        }
    }

    public void setRingtoneStyle(String style) {
        switch (style.toLowerCase()) {
            case "classic":
            case "modern":
            case "festive":
            case "dog_bark":
            case "disco":
                this.ringtoneStyle = style.toLowerCase();
                notifyObservers("Ringtone changed to " + style);
                break;
            default:
                this.ringtoneStyle = "standard";
                notifyObservers("Unknown ringtone style. Set to standard.");
                break;
        }
    }

    // Getters for the new properties
    public boolean isRinging() {
        return isRinging;
    }

    public boolean isMuted() {
        return isMuted;
    }

    public int getVolume() {
        return volume;
    }

    public String getLastVisitor() {
        return lastVisitor;
    }

    public List<String> getVisitorHistory() {
        return new ArrayList<>(visitorHistory);
    }

    public boolean isMotionDetectionEnabled() {
        return motionDetection;
    }

    public boolean isNightVisionEnabled() {
        return nightVision;
    }

    public String getRingtoneStyle() {
        return ringtoneStyle;
    }
}