package com.advanced_project.online_course_enrollment.controller;

import com.advanced_project.online_course_enrollment.util.StageBranding;
import javafx.scene.control.Alert;

public abstract class BaseController {

    protected void navigateTo(String view) {
        System.out.println("Navigating to: " + view);
    }

    protected void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        StageBranding.apply(alert);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    protected void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        StageBranding.apply(alert);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
