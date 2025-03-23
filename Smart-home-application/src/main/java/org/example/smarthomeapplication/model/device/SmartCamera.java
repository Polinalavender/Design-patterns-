package org.example.smarthomeapplication.model.device;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Color;
import java.awt.Graphics2D;

public class SmartCamera extends SmartDevice {
    private static final String PHOTOS_DIRECTORY = "camera_photos";
    private boolean isRecording;
    private boolean isNightMode;

    public SmartCamera(String name) {
        super(name);
        this.status = "off";
        this.isRecording = false;
        this.isNightMode = false;

        // Create photos directory if it doesn't exist
        createPhotosDirectory();
    }

    private void createPhotosDirectory() {
        try {
            Path photosPath = Paths.get(PHOTOS_DIRECTORY, deviceName);
            Files.createDirectories(photosPath);
            System.out.println("Photos directory created at: " + photosPath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to create photos directory: " + e.getMessage());
        }
    }

    @Override
    public void changeState(String newState) {
        if (!isActive) {
            System.out.println("Device is disconnected");
            return;
        }

        switch (newState.toLowerCase()) {
            case "off":
                this.status = "off";
                this.isRecording = false;
                this.isNightMode = false;
                notifyObservers("Camera is off");
                break;
            case "on":
                this.status = "on";
                this.isRecording = false;
                this.isNightMode = false;
                notifyObservers("Camera is on (standby)");
                break;
            case "recording":
                this.status = "recording";
                this.isRecording = true;
                this.isNightMode = false;
                notifyObservers("Camera is now recording");
                takePhoto(); // Take initial photo when recording starts
                break;
            case "night mode":
                this.status = "night mode";
                this.isRecording = true;
                this.isNightMode = true;
                notifyObservers("Camera is in night mode recording");
                takePhoto(); // Take initial photo when night mode starts
                break;
            default:
                System.out.println("Invalid camera state: " + newState);
        }
    }

    /**
     * Takes a photo if the camera is in recording mode
     * @return true if photo was taken successfully, false otherwise
     */
    public boolean takePhoto() {
        if (!isActive) {
            System.out.println("Cannot take photo: Camera is disconnected");
            return false;
        }

        if (!isRecording) {
            System.out.println("Cannot take photo: Camera is not in recording mode");
            return false;
        }

        try {
            // Create a unique filename based on timestamp
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
            String filename = deviceName + "_" + formatter.format(now) + ".png";

            // Take screenshot as a simulation of a camera photo
            Robot robot = new Robot();
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage screenCapture = robot.createScreenCapture(screenRect);

            // Convert to black and white if in night mode
            if (isNightMode) {
                screenCapture = convertToBlackAndWhite(screenCapture);
            }

            // Save the image
            File outputFile = new File(PHOTOS_DIRECTORY + "/" + deviceName + "/" + filename);
            ImageIO.write(screenCapture, "png", outputFile);

            // Notify users about the new photo
            notifyObservers("Photo taken: " + filename + ". View it in the gallery.");

            return true;
        } catch (Exception e) {
            System.err.println("Failed to take photo: " + e.getMessage());
            return false;
        }
    }

    /**
     * Converts a color image to black and white
     * @param original the original color image
     * @return black and white version of the image
     */
    private BufferedImage convertToBlackAndWhite(BufferedImage original) {
        BufferedImage blackAndWhite = new BufferedImage(
                original.getWidth(),
                original.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY);

        Graphics2D g2d = blackAndWhite.createGraphics();
        g2d.drawImage(original, 0, 0, null);
        g2d.dispose();

        return blackAndWhite;
    }

    /**
     * Takes multiple photos in sequence
     * @param count number of photos to take
     * @param delayMs delay between photos in milliseconds
     * @return number of photos successfully taken
     */
    public int takePhotoSequence(int count, int delayMs) {
        int successCount = 0;

        for (int i = 0; i < count; i++) {
            if (takePhoto()) {
                successCount++;
            }

            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        notifyObservers("Photo sequence completed: " + successCount + "/" + count + " photos taken. View them in the gallery.");
        return successCount;
    }

    /**
     * Gets all photos taken by this camera
     * @return array of photo file paths
     */
    public String[] getPhotosList() {
        File photoDir = new File(PHOTOS_DIRECTORY + "/" + deviceName);
        if (!photoDir.exists() || !photoDir.isDirectory()) {
            return new String[0];
        }

        return photoDir.list((dir, name) -> name.toLowerCase().endsWith(".png"));
    }

    /**
     * Gets the full path to a photo
     * @param photoName name of the photo file
     * @return full path to the photo
     */
    public String getPhotoPath(String photoName) {
        return PHOTOS_DIRECTORY + "/" + deviceName + "/" + photoName;
    }

    /**
     * Deletes all photos taken by this camera
     * @return true if successful, false otherwise
     */
    public boolean clearAllPhotos() {
        File photoDir = new File(PHOTOS_DIRECTORY + "/" + deviceName);
        if (!photoDir.exists() || !photoDir.isDirectory()) {
            return false;
        }

        boolean success = true;
        File[] photos = photoDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));

        if (photos != null) {
            for (File photo : photos) {
                if (!photo.delete()) {
                    success = false;
                }
            }
        }

        if (success) {
            notifyObservers("All photos cleared from " + deviceName);
        }

        return success;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public boolean isNightMode() {
        return isNightMode;
    }
}