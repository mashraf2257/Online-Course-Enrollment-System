package com.advanced_project.online_course_enrollment.controller;

import com.advanced_project.online_course_enrollment.util.StageBranding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;

public class AdminTemplateController {

    private static final String ADMIN_FXML_BASE =
            "/com/advanced_project/online_course_enrollment/fxml/admin/";

    @FXML private VBox contentArea;
    @FXML private Label pageTitleLabel;
    @FXML private ImageView pageTitleIcon;

    @FXML
    public void initialize() {
        loadPanel("admin_dashboard.fxml", "Dashboard", "menu.png");
    }

    @FXML
    private void showDashboard(ActionEvent event) {
        loadPanel("admin_dashboard.fxml", "Dashboard", "menu.png");
        markActive(event);
    }

    @FXML
    private void showStudents(ActionEvent event) {
        loadPanel("admin_students.fxml", "Student Management", "graduation.png");
        markActive(event);
    }

    @FXML
    private void showCourses(ActionEvent event) {
        loadPanel("admin_courses.fxml", "Courses Management", "book.png");
        markActive(event);
    }

    @FXML
    private void showEnrollments(ActionEvent event) {
        loadPanel("admin_enrollments.fxml", "Manage Enrollments", "shopping-cart.png");
        markActive(event);
    }

    @FXML
    private void showRecommendations(ActionEvent event) {
        loadPanel("admin_recommendation.fxml", "Recommendations", "ai-technology.png");
        markActive(event);
    }

    @FXML
    private void showAuditLog(ActionEvent event) {
        loadPanel("admin_audit_log.fxml", "Audit Log", "checklist.png");
        markActive(event);
    }

    @FXML
    private void showBulkImport(ActionEvent event) {
        loadPanel("admin_bulk_import.fxml", "Bulk Import", "stack-of-books.png");
        markActive(event);
    }

    @FXML
    private void showAnalytics(ActionEvent event) {
        loadPanel("admin_analytics.fxml", "Analytics", "learning.png");
        markActive(event);
    }

    @FXML
    private void handleSignOut(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/advanced_project/online_course_enrollment/fxml/login.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            
            javafx.scene.Scene scene = stage.getScene();
            scene.setRoot(root);
            
            // Re-apply the main theme
            scene.getStylesheets().clear();
            String theme = getClass().getResource("/com/advanced_project/online_course_enrollment/theme.css").toExternalForm();
            scene.getStylesheets().add(theme);
            
            StageBranding.apply(stage);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPanel(String fxmlFile, String title, String iconFile) {
        updateTopBar(title, iconFile);
        URL resource = getClass().getResource(ADMIN_FXML_BASE + fxmlFile);
        if (resource == null) {
            System.err.println("[AdminDashboard] Missing FXML file: " + ADMIN_FXML_BASE + fxmlFile);
            return;
        }

        try {
            Node panel = new FXMLLoader(resource).load();
            contentArea.getChildren().setAll(panel);
        } catch (IOException | RuntimeException e) {
            System.err.println("[AdminDashboard] Failed to load: " + fxmlFile);
            e.printStackTrace();
        }
    }

    private void updateTopBar(String title, String iconFile) {
        pageTitleLabel.setText(title);
        URL iconUrl = getClass().getResource("/com/advanced_project/online_course_enrollment/Icons/" + iconFile);
        if (iconUrl == null) {
            iconUrl = getClass().getResource("/com/example/student_uifx/Icons/" + iconFile);
        }
        if (iconUrl != null) {
            pageTitleIcon.setImage(new Image(iconUrl.toExternalForm()));
        }
    }

    private void markActive(ActionEvent event) {
        if (!(event.getSource() instanceof Button selectedButton)) {
            return;
        }

        selectedButton.getParent().getChildrenUnmodifiable().stream()
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .forEach(button -> button.getStyleClass().remove("nav-item-active"));

        if (!selectedButton.getStyleClass().contains("nav-item-active")) {
            selectedButton.getStyleClass().add("nav-item-active");
        }
    }
}
