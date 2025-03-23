package org.example.smarthomeapplication.model.device;

public class SmartLight extends SmartDevice {
    private int brightness;
    private String color;

    public SmartLight(String name) {
        super(name);
        this.status = "off";
        this.brightness = 100; // Default to 100%
        this.color = "white";  // Default color
    }

    @Override
    public void changeState(String newState) {
        if (!isActive) {
            System.out.println("Device is disconnected");
            return;
        }

        // Handle on/off states
        if (newState.equalsIgnoreCase("on") || newState.equalsIgnoreCase("off")) {
            this.status = newState.toLowerCase();
            notifyObservers("Light is " + status);
            return;
        }

        // Handle brightness changes
        if (newState.startsWith("brightness:")) {
            try {
                String brightnessStr = newState.substring("brightness:".length());
                int newBrightness = Integer.parseInt(brightnessStr);

                if (newBrightness >= 0 && newBrightness <= 100) {
                    this.brightness = newBrightness;
                    notifyObservers("Brightness changed to " + brightness + "%");
                } else {
                    notifyObservers("Invalid brightness value: " + newBrightness + " (must be 0-100)");
                }
            } catch (NumberFormatException e) {
                notifyObservers("Invalid brightness format");
            }
            return;
        }

        // Handle color changes
        if (newState.startsWith("color:")) {
            String newColor = newState.substring("color:".length()).toLowerCase();
            switch (newColor) {
                case "red", "blue", "pink", "white", "green", "yellow", "purple", "orange" -> {
                    this.color = newColor;
                    notifyObservers("Color changed to " + color);
                }
                default -> notifyObservers("Unsupported color: " + newColor);
            }
            return;
        }

        // If we get here, it's an unrecognized state
        notifyObservers("Unrecognized state: " + newState);
    }

    public int getBrightness() {
        return brightness;
    }

    public String getColor() {
        return color;
    }

    @Override
    public String getStatus() {
        if (status.equalsIgnoreCase("off")) {
            return "off";
        } else {
            return status + " (Brightness: " + brightness + "%, Color: " + color + ")";
        }
    }
}