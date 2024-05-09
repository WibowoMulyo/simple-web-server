package com.wibowo.simplewebserver;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.IOException;

public class App extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("dashboard.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Image icon = new Image("server.png");

        stage.getIcons().add(icon);
        stage.setTitle("Simple Web Server");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}