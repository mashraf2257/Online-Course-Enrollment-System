package com.advanced_project.online_course_enrollment.controller;

import com.advanced_project.online_course_enrollment.data.DataStore;
import com.advanced_project.online_course_enrollment.model.Student;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import com.advanced_project.online_course_enrollment.Main;
import com.advanced_project.online_course_enrollment.util.StageBranding;
import java.io.IOException;

public class SignupController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField idField;
    @FXML
    private ComboBox<String> majorField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        majorField.getItems().addAll(
                "Computer Science",
                "Engineering",
                "Business",
                "Bioinformatics",
                "Arts");

    }

    @FXML
    private void handleSignup(ActionEvent event) {
        String name = nameField.getText();
        String id = idField.getText();
        String major = majorField.getValue();
        String email = emailField.getText();
        String password = passwordField.getText();

        if (name.isEmpty() || id.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please fill in all the required fields.");
            return;
        }

        DataStore store = DataStore.getInstance();
        if (!store.isStudentIdUnique(id)) {
            statusLabel.setText("Student ID already exists.");
            return;
        }
        if (!store.isUsernameUnique(name)) {
            statusLabel.setText("Name already taken. Please use a unique name.");
            return;
        }

        // Creating student with default academic year "1"
        Student newStudent = new Student(id, name, password, major, "1", email);
        store.addStudent(newStudent);

        statusLabel.setStyle("-fx-text-fill: #4CAF50;");
        statusLabel.setText("Registration successful! Returning to login...");

        // Simple delay before returning to login would be nice, but for now we redirect
        handleBackToLogin(event);
    }

    @FXML
    private void handleBackToLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader
                    .load(getClass().getResource("/com/advanced_project/online_course_enrollment/fxml/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, Main.APP_WIDTH, Main.APP_HEIGHT));
            StageBranding.apply(stage);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
