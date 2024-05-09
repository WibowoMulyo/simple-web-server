package com.wibowo.simplewebserver;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;

import java.io.File;

public class DashboardController {

    @FXML
    private TextField portField;

    @FXML
    private TextField webDirField;

    @FXML
    private TextField logDirField;

    @FXML
    private TextArea logTextArea;

    @FXML
    private Button startButton;

    @FXML
    private Button stopButton;

    @FXML
    private Button browseWebDirButton;

    @FXML
    private Button browseLogDirButton;

    private int port;
    private String webDirectory;
    private String logDirectory;
    private WebServer server;

    @FXML
    private void initialize() {

        // Initialize server
        server = new WebServer(logTextArea);

        // Load configuration and set fields
        portField.setText(String.valueOf(server.getPort()));
        webDirField.setText(server.getWebDirectory());
        logDirField.setText(server.getLogDirectory());

        // Add event handler for start button
        startButton.setOnMouseClicked(event -> onStartButtonClick());

        // Add event handler for stop button
        stopButton.setOnMouseClicked(event -> onStopButtonClick());

        // Add event handler for stop button
        browseWebDirButton.setOnMouseClicked(event -> onBrowseClick("Select Web Directory", webDirField));

        // Add event handler for stop button
        browseLogDirButton.setOnMouseClicked(event -> onBrowseClick("Select Web Directory", logDirField));

        // Disable stop button initially
        stopButton.setDisable(true);
    }

    @FXML
    private void onStartButtonClick() {
        port = Integer.parseInt(portField.getText());
        webDirectory = webDirField.getText();
        logDirectory = logDirField.getText();
        server.start(port, webDirectory, logDirectory);
        startButton.setDisable(true);
        stopButton.setDisable(false);
        server.saveConfig(port, webDirectory, logDirectory);
    }

    @FXML
    private void onStopButtonClick()  {
        server.stop(logDirectory);
        startButton.setDisable(false);
        stopButton.setDisable(true);
    }

    @FXML
    private void onBrowseClick(String title, TextField directoryField) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(title);
        File selectedDirectory = directoryChooser.showDialog(null);
        if (selectedDirectory != null) {
            directoryField.setText(selectedDirectory.getAbsolutePath()); // Mengatur teks pada directoryField dengan path dari direktori yang dipilih oleh pengguna.
        }
    }

}
