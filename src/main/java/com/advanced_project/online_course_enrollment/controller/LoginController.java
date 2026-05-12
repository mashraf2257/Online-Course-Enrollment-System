package com.advanced_project.online_course_enrollment.controller;

import com.advanced_project.online_course_enrollment.data.DataStore;
import com.advanced_project.online_course_enrollment.Main;
import com.advanced_project.online_course_enrollment.model.Admin;
import com.advanced_project.online_course_enrollment.model.AuditLog;
import com.advanced_project.online_course_enrollment.model.Student;
import com.advanced_project.online_course_enrollment.util.StageBranding;
import com.example.student_uifx.StudentSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;

public class LoginController {

    @FXML
    private TextField nameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    @FXML
    private void handleLogin(ActionEvent event) {
        String name = nameField.getText();
        String password = passwordField.getText();

        if (name.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please fill in all fields.");
            return;
        }

        DataStore store = DataStore.getInstance();

        // Check Admin first (Admins still use ID for login in DataStore)
        Admin admin = store.authenticateAdminByUsername(name, password);
        if (admin != null) {
            logLogin(admin.getAdminId(), "Admin Login");
            navigateTo(event, "/com/advanced_project/online_course_enrollment/fxml/admin/admin_template.fxml",
                    "Admin Dashboard");
            return;
        }

        // Check Student (Uses name for login)
        Student student = store.authenticateByUsername(name, password);
        if (student != null) {
            logLogin(student.getId(), "Student Login");
            StudentSession.setLoggedInStudent(student);
            // Navigating to student app (example package)
            navigateTo(event, "/com/example/student_uifx/hello-view.fxml", "Student Portal");
            return;
        }

        errorLabel.setText("Invalid name or password.");
    }

    @FXML
    private void handleGoToSignup(ActionEvent event) {
        navigateTo(event, "/com/advanced_project/online_course_enrollment/fxml/signup.fxml", "Sign Up");
    }

    private void logLogin(String userId, String action) {
        DataStore.getInstance().addAuditLog(new AuditLog(userId, action, "N/A", LocalDateTime.now()));
    }

    private void navigateTo(ActionEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Set a larger size for the main dashboards
            Scene scene = new Scene(root, Main.APP_WIDTH, Main.APP_HEIGHT);

            // Re-apply theme if necessary
            String theme = getClass().getResource("/com/advanced_project/online_course_enrollment/theme.css")
                    .toExternalForm();
            scene.getStylesheets().add(theme);

            stage.setScene(scene);
            StageBranding.apply(stage);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            errorLabel.setText("Error loading page: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
