package com.advanced_project.online_course_enrollment.util;

import com.advanced_project.online_course_enrollment.model.Admin;
import com.advanced_project.online_course_enrollment.model.Student;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * SceneManager — Centralized Navigation Utility
 *
 * Singleton that manages all screen transitions in the app.
 * Usage from any controller:
 *
 *   SceneManager.getInstance().navigateTo(Screen.STUDENT_DASHBOARD);
 *   SceneManager.getInstance().navigateTo(Screen.LOGIN);
 *
 * To pass data between screens, use the session methods:
 *   SceneManager.getInstance().setCurrentStudent(student);
 *   SceneManager.getInstance().setCurrentAdmin(admin);
 */
public class SceneManager {

    // ── Singleton ────────────────────────────────────────────────
    private static SceneManager instance;

    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    private SceneManager() {
        buildRoutes();
    }

    // ── Screen Enum ──────────────────────────────────────────────
    public enum Screen {
        LOGIN,
        STUDENT_DASHBOARD,
        COURSE_CATALOG,
        ENROLLMENT_CART,
        AUDIT_LOGS,
        ADMIN_DASHBOARD,
        COURSE_MANAGEMENT
    }

    // ── State ────────────────────────────────────────────────────
    private Stage primaryStage;
    private Scene  mainScene;

    /** Who is logged in right now */
    private Student currentStudent;
    private Admin   currentAdmin;

    /** Simple history stack for back-navigation */
    private final java.util.Deque<Screen> history = new java.util.ArrayDeque<>();
    private Screen currentScreen;

    /** FXML route map:  Screen → resource path */
    private final Map<Screen, String> routes = new HashMap<>();

    /** Window title map */
    private final Map<Screen, String> titles = new HashMap<>();

    // ── Route Registration ───────────────────────────────────────
    private void buildRoutes() {
        // FXML paths (relative to resources root)
        String base = "/com/advanced_project/online_course_enrollment/fxml/";

        routes.put(Screen.LOGIN,              base + "login.fxml");
        routes.put(Screen.STUDENT_DASHBOARD,  base + "student_dashboard.fxml");
        routes.put(Screen.COURSE_CATALOG,     base + "course_catalog.fxml");
        routes.put(Screen.ENROLLMENT_CART,    base + "enrollment_cart.fxml");
        routes.put(Screen.AUDIT_LOGS,         base + "audit_logs.fxml");
        routes.put(Screen.ADMIN_DASHBOARD,    base + "admin/admin_template.fxml");
        routes.put(Screen.COURSE_MANAGEMENT,  base + "course_management.fxml");

        titles.put(Screen.LOGIN,              "EduSync Pro — Login");
        titles.put(Screen.STUDENT_DASHBOARD,  "EduSync Pro — Dashboard");
        titles.put(Screen.COURSE_CATALOG,     "EduSync Pro — Course Catalog");
        titles.put(Screen.ENROLLMENT_CART,    "EduSync Pro — Enrollment Cart");
        titles.put(Screen.AUDIT_LOGS,         "EduSync Pro — Audit Logs");
        titles.put(Screen.ADMIN_DASHBOARD,    "EduSync Pro — Admin Dashboard");
        titles.put(Screen.COURSE_MANAGEMENT,  "EduSync Pro — Course Management");
    }

    // ── Init (call once from HelloApplication) ───────────────────

    /**
     * Must be called once in HelloApplication.start() before any navigation.
     *
     * Example in HelloApplication.java:
     *   SceneManager.getInstance().init(primaryStage);
     *   SceneManager.getInstance().navigateTo(Screen.LOGIN);
     */
    public void init(Stage stage) {
        this.primaryStage = stage;
        this.primaryStage.setResizable(true);
        this.primaryStage.setMinWidth(900);
        this.primaryStage.setMinHeight(600);
        applyWindowStyle();
    }

    private void applyWindowStyle() {
        StageBranding.apply(primaryStage);
    }

    // ── Core Navigation ──────────────────────────────────────────

    /**
     * Navigate to a screen.
     * Pushes current screen to history so you can go back.
     */
    public void navigateTo(Screen screen) {
        try {
            String fxmlPath = routes.get(screen);
            if (fxmlPath == null) {
                throw new IllegalArgumentException("No route registered for screen: " + screen);
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Apply global theme stylesheet
            String css = getClass().getResource(
                "/com/advanced_project/online_course_enrollment/theme.css"
            ).toExternalForm();

            if (mainScene == null) {
                // First load — create the Scene
                mainScene = new Scene(root, com.advanced_project.online_course_enrollment.Main.APP_WIDTH, com.advanced_project.online_course_enrollment.Main.APP_HEIGHT);
                mainScene.getStylesheets().add(css);
                primaryStage.setScene(mainScene);
            } else {
                // Reuse scene, just swap the root (avoids stylesheet reload flicker)
                mainScene.getStylesheets().clear();
                mainScene.getStylesheets().add(css);
                mainScene.setRoot(root);
            }

            StageBranding.apply(primaryStage);

            // Track history
            if (currentScreen != null && currentScreen != screen) {
                history.push(currentScreen);
            }
            currentScreen = screen;

            primaryStage.show();

        } catch (IOException e) {
            System.err.println("[SceneManager] Failed to load screen: " + screen);
            e.printStackTrace();
        }
    }

    /**
     * Go back to the previous screen.
     * Falls back to LOGIN if history is empty.
     */
    public void goBack() {
        if (!history.isEmpty()) {
            Screen previous = history.pop();
            navigateTo(previous);
        } else {
            navigateTo(Screen.LOGIN);
        }
    }

    /**
     * Log out: clears session data and goes to login.
     */
    public void logout() {
        currentStudent = null;
        currentAdmin   = null;
        history.clear();
        navigateTo(Screen.LOGIN);
    }

    // ── Session Getters / Setters ─────────────────────────────────

    /** Set the currently logged-in student (call after login validation) */
    public void setCurrentStudent(Student student) {
        this.currentStudent = student;
        this.currentAdmin   = null; // only one role at a time
    }

    /** Set the currently logged-in admin (call after login validation) */
    public void setCurrentAdmin(Admin admin) {
        this.currentAdmin   = admin;
        this.currentStudent = null;
    }

    public Student getCurrentStudent() {
        return currentStudent;
    }

    public Admin getCurrentAdmin() {
        return currentAdmin;
    }

    /** Returns true if a student is logged in */
    public boolean isStudentSession() {
        return currentStudent != null;
    }

    /** Returns true if an admin is logged in */
    public boolean isAdminSession() {
        return currentAdmin != null;
    }

    public Screen getCurrentScreen() {
        return currentScreen;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }
}
