package com.advanced_project.online_course_enrollment;

import com.advanced_project.online_course_enrollment.util.StageBranding;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    public static final double APP_WIDTH = 1920;
    public static final double APP_HEIGHT = 1000;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("fxml/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), APP_WIDTH, APP_HEIGHT);
        scene.getStylesheets().add(
                Main.class.getResource("theme.css").toExternalForm());

        StageBranding.apply(stage);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
