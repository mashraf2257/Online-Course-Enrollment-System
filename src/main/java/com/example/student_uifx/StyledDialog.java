package com.example.student_uifx;

import com.advanced_project.online_course_enrollment.util.StageBranding;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Custom frameless dialog utility that matches the project's visual style.
 * Replaces default JavaFX Alert dialogs with a modern overlay + panel design.
 */
public class StyledDialog {

    /**
     * Shows a confirmation dialog with Cancel/Confirm buttons.
     */
    public static void showConfirm(Stage ownerStage, String title, String message,
                                    String confirmText, Runnable onConfirm) {
        Stage dialog = createDialogStage(ownerStage);

        VBox panel = new VBox(16);
        panel.getStyleClass().add("dialog-panel");
        panel.setMaxHeight(400);

        Label titleLabel = new Label("\u26A0  " + title);
        titleLabel.getStyleClass().add("dialog-title");
        titleLabel.setWrapText(true);

        Pane sep = new Pane();
        sep.setMinHeight(1); sep.setMaxHeight(1);
        sep.setStyle("-fx-background-color: #E5E7EB;");

        Label msgLabel = new Label(message);
        msgLabel.getStyleClass().add("dialog-message");
        msgLabel.setWrapText(true);

        HBox buttons = new HBox(12);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("dialog-btn-cancel");
        cancelBtn.setOnAction(e -> dialog.close());

        Button confirmBtn = new Button(confirmText);
        confirmBtn.getStyleClass().add("dialog-btn-confirm");
        confirmBtn.setOnAction(e -> {
            dialog.close();
            onConfirm.run();
        });

        buttons.getChildren().addAll(cancelBtn, confirmBtn);
        panel.getChildren().addAll(titleLabel, sep, msgLabel, buttons);

        showDialog(dialog, panel);
    }

    /**
     * Shows an info dialog with custom VBox content and an OK button.
     * No scrollbar — content is displayed directly.
     */
    public static void showInfo(Stage ownerStage, String title, VBox content) {
        Stage dialog = createDialogStage(ownerStage);

        VBox panel = new VBox(16);
        panel.getStyleClass().add("dialog-panel");
        panel.setMaxHeight(500);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("dialog-title");
        titleLabel.setWrapText(true);

        Pane sep = new Pane();
        sep.setMinHeight(1); sep.setMaxHeight(1);
        sep.setStyle("-fx-background-color: #E5E7EB;");

        HBox buttons = new HBox(12);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        Button okBtn = new Button("OK");
        okBtn.getStyleClass().add("dialog-btn-ok");
        okBtn.setOnAction(e -> dialog.close());
        buttons.getChildren().add(okBtn);

        // No ScrollPane — just place content directly
        panel.getChildren().addAll(titleLabel, sep, content, buttons);

        showDialog(dialog, panel);
    }

    /**
     * Shows a settings dialog with custom VBox content and a Close button.
     */
    public static void showSettings(Stage ownerStage, String title, VBox content) {
        Stage dialog = createDialogStage(ownerStage);

        VBox panel = new VBox(18);
        panel.getStyleClass().add("dialog-panel");
        panel.setMaxHeight(500);

        Label titleLabel = new Label("\u2699  " + title);
        titleLabel.getStyleClass().add("dialog-title");
        titleLabel.setWrapText(true);

        Pane sep = new Pane();
        sep.setMinHeight(1); sep.setMaxHeight(1);
        sep.setStyle("-fx-background-color: #E5E7EB;");

        HBox buttons = new HBox(12);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        Button closeBtn = new Button("Close");
        closeBtn.getStyleClass().add("dialog-btn-ok");
        closeBtn.setOnAction(e -> dialog.close());
        buttons.getChildren().add(closeBtn);

        panel.getChildren().addAll(titleLabel, sep, content, buttons);

        showDialog(dialog, panel);
    }

    private static Stage createDialogStage(Stage ownerStage) {
        Stage dialog = new Stage();
        StageBranding.apply(dialog);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);
        if (ownerStage != null) {
            dialog.initOwner(ownerStage);
        }
        return dialog;
    }

    private static void showDialog(Stage dialog, VBox panel) {
        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("dialog-overlay");
        overlay.setAlignment(Pos.CENTER);
        overlay.getChildren().add(panel);

        overlay.setOnMouseClicked(e -> {
            if (e.getTarget() == overlay) {
                dialog.close();
            }
        });

        Scene scene = new Scene(overlay);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        scene.getStylesheets().add(
                StyledDialog.class.getResource("theme.css").toExternalForm());

        Stage owner = (Stage) dialog.getOwner();
        if (owner != null) {
            dialog.setWidth(owner.getWidth());
            dialog.setHeight(owner.getHeight());
            dialog.setX(owner.getX());
            dialog.setY(owner.getY());
        } else {
            dialog.setWidth(800);
            dialog.setHeight(600);
        }

        dialog.setScene(scene);
        dialog.showAndWait();
    }
}
