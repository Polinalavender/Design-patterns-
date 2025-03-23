package org.example.smarthomeapplication.model.device;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.nio.file.Paths;
import java.nio.file.Files;

public class VideoDoorbell extends SmartDevice {
    private boolean isRinging;
    private boolean isMuted;
    private int volume;
    private String lastVisitor;
    private List<String> visitorHistory;
    private boolean motionDetection;
    private boolean nightVision;
    private String ringtoneStyle;
    private Clip audioClip;
    private Map<String, String> soundFiles;
    private Map<String, String> voiceResponses;
    private String lastCapturedImagePath;
    private boolean isConversationActive;
    private Thread ringTimeoutThread;
    private static final String SOUND_DIR = "src/main/resources/sounds/";

    // Track doorbell state with enum for clarity
    public enum DoorbellState {
        STANDBY, RINGING, ANSWERED, MOTION_DETECTED, RECORDING, CONVERSATION
    }

    private DoorbellState currentState;

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
        this.currentState = DoorbellState.STANDBY;
        this.isConversationActive = false;

        // Initialize sound files
        initializeSoundFiles();
        initializeVoiceResponses();

        // Create sound directory if it doesn't exist
        createSoundDirectory();
    }

    private void createSoundDirectory() {
        try {
            Files.createDirectories(Paths.get(SOUND_DIR));
        } catch (Exception e) {
            System.err.println("Could not create sound directory: " + e.getMessage());
        }
    }

    private void initializeSoundFiles() {
        soundFiles = new HashMap<>();

        // Different ringtone styles
        soundFiles.put("standard", SOUND_DIR + "standard_doorbell.wav");
        soundFiles.put("classic", SOUND_DIR + "classic_doorbell.wav");
        soundFiles.put("modern", SOUND_DIR + "modern_doorbell.wav");
        soundFiles.put("festive", SOUND_DIR + "festive_doorbell.wav");
        soundFiles.put("dog_bark", SOUND_DIR + "dog_bark_doorbell.wav");
        soundFiles.put("disco", SOUND_DIR + "disco_doorbell.wav");

        // System sounds
        soundFiles.put("motion_detected", SOUND_DIR + "motion_alert.wav");
        soundFiles.put("camera_shutter", SOUND_DIR + "camera_shutter.wav");
        soundFiles.put("call_connecting", SOUND_DIR + "call_connecting.wav");
        soundFiles.put("call_ended", SOUND_DIR + "call_ended.wav");
        soundFiles.put("door_unlock", SOUND_DIR + "door_unlock.wav");
    }

    private void initializeVoiceResponses() {
        voiceResponses = new HashMap<>();
        voiceResponses.put("welcome", "Welcome! I'll let the homeowner know you're here.");
        voiceResponses.put("wait", "Please wait a moment.");
        voiceResponses.put("leave_message", "No one is available right now. Would you like to leave a message?");
        voiceResponses.put("thanks", "Thank you for your visit.");
        voiceResponses.put("delivery", "Please leave the package by the door. Thank you!");
    }

    @Override
    public void changeState(String newState) {
        if (!isActive) {
            notifyObservers("Device is disconnected");
            return;
        }

        // Process the state change based on the request
        switch (newState.toLowerCase()) {
            case "standby":
                transitionToStandby();
                break;

            case "ringing":
                ring();
                break;

            case "ring_once":
                ringOnce();
                break;

            case "answered":
                answerCall();
                break;

            case "ignore":
                ignoreCall();
                break;

            case "mute":
                this.isMuted = true;
                notifyObservers("🔕 Doorbell has been muted");
                break;

            case "unmute":
                this.isMuted = false;
                notifyObservers("🔔 Doorbell has been unmuted");
                break;

            case "motion_detected":
                handleMotionDetection();
                break;

            case "unlock_door":
                unlockDoor();
                break;

            case "speak":
            case "speak:welcome":
                speakToVisitor("welcome");
                break;

            case "speak:wait":
                speakToVisitor("wait");
                break;

            case "speak:leave_message":
                speakToVisitor("leave_message");
                break;

            case "speak:thanks":
                speakToVisitor("thanks");
                break;

            case "speak:delivery":
                speakToVisitor("delivery");
                break;

            default:
                handleAdditionalCommands(newState);
                break;
        }
    }

    private void handleAdditionalCommands(String command) {
        // Check if it's a volume change command
        if (command.startsWith("volume:")) {
            try {
                int newVolume = Integer.parseInt(command.substring(7));
                if (newVolume >= 0 && newVolume <= 100) {
                    this.volume = newVolume;
                    notifyObservers("🔊 Doorbell volume set to " + newVolume + "%");

                    // If something is currently playing, adjust its volume
                    if (audioClip != null && audioClip.isActive()) {
                        adjustClipVolume(audioClip, newVolume);
                    }
                } else {
                    notifyObservers("❌ Invalid volume level. Use 0-100");
                }
            } catch (NumberFormatException e) {
                notifyObservers("❌ Invalid volume format");
            }
        }
        // Check if it's a ringtone change command
        else if (command.startsWith("ringtone:")) {
            String style = command.substring(9);
            setRingtoneStyle(style);
        }
        // Check if it's a custom voice message
        else if (command.startsWith("say:")) {
            String message = command.substring(4);
            if (!message.isEmpty()) {
                notifyObservers("🗣️ Doorbell says: \"" + message + "\"");
                // In a real implementation, this would use text-to-speech
            }
        }
        // Check if it's a night vision toggle
        else if (command.equals("night_vision_on")) {
            this.nightVision = true;
            notifyObservers("🌙 Night vision enabled");
        }
        else if (command.equals("night_vision_off")) {
            this.nightVision = false;
            notifyObservers("☀️ Night vision disabled");
        }
        // Toggle motion detection
        else if (command.equals("motion_detection_on")) {
            this.motionDetection = true;
            notifyObservers("👁️ Motion detection enabled");
        }
        else if (command.equals("motion_detection_off")) {
            this.motionDetection = false;
            notifyObservers("👁️‍🗨️ Motion detection disabled");
        }
        else if (command.equals("take_snapshot")) {
            takeSnapshot();
        }
        else if (command.equals("start_recording")) {
            startRecording();
        }
        else if (command.equals("stop_recording")) {
            stopRecording();
        }
        else {
            // For any other state, just update the status
            this.status = command;
            notifyObservers("Doorbell status changed to: " + status);
        }
    }

    private void transitionToStandby() {
        stopAllSounds();
        this.status = "standby";
        this.isRinging = false;
        this.currentState = DoorbellState.STANDBY;

        // Cancel any pending ring timeout
        if (ringTimeoutThread != null && ringTimeoutThread.isAlive()) {
            ringTimeoutThread.interrupt();
        }

        notifyObservers("🚪 Doorbell is now in standby mode");
    }

    private void ringOnce() {
        if (isMuted) {
            notifyObservers("🔕 Doorbell is muted - silent ring");
            return;
        }

        this.isRinging = true;
        playSound(ringtoneStyle);

        // Record the visitor (in a real app, this would capture from camera)
        recordVisit("Visitor");

        // Don't change the state permanently - this is a one-time ring
        notifyObservers("🛎️ Doorbell rang once");
    }

    private void ring() {
        if (currentState == DoorbellState.RINGING) {
            notifyObservers("🔄 Doorbell already ringing");
            return;
        }

        this.status = "ringing";
        this.isRinging = true;
        this.currentState = DoorbellState.RINGING;

        // Take a snapshot when someone rings
        takeSnapshot();

        // Record the visitor
        recordVisit("Visitor");

        if (!isMuted) {
            // Start repeating doorbell sound
            startRepeatingRing();
        }

        notifyObservers("🔔 Doorbell is ringing! Visitor detected");

        // Auto-timeout after 30 seconds
        startRingTimeout();
    }

    private void startRepeatingRing() {
        // In a real implementation, this would use a scheduled executor
        // to repeat the sound at intervals
        playSound(ringtoneStyle);

        // Simulate repeating by playing multiple times
        new Thread(() -> {
            try {
                Thread.sleep(3000); // Wait 3 seconds between rings
                if (isRinging && !isMuted) {
                    playSound(ringtoneStyle);
                }
                Thread.sleep(3000);
                if (isRinging && !isMuted) {
                    playSound(ringtoneStyle);
                }
            } catch (InterruptedException e) {
                // Thread was interrupted, stop ringing
            }
        }).start();
    }

    private void startRingTimeout() {
        // Create a timeout thread
        ringTimeoutThread = new Thread(() -> {
            try {
                // Wait 30 seconds
                Thread.sleep(30000);
                // If still ringing, transition to standby
                if (currentState == DoorbellState.RINGING) {
                    transitionToStandby();
                    notifyObservers("⏱️ Doorbell ring timed out - no answer");
                }
            } catch (InterruptedException e) {
                // Thread was interrupted, which is expected if the call is answered
            }
        });
        ringTimeoutThread.start();
    }

    private void answerCall() {
        stopAllSounds();
        this.status = "answered";
        this.isRinging = false;
        this.currentState = DoorbellState.ANSWERED;
        this.isConversationActive = true;

        // Play connection sound
        playSound("call_connecting");

        notifyObservers("📱 Doorbell call answered - conversation active");
    }

    private void ignoreCall() {
        stopAllSounds();
        this.status = "ignored";
        this.isRinging = false;
        this.currentState = DoorbellState.STANDBY;

        notifyObservers("🔕 Doorbell call ignored");
    }

    private void speakToVisitor(String messageKey) {
        if (!isConversationActive && currentState != DoorbellState.RINGING) {
            notifyObservers("❌ Cannot speak - no active conversation or ring");
            return;
        }

        String message = voiceResponses.getOrDefault(messageKey, "Hello");
        notifyObservers("🗣️ To visitor: \"" + message + "\"");
    }

    private void unlockDoor() {
        // Play door unlock sound
        playSound("door_unlock");
        notifyObservers("🔓 Door unlocked");
    }

    private void takeSnapshot() {
        // Play camera shutter sound
        playSound("camera_shutter");

        // Generate a filename for the snapshot
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        lastCapturedImagePath = "doorbell_snapshot_" + timestamp + ".jpg";

        notifyObservers("📸 Snapshot taken: " + lastCapturedImagePath);
    }

    private void startRecording() {
        this.status = "recording";
        this.currentState = DoorbellState.RECORDING;

        // Generate a filename for the recording
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String recordingFile = "doorbell_recording_" + timestamp + ".mp4";

        notifyObservers("🎥 Started recording: " + recordingFile);
    }

    private void stopRecording() {
        if (this.currentState == DoorbellState.RECORDING) {
            this.status = "standby";
            this.currentState = DoorbellState.STANDBY;
            notifyObservers("⏹️ Recording stopped");
        }
    }

    private void handleMotionDetection() {
        if (!motionDetection) {
            return;
        }

        this.status = "motion_detected";
        this.currentState = DoorbellState.MOTION_DETECTED;

        // Play motion alert sound, but only if not muted
        if (!isMuted) {
            playSound("motion_detected");
        }

        // Check if night vision should be activated automatically
        boolean isDark = isNightTime();
        if (isDark && !nightVision) {
            nightVision = true;
            notifyObservers("🌙 Low light detected - Night vision automatically enabled");
        }

        notifyObservers("🚶 Motion detected near doorbell");

        // Take a snapshot when motion is detected
        takeSnapshot();
    }

    // Play a sound file based on the configured ringtone style
    private void playSound(String soundKey) {
        if (isMuted) return;

        String soundFile = soundFiles.getOrDefault(soundKey, soundFiles.get("standard"));

        try {
            playWithJavaSound(soundFile);
        } catch (Exception e) {
            System.err.println("Could not play sound: " + e.getMessage());
            // Just notify without attempting to play
            notifyObservers("🔊 Would play sound: " + new File(soundFile).getName());
        }
    }

    private void playWithJavaSound(String soundFile) {
        try {
            File file = new File(soundFile);
            if (file.exists()) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
                audioClip = AudioSystem.getClip();
                audioClip.open(audioStream);
                adjustClipVolume(audioClip, volume);
                audioClip.start();
                notifyObservers("🔊 Playing sound: " + file.getName());
            } else {
                // File doesn't exist, just simulate
                notifyObservers("🔊 Playing sound (simulated): " + file.getName());
            }
        } catch (Exception e) {
            System.err.println("Error playing sound: " + e.getMessage());
        }
    }

    private void adjustClipVolume(Clip clip, int volumeLevel) {
        try {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

            // Convert volume (0-100) to gain in dB (-80.0 to 6.0)
            float gain = (volumeLevel / 100.0f) * 86.0f - 80.0f;
            gainControl.setValue(gain);
        } catch (Exception e) {
            System.err.println("Could not adjust volume: " + e.getMessage());
        }
    }

    private void stopAllSounds() {
        // Stop any playing sounds
        if (audioClip != null && audioClip.isActive()) {
            audioClip.stop();
            audioClip.close();
        }
    }

    private boolean isNightTime() {
        // Simple simulation - consider 7PM to 6AM as night time
        int hour = LocalDateTime.now().getHour();
        return hour >= 19 || hour < 6;
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
                notifyObservers("🎵 Ringtone changed to " + style);
                // Play a sample of the chosen ringtone
                playSound(ringtoneStyle);
                break;
            default:
                this.ringtoneStyle = "standard";
                notifyObservers("⚠️ Unknown ringtone style. Set to standard.");
                playSound("standard");
                break;
        }
    }

    // Getters for the properties
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

    public String getLastCapturedImagePath() {
        return lastCapturedImagePath;
    }

    public DoorbellState getCurrentState() {
        return currentState;
    }

    public boolean isConversationActive() {
        return isConversationActive;
    }

    public void endConversation() {
        if (isConversationActive) {
            isConversationActive = false;

            // Play call ended sound
            playSound("call_ended");

            notifyObservers("📞 Conversation ended");
            transitionToStandby();
        }
    }
}