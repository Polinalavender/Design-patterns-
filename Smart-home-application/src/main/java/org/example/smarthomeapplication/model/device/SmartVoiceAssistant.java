package org.example.smarthomeapplication.model.device;

import org.example.smarthomeapplication.user.Observer;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmartVoiceAssistant extends SmartDevice {
    private String listeningMode; // "active", "passive", "muted"
    private int volume;
    private Map<String, List<Pattern>> commandPatterns;
    private Map<String, String> commandHistory;
    private String activeConversation;
    private boolean isProcessingCommand;
    private Map<String, Set<String>> entityDatabase;
    private float confidenceThreshold;
    private int contextMemorySize;
    private Deque<String> conversationContext;

    public SmartVoiceAssistant(String deviceName) {
        super(deviceName);
        this.listeningMode = "passive";
        this.volume = 50;
        this.status = "idle";
        this.commandHistory = new LinkedHashMap<>();
        this.activeConversation = "";
        this.isProcessingCommand = false;
        this.confidenceThreshold = 0.7f;
        this.contextMemorySize = 5;
        this.conversationContext = new LinkedList<>();

        initializeCommandPatterns();
        initializeEntityDatabase();
    }

    private void initializeCommandPatterns() {
        commandPatterns = new HashMap<>();

        // Lights commands
        List<Pattern> lightPatterns = new ArrayList<>();
        lightPatterns.add(Pattern.compile("(?i)turn (on|off) (?:the )?(?:lights|light)(?: in | at )?(the )?(.+)?"));
        lightPatterns.add(Pattern.compile("(?i)(dim|brighten)(?: the)? (?:lights|light)(?: in | at )?(the )?(.+)?"));
        lightPatterns.add(Pattern.compile("(?i)set (?:the )?(?:lights|light)(?: in | at )?(the )?(.+)? to (\\d+)(?:%| percent)"));
        commandPatterns.put("lights", lightPatterns);

        // Temperature commands
        List<Pattern> tempPatterns = new ArrayList<>();
        tempPatterns.add(Pattern.compile("(?i)(?:set|change) (?:the )?temperature to (\\d+)(?:°C| degrees)"));
        tempPatterns.add(Pattern.compile("(?i)(increase|decrease|raise|lower) (?:the )?temperature(?: by)? (\\d+)(?:°C| degrees)?"));
        tempPatterns.add(Pattern.compile("(?i)(?:what is|what's|tell me|get) (?:the )?(?:current )?temperature"));
        commandPatterns.put("temperature", tempPatterns);

        // Music commands
        List<Pattern> musicPatterns = new ArrayList<>();
        musicPatterns.add(Pattern.compile("(?i)play(?: some)?(?: music| song)?(?: by)? (.+)"));
        musicPatterns.add(Pattern.compile("(?i)(pause|stop|resume) (?:the )?music"));
        musicPatterns.add(Pattern.compile("(?i)(next|previous|skip) (?:song|track)"));
        musicPatterns.add(Pattern.compile("(?i)(?:set|change) (?:the )?volume to (\\d+)(?:%| percent)"));
        commandPatterns.put("music", musicPatterns);

        // Security commands
        List<Pattern> securityPatterns = new ArrayList<>();
        securityPatterns.add(Pattern.compile("(?i)show me (?:the )?(front door|back door|garage|kitchen|living room|bedroom|bathroom) camera"));
        securityPatterns.add(Pattern.compile("(?i)(arm|disarm) (?:the )?security system"));
        securityPatterns.add(Pattern.compile("(?i)lock (?:the )?(?:front |back |side )?(?:door|doors)"));
        commandPatterns.put("security", securityPatterns);

        // Weather commands
        List<Pattern> weatherPatterns = new ArrayList<>();
        weatherPatterns.add(Pattern.compile("(?i)(?:what's|what is|how's|how is) (?:the )?weather(?: like)?(?: today| tomorrow| this week)?"));
        weatherPatterns.add(Pattern.compile("(?i)is it going to rain(?: today| tomorrow| this week)?"));
        weatherPatterns.add(Pattern.compile("(?i)(?:what's|what is) (?:the )?forecast(?: for)? (.+)?"));
        commandPatterns.put("weather", weatherPatterns);

        // Time commands
        List<Pattern> timePatterns = new ArrayList<>();
        timePatterns.add(Pattern.compile("(?i)(?:what's|what is|tell me) (?:the )?(time|date)"));
        timePatterns.add(Pattern.compile("(?i)(?:what day|day) is (?:it|today)"));
        commandPatterns.put("time", timePatterns);

        // Reminder commands
        List<Pattern> reminderPatterns = new ArrayList<>();
        reminderPatterns.add(Pattern.compile("(?i)remind me to (.+) at (.+)"));
        reminderPatterns.add(Pattern.compile("(?i)set (?:a |an )?(?:reminder|alarm) for (.+)(?: at| in| on) (.+)"));
        reminderPatterns.add(Pattern.compile("(?i)what (?:reminders|alarms) do I have(?: today| tomorrow| this week)?"));
        commandPatterns.put("reminders", reminderPatterns);

        // Routine commands
        List<Pattern> routinePatterns = new ArrayList<>();
        routinePatterns.add(Pattern.compile("(?i)(?:run|start|execute) (?:the )?(.+) (?:routine|scene)"));
        routinePatterns.add(Pattern.compile("(?i)(?:good morning|good night|i'm leaving|i'm home)"));
        commandPatterns.put("routines", routinePatterns);
    }

    private void initializeEntityDatabase() {
        entityDatabase = new HashMap<>();

        // Rooms
        Set<String> rooms = new HashSet<>(Arrays.asList(
                "living room", "kitchen", "bedroom", "bathroom", "master bedroom",
                "guest room", "office", "hallway", "dining room", "basement", "attic"
        ));
        entityDatabase.put("room", rooms);

        // Artists/Genres (simple examples)
        Set<String> music = new HashSet<>(Arrays.asList(
                "rock", "pop", "jazz", "classical", "hip hop", "country",
                "relaxing", "workout", "party", "focus"
        ));
        entityDatabase.put("music", music);

        // Routines
        Set<String> routines = new HashSet<>(Arrays.asList(
                "morning", "evening", "night", "away", "home", "movie", "dinner", "workout"
        ));
        entityDatabase.put("routine", routines);
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
                } else if (newState.startsWith("confidence:")) {
                    try {
                        float newThreshold = Float.parseFloat(newState.substring(11));
                        if (newThreshold >= 0 && newThreshold <= 1) {
                            confidenceThreshold = newThreshold;
                            notifyObservers("Recognition confidence threshold set to " + confidenceThreshold);
                        } else {
                            notifyObservers("Confidence threshold must be between 0 and 1");
                        }
                    } catch (NumberFormatException e) {
                        notifyObservers("Invalid confidence threshold format");
                    }
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

        // Add to conversation context
        updateConversationContext(command);

        // Record in command history
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        commandHistory.put(timestamp, command);

        // Process the command with enhanced NLP
        RecognitionResult result = recognizeCommand(command);

        // Create response based on recognition result
        String response;
        if (result.getConfidence() >= confidenceThreshold) {
            response = generateResponse(result);
        } else {
            // Try to use context memory to improve understanding
            result = applyContextForBetterUnderstanding(command, result);
            if (result.getConfidence() >= confidenceThreshold) {
                response = generateResponse(result);
            } else {
                response = "I'm not quite sure what you mean. Could you please rephrase that?";
            }
        }

        activeConversation = command + "\n" + response;

        // Simulate processing delay and then provide the response
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                updateConversationContext(response);
                notifyObservers("Response: " + response);
                status = listeningMode.equals("active") ? "listening" : "idle";
                isProcessingCommand = false;
            }
        }, 800); // 800ms delay to simulate processing
    }

    private RecognitionResult recognizeCommand(String command) {
        String normalizedCommand = command.toLowerCase().trim();
        float highestConfidence = 0;
        String bestCategory = "";
        Map<String, String> bestEntities = new HashMap<>();
        Pattern bestPattern = null;

        // Try to match against our patterns
        for (Map.Entry<String, List<Pattern>> categoryEntry : commandPatterns.entrySet()) {
            String category = categoryEntry.getKey();
            for (Pattern pattern : categoryEntry.getValue()) {
                Matcher matcher = pattern.matcher(normalizedCommand);
                if (matcher.find()) {
                    // Calculate confidence based on how much of the input was matched
                    int matchLength = matcher.end() - matcher.start();
                    float confidence = (float) matchLength / normalizedCommand.length();

                    // Extract entities from groups
                    Map<String, String> entities = new HashMap<>();
                    for (int i = 1; i <= matcher.groupCount(); i++) {
                        String value = matcher.group(i);
                        if (value != null && !value.isEmpty()) {
                            entities.put("entity" + i, value);
                        }
                    }

                    // Check if entities match our database for higher confidence
                    for (Map.Entry<String, String> entity : entities.entrySet()) {
                        for (Map.Entry<String, Set<String>> dbEntry : entityDatabase.entrySet()) {
                            if (dbEntry.getValue().contains(entity.getValue())) {
                                confidence += 0.1f; // Boost confidence for recognized entities
                                entities.put(dbEntry.getKey(), entity.getValue()); // Label the entity properly
                            }
                        }
                    }

                    // Cap confidence at 1.0
                    confidence = Math.min(confidence, 1.0f);

                    if (confidence > highestConfidence) {
                        highestConfidence = confidence;
                        bestCategory = category;
                        bestEntities = entities;
                        bestPattern = pattern;
                    }
                }
            }
        }

        // If no pattern matched well, try keyword matching as fallback
        if (highestConfidence < 0.5f) {
            String[] keywords = {"light", "temperature", "music", "security", "weather", "time", "remind", "routine"};
            Map<String, Float> categoryScores = new HashMap<>();

            for (String keyword : keywords) {
                if (normalizedCommand.contains(keyword)) {
                    String category = mapKeywordToCategory(keyword);
                    float score = categoryScores.getOrDefault(category, 0f) + 0.2f;
                    categoryScores.put(category, score);
                }
            }

            for (Map.Entry<String, Float> entry : categoryScores.entrySet()) {
                if (entry.getValue() > highestConfidence) {
                    highestConfidence = entry.getValue();
                    bestCategory = entry.getKey();
                    // No specific entities or pattern since we're just doing keyword matching
                }
            }
        }

        return new RecognitionResult(bestCategory, bestEntities, highestConfidence, bestPattern);
    }

    private String mapKeywordToCategory(String keyword) {
        switch (keyword) {
            case "light": return "lights";
            case "temperature":
            case "thermostat":
            case "heat": return "temperature";
            case "music":
            case "play":
            case "song": return "music";
            case "security":
            case "camera": return "security";
            case "weather": return "weather";
            case "time":
            case "date": return "time";
            case "remind":
            case "reminder":
            case "alarm": return "reminders";
            case "routine":
            case "scene": return "routines";
            default: return "unknown";
        }
    }

    private void updateConversationContext(String message) {
        conversationContext.addLast(message);
        while (conversationContext.size() > contextMemorySize) {
            conversationContext.removeFirst();
        }
    }

    private RecognitionResult applyContextForBetterUnderstanding(String command, RecognitionResult initialResult) {
        // If the command is very short, it might be a follow-up
        if (command.split("\\s+").length <= 3 && !conversationContext.isEmpty()) {
            // Look for context in previous exchanges
            List<String> contextList = new ArrayList<>(conversationContext);
            for (int i = contextList.size() - 2; i >= 0; i--) { // Skip the current command
                String previousMessage = contextList.get(i);
                if (previousMessage.contains("lights") || previousMessage.contains("temperature") ||
                        previousMessage.contains("music") || previousMessage.contains("security")) {

                    // Try to determine category from previous context
                    String categoryFromContext = "";
                    if (previousMessage.contains("light")) categoryFromContext = "lights";
                    else if (previousMessage.contains("temperature")) categoryFromContext = "temperature";
                    else if (previousMessage.contains("music") || previousMessage.contains("play")) categoryFromContext = "music";
                    else if (previousMessage.contains("security") || previousMessage.contains("camera")) categoryFromContext = "security";

                    if (!categoryFromContext.isEmpty()) {
                        // Create a new result with higher confidence
                        float newConfidence = Math.min(initialResult.getConfidence() + 0.3f, 1.0f);
                        return new RecognitionResult(
                                categoryFromContext,
                                initialResult.getEntities(),
                                newConfidence,
                                initialResult.getPattern()
                        );
                    }
                }
            }
        }

        // If we couldn't enhance with context, return the original result
        return initialResult;
    }

    private String generateResponse(RecognitionResult result) {
        String category = result.getCategory();
        Map<String, String> entities = result.getEntities();

        switch (category) {
            case "lights":
                if (entities.containsKey("entity1") && entities.containsKey("entity2")) {
                    String action = entities.get("entity1"); // on/off/dim/brighten
                    String location = entities.get("entity2"); // room
                    return "I'll " + action + " the lights in the " + location + ".";
                } else if (entities.containsKey("entity1")) {
                    String action = entities.get("entity1");
                    return "Which room would you like me to " + action + " the lights in?";
                }
                return "I can control your lights. Which room and what would you like me to do?";

            case "temperature":
                if (entities.containsKey("entity1") && entities.containsKey("entity2")) {
                    String action = entities.get("entity1");
                    String amount = entities.get("entity2");
                    if (action.equals("set") || action.equals("change")) {
                        return "Setting temperature to " + amount + "°C.";
                    } else {
                        return action + "ing temperature by " + amount + "°C.";
                    }
                } else if (!entities.isEmpty() && entities.containsKey("entity1")) {
                    if (entities.get("entity1").matches("\\d+")) {
                        return "Setting temperature to " + entities.get("entity1") + "°C.";
                    }
                }
                return "Current temperature is 22°C. Would you like me to adjust it?";

            case "music":
                if (entities.containsKey("entity1")) {
                    String musicCommand = entities.get("entity1");
                    if (musicCommand.equals("pause") || musicCommand.equals("stop") ||
                            musicCommand.equals("resume") || musicCommand.equals("next") ||
                            musicCommand.equals("previous") || musicCommand.equals("skip")) {
                        return musicCommand + "ing the music.";
                    } else {
                        return "Playing music by " + musicCommand + " on your speakers.";
                    }
                }
                return "Playing your favorite playlist on the living room speakers.";

            case "security":
                if (entities.containsKey("entity1")) {
                    String securityCommand = entities.get("entity1");
                    if (securityCommand.equals("arm") || securityCommand.equals("disarm")) {
                        return securityCommand + "ing the security system.";
                    } else if (securityCommand.equals("lock")) {
                        return "Locking all doors.";
                    } else {
                        return "Showing feed from the " + securityCommand + " camera.";
                    }
                }
                return "Accessing security cameras. All systems are normal.";

            case "weather":
                if (entities.containsKey("entity1")) {
                    String location = entities.get("entity1");
                    return "The weather forecast for " + location + " shows partly cloudy skies with a high of 24°C.";
                }
                return "Today's forecast shows partly cloudy skies with a high of 24°C.";

            case "time":
                return "The current time is " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

            case "reminders":
                if (entities.containsKey("entity1") && entities.containsKey("entity2")) {
                    String task = entities.get("entity1");
                    String time = entities.get("entity2");
                    return "I've set a reminder for you to " + task + " at " + time + ".";
                }
                return "Would you like me to set a reminder? Please specify the task and time.";

            case "routines":
                if (entities.containsKey("entity1")) {
                    String routine = entities.get("entity1");
                    if (routine.equals("good morning")) {
                        return "Good morning! Running your morning routine.";
                    } else if (routine.equals("good night")) {
                        return "Good night! Running your night routine.";
                    } else if (routine.contains("leaving") || routine.contains("away")) {
                        return "Running your away routine. Have a great day!";
                    } else if (routine.contains("home")) {
                        return "Welcome home! Running your home routine.";
                    } else {
                        return "Running your " + routine + " routine.";
                    }
                }
                return "Would you like to activate your morning, evening, or away routine?";

            default:
                if (result.getConfidence() > 0.5) {
                    return "I think I understood, but I'm not sure how to handle that request yet.";
                } else {
                    return "I'm sorry, I didn't understand that command. Can you try again?";
                }
        }
    }

    // Class to hold recognition results
    private static class RecognitionResult {
        private final String category;
        private final Map<String, String> entities;
        private final float confidence;
        private final Pattern pattern;

        public RecognitionResult(String category, Map<String, String> entities, float confidence, Pattern pattern) {
            this.category = category;
            this.entities = entities;
            this.confidence = confidence;
            this.pattern = pattern;
        }

        public String getCategory() {
            return category;
        }

        public Map<String, String> getEntities() {
            return entities;
        }

        public float getConfidence() {
            return confidence;
        }

        public Pattern getPattern() {
            return pattern;
        }
    }

    public void addEntityToDatabase(String type, String entity) {
        if (!entityDatabase.containsKey(type)) {
            entityDatabase.put(type, new HashSet<>());
        }
        entityDatabase.get(type).add(entity.toLowerCase());
        notifyObservers("Added '" + entity + "' to " + type + " database");
    }

    public void addCommandPattern(String category, String regex) {
        if (!commandPatterns.containsKey(category)) {
            commandPatterns.put(category, new ArrayList<>());
        }
        try {
            Pattern pattern = Pattern.compile(regex);
            commandPatterns.get(category).add(pattern);
            notifyObservers("Added new pattern for " + category + " commands");
        } catch (Exception e) {
            notifyObservers("Error adding pattern: " + e.getMessage());
        }
    }

    public List<String> getSupportedCommands() {
        return new ArrayList<>(commandPatterns.keySet());
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

    public float getConfidenceThreshold() {
        return confidenceThreshold;
    }

    public void setConfidenceThreshold(float threshold) {
        if (threshold >= 0 && threshold <= 1) {
            this.confidenceThreshold = threshold;
            notifyObservers("Recognition confidence threshold set to " + confidenceThreshold);
        } else {
            notifyObservers("Confidence threshold must be between 0 and 1");
        }
    }

    public void setContextMemorySize(int size) {
        if (size >= 0) {
            this.contextMemorySize = size;
            while (conversationContext.size() > contextMemorySize) {
                conversationContext.removeFirst();
            }
            notifyObservers("Context memory size set to " + contextMemorySize + " exchanges");
        } else {
            notifyObservers("Context memory size must be non-negative");
        }
    }

    public int getContextMemorySize() {
        return contextMemorySize;
    }

    public List<String> getConversationContext() {
        return new ArrayList<>(conversationContext);
    }
}