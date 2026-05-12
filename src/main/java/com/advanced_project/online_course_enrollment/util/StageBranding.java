package com.advanced_project.online_course_enrollment.util;

import javafx.scene.image.Image;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;

public final class StageBranding {
    private static final String APP_TITLE = "EduSync";
    private static final String LOGO_PATH = "/com/example/student_uifx/Icons/logo.png";
    private static Image logo;

    private StageBranding() {
    }

    public static void apply(Stage stage) {
        if (stage == null) {
            return;
        }

        stage.setTitle(APP_TITLE);
        Image icon = getLogo();
        if (icon != null) {
            stage.getIcons().setAll(icon);
        }
    }

    public static void apply(Dialog<?> dialog) {
        if (dialog == null) {
            return;
        }

        dialog.setTitle(APP_TITLE);
        dialog.setOnShown(event -> {
            if (dialog.getDialogPane().getScene().getWindow() instanceof Stage stage) {
                apply(stage);
            }
        });
    }

    private static Image getLogo() {
        if (logo == null) {
            var stream = StageBranding.class.getResourceAsStream(LOGO_PATH);
            if (stream == null) {
                return null;
            }
            logo = new Image(stream);
        }
        return logo;
    }
}
