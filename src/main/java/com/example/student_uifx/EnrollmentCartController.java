package com.example.student_uifx;

import com.advanced_project.online_course_enrollment.data.DataStore;
import com.advanced_project.online_course_enrollment.model.Course;
import com.advanced_project.online_course_enrollment.model.Enrollment;
import com.advanced_project.online_course_enrollment.model.Student;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class EnrollmentCartController extends NavigationController {

    @FXML private VBox cartItemsContainer;
    @FXML private Label cartCountLabel;
    @FXML private Label cartCreditsLabel;
    @FXML private Button submitEnrollmentBtn;
    @FXML private Label profileNameLabel;
    @FXML private Label profileSubtitleLabel;

    @FXML
    public void initialize() {
        populateProfile();
        if (submitEnrollmentBtn != null) {
            submitEnrollmentBtn.setOnAction(e -> submitBatchEnrollment());
        }

        renderCart();
    }

    private void populateProfile() {
        Student student = StudentSession.getLoggedInStudent();
        if (student == null) {
            return;
        }
        if (profileNameLabel != null) {
            profileNameLabel.setText(student.getName());
        }
        if (profileSubtitleLabel != null) {
            profileSubtitleLabel.setText(student.getMajor() + " Major");
        }
    }

    private void renderCart() {
        List<Enrollment> enrollments = getRegisteredEnrollments();
        int cartCredits = calculateCartCredits(enrollments);

        if (cartItemsContainer != null) {
            cartItemsContainer.getChildren().clear();

            if (enrollments.isEmpty()) {
                Label emptyLabel = new Label("Your enrollment cart is empty.");
                emptyLabel.setStyle("-fx-padding: 30; -fx-text-fill: #6B7280; -fx-font-size: 16;");
                cartItemsContainer.getChildren().add(emptyLabel);
            } else {
                for (int i = 0; i < enrollments.size(); i++) {
                    Enrollment enrollment = enrollments.get(i);
                    Course course = DataStore.getCourse(enrollment.getCourseId());
                    if (course == null) {
                        continue;
                    }
                    boolean isLast = i == enrollments.size() - 1;
                    cartItemsContainer.getChildren().add(buildCartRow(enrollment, course, isLast));
                }
            }
        }

        if (cartCountLabel != null) {
            int count = enrollments.size();
            cartCountLabel.setText("Showing " + count + " of " + count + " selected items");
        }

        if (cartCreditsLabel != null) {
            cartCreditsLabel.setText(String.valueOf(cartCredits));
        }

        if (submitEnrollmentBtn != null) {
            submitEnrollmentBtn.setDisable(enrollments.isEmpty());
        }
    }

    private HBox buildCartRow(Enrollment enrollment, Course course, boolean isLast) {
        HBox row = new HBox(16.0);
        row.setAlignment(Pos.CENTER_LEFT);

        String borderStyle = isLast ? "" : "-fx-border-color: transparent transparent #E5E7EB transparent; -fx-border-width: 0 0 1 0;";
        row.setStyle("-fx-padding: 16 24; " + borderStyle);

        Pane iconBox = new Pane();
        iconBox.setPrefSize(75.0, 57.0);
        iconBox.setStyle("-fx-background-color: #F3F6FB; -fx-background-radius: 16px;");

        ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("Icons/book.png")));
        icon.setFitHeight(42.0); icon.setFitWidth(42.0);
        icon.setLayoutX(17.0); icon.setLayoutY(8.0);
        icon.setPreserveRatio(true);
        iconBox.getChildren().add(icon);

        VBox titleBox = new VBox(4.0);
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        Label title = new Label(course.getName());
        title.getStyleClass().add("course-title");
        Label code = new Label(course.getCourseId());
        code.getStyleClass().add("course-id");
        titleBox.getChildren().addAll(title, code);

        Label credits = new Label(String.valueOf(course.getCredits()));
        credits.setPrefWidth(120.0);
        credits.getStyleClass().add("general-number");
        credits.setStyle("-fx-alignment: center; -fx-font-size: 18;");

        // Status badge with queue position for waitlisted courses
        Label status = new Label(getStatusDisplayText(enrollment));
        status.setPrefWidth(160.0);
        status.setStyle("-fx-alignment: center;");
        status.getStyleClass().add(getStatusStyleClass(enrollment.getStatus()));

        Button removeBtn = new Button("x");
        removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #9CA3AF; -fx-font-size: 18; -fx-cursor: hand;");
        removeBtn.setOnAction(e -> {
            DataStore.removeFromCart(enrollment.getStudentId(), enrollment.getCourseId());
            renderCart();
        });

        row.getChildren().addAll(iconBox, titleBox, credits, status, removeBtn);
        return row;
    }

    /**
     * Returns the display text for enrollment status badges.
     */
    private String getStatusDisplayText(Enrollment enrollment) {
        String s = enrollment.getStatus();
        if ("Waitlisted".equals(s) && enrollment.getQueuePosition() > 0) {
            return "Waitlisted #" + enrollment.getQueuePosition();
        } else if ("Waiting for approval".equals(s)) {
            return "Awaiting Approval";
        } else if ("Enrolled".equals(s)) {
            return "\u2713 Enrolled";
        } else if ("Rejected".equals(s)) {
            return "\u2715 Rejected";
        }
        return s;
    }

    /**
     * Returns the CSS style class for a given enrollment status.
     */
    private String getStatusStyleClass(String status) {
        switch (status) {
            case "Waitlisted":
                return "status-waitlist";
            case "Waiting for approval":
                return "status-pending";
            case "Enrolled":
                return "status-enrolled";
            case "Rejected":
                return "status-rejected";
            default:
                return "status-enrolled";
        }
    }

    private List<Enrollment> getRegisteredEnrollments() {
        Student student = StudentSession.getLoggedInStudent();
        if (student == null) {
            return java.util.Collections.emptyList();
        }

        return DataStore.getCartForStudent(student.getId());
    }

    private int calculateCartCredits(List<Enrollment> enrollments) {
        return enrollments.stream()
                .map(Enrollment::getCourseId)
                .map(DataStore::getCourse)
                .filter(course -> course != null)
                .mapToInt(Course::getCredits)
                .sum();
    }

    private void submitBatchEnrollment() {
        Student student = StudentSession.getLoggedInStudent();
        if (student == null) return;

        List<Enrollment> cart = DataStore.getCartForStudent(student.getId());
        if (cart.isEmpty()) return;

        List<Enrollment> registeredResults = new ArrayList<>();
        List<String> failedResults = new ArrayList<>();
        int successCount = 0;

        for (Enrollment e : cart) {
            try {
                Enrollment registered = DataStore.registerCourse(student, e.getCourseId());
                registeredResults.add(registered);
                successCount++;
            } catch (IllegalArgumentException ex) {
                failedResults.add(e.getCourseId() + ": " + ex.getMessage());
            }
        }

        DataStore.clearCart(student.getId());
        renderCart();

        showSubmitResultsDialog(registeredResults, failedResults, successCount, cart.size());
    }

    /**
     * Shows a styled custom dialog with colored status badges for each submitted course.
     */
    private void showSubmitResultsDialog(List<Enrollment> results, List<String> failures,
                                          int successCount, int totalCount) {
        VBox content = new VBox(10);

        // Course results with badges
        for (Enrollment enrollment : results) {
            Course course = DataStore.getCourse(enrollment.getCourseId());
            String courseName = course != null ? course.getName() : enrollment.getCourseId();

            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-padding: 4 0;");

            Label nameLabel = new Label(enrollment.getCourseId() + "  " + courseName);
            nameLabel.getStyleClass().add("dialog-course-name");
            nameLabel.setWrapText(true);
            HBox.setHgrow(nameLabel, Priority.ALWAYS);

            Label badge = new Label(getStatusDisplayText(enrollment));
            badge.getStyleClass().add(getStatusStyleClass(enrollment.getStatus()));
            badge.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);

            row.getChildren().addAll(nameLabel, badge);
            content.getChildren().add(row);
        }

        // Failed courses
        for (String failure : failures) {
            Label failLabel = new Label("\u2715  " + failure);
            failLabel.getStyleClass().add("dialog-fail-text");
            failLabel.setWrapText(true);
            content.getChildren().add(failLabel);
        }

        Stage stage = (Stage) cartItemsContainer.getScene().getWindow();
        StyledDialog.showInfo(
                stage,
                "\u2705  Submitted " + successCount + " of " + totalCount + " courses",
                content
        );
    }
}
