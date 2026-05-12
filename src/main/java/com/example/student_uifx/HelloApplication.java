package com.example.student_uifx;

import com.advanced_project.online_course_enrollment.util.StageBranding;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    private static final double APP_WIDTH = 1980;
    private static final double APP_HEIGHT = 1020;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Parent root = fxmlLoader.load();
        root.getStylesheets().clear(); // Remove FXML-hardcoded stylesheet so Scene level works
        Scene scene = new Scene(root, APP_WIDTH, APP_HEIGHT);
        scene.getStylesheets().add(HelloApplication.class.getResource("theme.css").toExternalForm());
        StageBranding.apply(stage);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
