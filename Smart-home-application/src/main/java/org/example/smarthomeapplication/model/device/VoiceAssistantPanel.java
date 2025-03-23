package org.example.smarthomeapplication.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;

import com.smarthome.util.UIHelper;
import org.example.smarthomeapplication.model.device.SmartDevice;
import org.example.smarthomeapplication.model.device.SmartVoiceAssistant;
import org.example.smarthomeapplication.viewmodel.SmartHomeController;
import org.example.smarthomeapplication.user.Observer;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class VoiceAssistantPanel implements Observer {
    private final SmartHomeController controller;
    private SmartVoiceAssistant assistant;
    private String deviceName;

    // UI Elements
    private TextArea conversationArea;
    private TextField commandInput;
    private Button sendButton;
    private Slider volumeSlider;
    private Label statusLabel;
    private Circle statusIndicator;
    private Button historyButton;
    private ToggleButton listeningToggle;
    private ToggleButton muteToggle;
    private VBox animatedWaveform;

    private final ScheduledExecutorService pulseExecutor = Executors.newSingleThreadScheduledExecutor();
    private Timeline waveformAnimation;

    public VoiceAssistantPanel(SmartHomeController controller) {
        this.controller = controller;
    }

    public void showVoiceAssistantPanel(String deviceName) {
        this.deviceName = deviceName;
        SmartDevice device = controller.getDevice(deviceName);

        if (!(device instanceof SmartVoiceAssistant)) {
            UIHelper.showErrorAlert("Device Error", "Selected device is not a voice assistant");
            return;
        }

        this.assistant = (SmartVoiceAssistant) device;
        this.assistant.addObserver(this);

        Stage stage = new Stage();
        stage.setTitle(deviceName + " - Voice Assistant");

        VBox root = createVoiceAssistantUI();
        Scene scene = new Scene(root, 500, 700);

        stage.setScene(scene);
        stage.setOnCloseRequest(e -> {
            this.assistant.removeObserver(this);
            if (waveformAnimation != null) {
                waveformAnimation.stop();
            }
            pulseExecutor.shutdown();
        });

        stage.show();
        updateStatus();
    }

    private VBox createVoiceAssistantUI() {
        // Status
        statusIndicator = new Circle(15);
        updateStatusIndicator();

        statusLabel = new Label("Status: " + assistant.getStatus());
        statusLabel.setStyle("-fx-font-weight: bold;");

        HBox statusBox = new HBox(10, statusIndicator, statusLabel);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        statusBox.setPadding(new Insets(10));

        // Animated Waveform
        animatedWaveform = createWaveformVisualization();
        StackPane waveformContainer = new StackPane(animatedWaveform);
        waveformContainer.setMinHeight(100);
        waveformContainer.setMaxHeight(100);
        waveformContainer.setStyle("-fx-background-color: #2c3e50; -fx-background-radius: 10;");

        // Control buttons
        listeningToggle = new ToggleButton("Active Listening");
        listeningToggle.setSelected(assistant.getListeningMode().equals("active"));
        listeningToggle.setOnAction(e -> {
            if (listeningToggle.isSelected()) {
                assistant.changeState("listening");
                if (muteToggle.isSelected()) {
                    muteToggle.setSelected(false);
                }
            } else {
                assistant.changeState("passive");
            }
        });

        muteToggle = new ToggleButton("Mute");
        muteToggle.setSelected(assistant.getListeningMode().equals("muted"));
        muteToggle.setOnAction(e -> {
            if (muteToggle.isSelected()) {
                assistant.changeState("mute");
                if (listeningToggle.isSelected()) {
                    listeningToggle.setSelected(false);
                }
            } else {
                assistant.changeState("passive");
            }
        });

        // Volume slider
        Label volumeLabel = new Label("Volume:");
        volumeSlider = new Slider(0, 100, assistant.getVolume());
        volumeSlider.setShowTickLabels(true);
        volumeSlider.setShowTickMarks(true);
        volumeSlider.setMajorTickUnit(20);
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            assistant.changeState("volume:" + newVal.intValue());
        });

        HBox volumeBox = new HBox(10, volumeLabel, volumeSlider);
        volumeBox.setAlignment(Pos.CENTER_LEFT);

        // History button
        historyButton = new Button("Command History");
        historyButton.setOnAction(e -> showCommandHistory());

        // Conversation
        conversationArea = new TextArea();
        conversationArea.setEditable(false);
        conversationArea.setWrapText(true);
        conversationArea.setPrefHeight(300);
        conversationArea.setStyle("-fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 14px;");

        // Command input
        commandInput = new TextField();
        commandInput.setPromptText("Say something to your assistant...");
        commandInput.setOnAction(e -> sendCommand());

        sendButton = new Button("Send");
        sendButton.setDefaultButton(true);
        sendButton.setOnAction(e -> sendCommand());

        HBox inputBox = new HBox(10, commandInput, sendButton);
        inputBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(commandInput, Priority.ALWAYS);

        // Control box
        HBox controlBox = new HBox(10, listeningToggle, muteToggle, historyButton);
        controlBox.setAlignment(Pos.CENTER);

        // Put it all together
        VBox root = new VBox(15);
        root.setPadding(new Insets(15));
        root.getChildren().addAll(
                statusBox,
                waveformContainer,
                volumeBox,
                controlBox,
                new Separator(),
                conversationArea,
                inputBox
        );

        return root;
    }

    private VBox createWaveformVisualization() {
        VBox waveform = new VBox(5);
        waveform.setAlignment(Pos.CENTER);
        waveform.setPadding(new Insets(10));

        // Create horizontal bars for waveform visualization
        HBox barContainer = new HBox(3);
        barContainer.setAlignment(Pos.CENTER);

        for (int i = 0; i < 10; i++) {
            Region bar = new Region();
            bar.setPrefWidth(8);
            bar.setPrefHeight(5);
            bar.setMinHeight(5);
            bar.setStyle("-fx-background-color: #3498db;");
            barContainer.getChildren().add(bar);
        }

        waveform.getChildren().add(barContainer);

        // Add animation circle that pulses
        Circle pulseCircle = new Circle(25, Color.web("#3498db", 0.7));
        pulseCircle.setStroke(Color.web("#3498db"));

        StackPane pulseContainer = new StackPane(pulseCircle);
        waveform.getChildren().add(pulseContainer);

        // Start pulse animation
        startPulseAnimation(pulseCircle);

        return waveform;
    }

    private void startPulseAnimation(Circle circle) {

        waveformAnimation = new Timeline();
        final double[] baseHeights = {10, 15, 20, 15, 25, 30, 20, 25, 15, 10};

        // Set up animation for each bar in the waveform
        HBox barContainer = (HBox) ((VBox) animatedWaveform).getChildren().get(0);

        for (int i = 0; i < barContainer.getChildren().size(); i++) {
            Region bar = (Region) barContainer.getChildren().get(i);
            final int index = i;

            // Create a scheduling that changes the height of each bar
            pulseExecutor.scheduleAtFixedRate(() -> {
                if (assistant.getStatus().equals("listening") || assistant.getStatus().equals("processing")) {
                    double randomHeight = baseHeights[index] + Math.random() * 30;
                    Platform.runLater(() -> bar.setPrefHeight(randomHeight));
                } else {
                    Platform.runLater(() -> bar.setPrefHeight(baseHeights[index]));
                }
            }, 0, 200, TimeUnit.MILLISECONDS);
        }

        // Setup pulse animation for the circle
        Timeline pulseTimeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(circle.radiusProperty(), 25),
                        new KeyValue(circle.opacityProperty(), 0.7)),
                new KeyFrame(Duration.seconds(1.5),
                        new KeyValue(circle.radiusProperty(), 30),
                        new KeyValue(circle.opacityProperty(), 0.3))
        );
        pulseTimeline.setCycleCount(Timeline.INDEFINITE);
        pulseTimeline.setAutoReverse(true);
        pulseTimeline.play();
    }

    private void sendCommand() {
        String command = commandInput.getText().trim();
        if (command.isEmpty()) return;

        appendToConversation("You: " + command);
        assistant.changeState("command:" + command);
        commandInput.clear();
    }

    private void showCommandHistory() {
        Stage historyStage = new Stage();
        historyStage.setTitle("Command History - " + deviceName);

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        ListView<String> historyList = new ListView<>();
        Map<String, String> history = assistant.getCommandHistory();

        for (Map.Entry<String, String> entry : history.entrySet()) {
            historyList.getItems().add(entry.getKey() + " - " + entry.getValue());
        }

        Button clearButton = new Button("Clear History");
        clearButton.setOnAction(e -> {
            if (UIHelper.showConfirmationAlert("Clear History", "Are you sure you want to clear command history?")) {
                assistant.clearCommandHistory();
                historyList.getItems().clear();
            }
        });

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> historyStage.close());

        HBox buttonBox = new HBox(10, clearButton, closeButton);
        buttonBox.setAlignment(Pos.CENTER);

        root.getChildren().addAll(new Label("Command History:"), historyList, buttonBox);
        VBox.setVgrow(historyList, Priority.ALWAYS);

        Scene scene = new Scene(root, 400, 500);
        historyStage.setScene(scene);
        historyStage.show();
    }

    @Override
    public void update(String message) {
        Platform.runLater(() -> {
            if (message.startsWith("Response:")) {
                appendToConversation("Assistant: " + message.substring(10));
            } else {
                appendToConversation("System: " + message);
            }
            updateStatus();
        });
    }

    private void appendToConversation(String message) {
        conversationArea.appendText(message + "\n\n");
        conversationArea.positionCaret(conversationArea.getText().length());
    }

    private void updateStatus() {
        statusLabel.setText("Status: " + assistant.getStatus());
        updateStatusIndicator();

        listeningToggle.setSelected(assistant.getListeningMode().equals("active"));
        muteToggle.setSelected(assistant.getListeningMode().equals("muted"));
    }

    private void updateStatusIndicator() {
        switch (assistant.getStatus()) {
            case "listening":
                statusIndicator.setFill(Color.GREEN);
                break;
            case "processing":
                statusIndicator.setFill(Color.ORANGE);
                break;
            case "muted":
                statusIndicator.setFill(Color.RED);
                break;
            default:
                statusIndicator.setFill(Color.LIGHTGRAY);
                break;
        }
    }
}