package org.example.smarthomeapplication.model.device;

import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

public class SmartVoiceAssistant extends SmartDevice {
    private String listeningMode; // "active", "passive", "muted"
    private int volume;
    private List<String> supportedCommands;
    private Map<String, String> commandHistory;
    private String activeConversation;
    private boolean isProcessingCommand;
    private final Random random = new Random();
    private ResponseGenerator responseGenerator;

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
        this.responseGenerator = new ResponseGenerator();
    }

    private class ResponseGenerator {
        private class ResponseTemplate {
            List<String> variations;

            ResponseTemplate(String... templates) {
                this.variations = Arrays.asList(templates);
            }

            String generate(Map<String, String> context) {
                // Select a random variation
                String template = variations.get(random.nextInt(variations.size()));

                // Replace placeholders with context values
                for (Map.Entry<String, String> entry : context.entrySet()) {
                    template = template.replace("{{" + entry.getKey() + "}}", entry.getValue());
                }

                return template;
            }
        }

        private Map<String, ResponseTemplate> responseTemplates = new HashMap<>();

        ResponseGenerator() {
            // Temperature response templates with multiple variations
            responseTemplates.put("temperature", new ResponseTemplate(
                    "The temperature is currently {{temp}}°C. Would you like me to adjust it?",
                    "I'm detecting a room temperature of {{temp}}°C. Shall I make any changes?",
                    "Current reading shows {{temp}}°C. How are you feeling about the temperature?",
                    "Checking the thermostat... we're sitting at {{temp}}°C right now."
            ));

            // Time response templates
            responseTemplates.put("time", new ResponseTemplate(
                    "The current time is {{time}}. It's {{dayOfWeek}} today.",
                    "Right now, it's {{time}} on {{dayOfWeek}}, {{date}}.",
                    "My internal clock says it's {{time}} on {{dayOfWeek}}, {{date}}.",
                    "Synchronizing time... we're at {{time}}, {{dayOfWeek}} {{date}}."
            ));

            // Security response templates
            responseTemplates.put("security", new ResponseTemplate(
                    "{{action}} security system in progress...",
                    "Executing {{action}} for home security.",
                    "Processing security {{action}} request.",
                    "Security protocol for {{action}} is now active."
            ));

            // Weather response templates
            responseTemplates.put("weather", new ResponseTemplate(
                    "Let me check the forecast for {{period}}...",
                    "Weather update for {{period}} incoming.",
                    "Retrieving meteorological data for {{period}}.",
                    "Scanning atmospheric conditions for {{period}}."
            ));
        }

        // Generate a contextual response
        String generateResponse(String type, Map<String, String> context) {
            ResponseTemplate template = responseTemplates.get(type);
            return template != null
                    ? template.generate(context)
                    : "I'm not sure how to respond to that right now.";
        }
    }

    private String generateTimeResponse(String command) {
        LocalDateTime now = LocalDateTime.now();
        Map<String, String> context = new HashMap<>();

        context.put("time", now.format(DateTimeFormatter.ofPattern("h:mm a")));
        context.put("dayOfWeek", now.format(DateTimeFormatter.ofPattern("EEEE")));
        context.put("date", now.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));

        return responseGenerator.generateResponse("time", context);
    }

    private String generateTemperatureResponse(double temperature) {
        Map<String, String> context = new HashMap<>();
        context.put("temp", String.format("%.1f", temperature));

        return responseGenerator.generateResponse("temperature", context);
    }

    private String generateSecurityResponse(String action) {
        Map<String, String> context = new HashMap<>();
        context.put("action", action);

        return responseGenerator.generateResponse("security", context);
    }

    private String generateWeatherResponse(String period) {
        Map<String, String> context = new HashMap<>();
        context.put("period", period);

        return responseGenerator.generateResponse("weather", context);
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

                // Dynamic context-aware command matching
                Map<String, Function<String, String>> commandHandlers = new HashMap<>();

                commandHandlers.put("temperature", cmd ->
                        generateTemperatureResponse(22.0)
                );

                commandHandlers.put("time", cmd ->
                        generateTimeResponse(cmd)
                );

                commandHandlers.put("security", cmd -> {
                    List<String> securityActions = Arrays.asList("arm", "disarm", "check", "camera");

                    // Find first matching security action
                    Optional<String> matchedAction = securityActions.stream()
                            .filter(cmd::contains)
                            .findFirst();

                    return matchedAction
                            .map(this::generateSecurityResponse)
                            .orElse("I can help with security. What specific action would you like?");
                });

                commandHandlers.put("weather", cmd -> {
                    List<String> weatherPeriods = Arrays.asList("today", "tomorrow", "weekend", "week");

                    // Find first matching weather period
                    Optional<String> matchedPeriod = weatherPeriods.stream()
                            .filter(cmd::contains)
                            .findFirst();

                    return matchedPeriod
                            .map(this::generateWeatherResponse)
                            .orElse("I can provide weather information. Which timeframe interests you?");
                });

                // Find matching command handler
                String finalCommand1 = command;
                Optional<Function<String, String>> handler = commandHandlers.entrySet().stream()
                        .filter(entry -> finalCommand1.contains(entry.getKey()))
                        .map(Map.Entry::getValue)
                        .findFirst();

                // Fallback responses with randomness
                List<String> fallbackResponses = Arrays.asList(
                        "I'm not quite sure I understood that.",
                        "Could you rephrase that for me?",
                        "I'm listening, but could you be more specific?",
                        "Hmm, I didn't catch that completely."
                );

                // Generate conversational responses
                if (command.contains("hello") || command.contains("hi")) {
                    return "Hello! How can I assist you today?";
                } else if (command.contains("thank")) {
                    return "You're welcome! Is there anything else I can help with?";
                }

                // Execute matching handler or return random fallback
                String finalCommand = command;
                return handler
                        .map(h -> h.apply(finalCommand))
                        .orElse(fallbackResponses.get(random.nextInt(fallbackResponses.size())));
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