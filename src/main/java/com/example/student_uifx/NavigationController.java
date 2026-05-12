package com.example.student_uifx;

import com.advanced_project.online_course_enrollment.util.StageBranding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Base controller that provides sidebar navigation for all views.
 * Each view controller should extend this class to inherit nav button handlers.
 */
public class NavigationController {

    /** Shared dark mode state (persists across page navigations within the session) */
    private static boolean darkModeEnabled = false;

    private static final String DARK_CSS_FILENAME = "theme-dark.css";

    /**
     * Navigates to a different view by replacing the scene root.
     * Preserves the current stage, maximized state, and theme CSS.
     */
    protected void navigateTo(ActionEvent event, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();
            StageBranding.apply(stage);

            root.getStylesheets().clear(); // Remove FXML-hardcoded stylesheet
            scene.setRoot(root);

            // Clear existing stylesheets and apply the correct theme
            scene.getStylesheets().clear();
            applyDarkMode(scene);
        } catch (IOException e) {
            System.err.println("Failed to load view: " + fxmlFile);
            e.printStackTrace();
        }
    }

    @FXML
    protected void goToDashboard(ActionEvent event) {
        navigateTo(event, "hello-view.fxml");
    }

    @FXML
    protected void goToCourseCatalog(ActionEvent event) {
        navigateTo(event, "Course Catalog.fxml");
    }

    @FXML
    protected void goToEnrollmentCart(ActionEvent event) {
        navigateTo(event, "Enrollment_Cart.fxml");
    }

    @FXML
    protected void goToMyCourses(ActionEvent event) {
        navigateTo(event, "MyCourses.fxml");
    }

    @FXML
    protected void handleSettings(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        VBox content = new VBox(16);

        // -- Dark Theme toggle --
        content.getChildren().add(buildDarkThemeRow(stage));

        // Separator
        Pane sep1 = new Pane();
        sep1.setMinHeight(1); sep1.setMaxHeight(1);
        sep1.setStyle("-fx-background-color: #E5E7EB;");
        content.getChildren().add(sep1);

        // -- Arabic Language toggle (placeholder) --
        content.getChildren().add(buildSettingRow(
                "\uD83C\uDF10  Translate to Arabic",
                "Switch the interface language to Arabic (\u0627\u0644\u0639\u0631\u0628\u064A\u0629)",
                "Coming Soon"
        ));

        StyledDialog.showSettings(stage, "System Settings", content);
    }

    /**
     * Builds the dark theme toggle row with a real ON/OFF button.
     */
    private HBox buildDarkThemeRow(Stage stage) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 14 18; -fx-background-color: #F9FAFB; "
                + "-fx-background-radius: 12; -fx-border-color: #E5E7EB; "
                + "-fx-border-radius: 12; -fx-border-width: 1;");

        VBox textBox = new VBox(4);
        Label titleLabel = new Label("\uD83C\uDF19  Dark Theme");
        titleLabel.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #111827;");
        Label descLabel = new Label("Switch the interface to a dark color scheme");
        descLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #6B7280;");
        descLabel.setWrapText(true);
        textBox.getChildren().addAll(titleLabel, descLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Toggle button
        Label toggleLabel = new Label(darkModeEnabled ? "ON" : "OFF");
        String activeStyle = "-fx-background-color: #2563EB; -fx-background-radius: 999; "
                + "-fx-padding: 6 16; -fx-font-size: 12; -fx-font-weight: bold; "
                + "-fx-text-fill: white; -fx-cursor: hand;";
        String inactiveStyle = "-fx-background-color: #E5E7EB; -fx-background-radius: 999; "
                + "-fx-padding: 6 16; -fx-font-size: 12; -fx-font-weight: bold; "
                + "-fx-text-fill: #6B7280; -fx-cursor: hand;";
        toggleLabel.setStyle(darkModeEnabled ? activeStyle : inactiveStyle);

        toggleLabel.setOnMouseClicked(e -> {
            darkModeEnabled = !darkModeEnabled;
            toggleLabel.setText(darkModeEnabled ? "ON" : "OFF");
            toggleLabel.setStyle(darkModeEnabled
                    ? "-fx-background-color: #2563EB; -fx-background-radius: 999; "
                        + "-fx-padding: 6 16; -fx-font-size: 12; -fx-font-weight: bold; "
                        + "-fx-text-fill: white; -fx-cursor: hand;"
                    : "-fx-background-color: #E5E7EB; -fx-background-radius: 999; "
                        + "-fx-padding: 6 16; -fx-font-size: 12; -fx-font-weight: bold; "
                        + "-fx-text-fill: #6B7280; -fx-cursor: hand;"
            );

            // Apply or remove dark CSS immediately
            applyDarkMode(stage.getScene());
        });

        row.getChildren().addAll(textBox, spacer, toggleLabel);
        return row;
    }

    /**
     * Builds a static setting row with a label pill (non-interactive).
     */
    private HBox buildSettingRow(String title, String description, String pillText) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 14 18; -fx-background-color: #F9FAFB; "
                + "-fx-background-radius: 12; -fx-border-color: #E5E7EB; "
                + "-fx-border-radius: 12; -fx-border-width: 1;");

        VBox textBox = new VBox(4);
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #111827;");
        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #6B7280;");
        descLabel.setWrapText(true);
        textBox.getChildren().addAll(titleLabel, descLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label toggleLabel = new Label(pillText);
        toggleLabel.setStyle("-fx-background-color: #E5E7EB; -fx-background-radius: 999; "
                + "-fx-padding: 6 14; -fx-font-size: 11; -fx-font-weight: bold; "
                + "-fx-text-fill: #6B7280;");

        row.getChildren().addAll(textBox, spacer, toggleLabel);
        return row;
    }

    private static final String LIGHT_CSS_FILENAME = "theme.css";

    /**
     * Applies or removes the dark theme.
     * Strategy: SWAP the entire stylesheet (light ↔ dark) so that all rules
     * take effect, since JavaFX first-stylesheet-wins for same-specificity rules.
     * Also sets inline style on root to override CSS variables for elements
     * that use looked-up colors.
     */
    private static void applyDarkMode(Scene scene) {
        if (scene == null) return;
        String lightCss = NavigationController.class.getResource(LIGHT_CSS_FILENAME).toExternalForm();
        String darkCss = NavigationController.class.getResource(DARK_CSS_FILENAME).toExternalForm();

        if (darkModeEnabled) {
            // Remove light, add dark
            scene.getStylesheets().remove(lightCss);
            if (!scene.getStylesheets().contains(darkCss)) {
                scene.getStylesheets().add(darkCss);
            }
            // Inline style on root for CSS variable overrides
            if (scene.getRoot() != null) {
                scene.getRoot().setStyle(
                    "-fx-background-color: #111827; "
                    + "-app-bg: #111827; -app-surface: #1F2937; -app-surface-muted: #1a2332; "
                    + "-app-border: #374151; -app-border-soft: #374151; "
                    + "-app-text: #F9FAFB; -app-text-muted: #D1D5DB; -app-text-soft: #9CA3AF; "
                    + "-app-primary: #3B82F6; -app-primary-dark: #2563EB; -app-primary-soft: #1E3A5F; "
                    + "-app-sidebar-bg: #0F172A; -app-sidebar-line: #1E293B; "
                    + "-app-sidebar-text: #E2E8F0; -app-sidebar-text-muted: #94A3B8; "
                    + "-app-sidebar-active: #60A5FA; -app-sidebar-active-bg: #1E293B; "
                    + "-app-success: #34D399; -app-success-bg: #064E3B; "
                    + "-app-danger: #F87171; -app-danger-bg: #7F1D1D; "
                    + "-app-draft: #9CA3AF; -app-draft-bg: #374151;"
                );
            }
        } else {
            // Remove dark, restore light
            scene.getStylesheets().remove(darkCss);
            if (!scene.getStylesheets().contains(lightCss)) {
                scene.getStylesheets().add(lightCss);
            }
            // Clear inline override
            if (scene.getRoot() != null) {
                scene.getRoot().setStyle("");
            }
        }
    }

    @FXML
    protected void handleSignOut(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/advanced_project/online_course_enrollment/fxml/login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();
            scene.setRoot(root);
            
            scene.getStylesheets().clear();
            String mainTheme = getClass().getResource("/com/advanced_project/online_course_enrollment/theme.css").toExternalForm();
            scene.getStylesheets().add(mainTheme);
            
            StageBranding.apply(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
