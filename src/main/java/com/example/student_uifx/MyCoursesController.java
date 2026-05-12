package com.example.student_uifx;

import com.advanced_project.online_course_enrollment.data.DataStore;
import com.advanced_project.online_course_enrollment.model.Course;
import com.advanced_project.online_course_enrollment.model.Enrollment;
import com.advanced_project.online_course_enrollment.model.Student;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MyCoursesController extends NavigationController {

    @FXML private Label totalCreditsLabel;
    @FXML private Label cumulativeGpaLabel;
    @FXML private VBox currentEnrollmentsContainer;
    @FXML private VBox courseHistoryContainer;

    // ── Standard GPA scale ──────────────────────────────────────
    private static double gradeToPoints(String grade) {
        if (grade == null) return 0.0;
        switch (grade.trim().toUpperCase()) {
            case "A+": return 4.0;
            case "A":  return 4.0;
            case "A-": return 3.7;
            case "B+": return 3.3;
            case "B":  return 3.0;
            case "B-": return 2.7;
            case "C+": return 2.3;
            case "C":  return 2.0;
            case "C-": return 1.7;
            case "D+": return 1.3;
            case "D":  return 1.0;
            case "D-": return 0.7;
            case "F":  return 0.0;
            default:   return 0.0;
        }
    }

    /** A grade better than "F" means the course is completed. */
    private static boolean isCompleted(String grade) {
        return grade != null && gradeToPoints(grade.trim().toUpperCase()) > 0.0;
    }

    @FXML
    public void initialize() {
        Student loggedIn = StudentSession.getLoggedInStudent();
        if (loggedIn == null) {
            return;
        }

        Map<String, String> history = loggedIn.getCourseHistory();   // courseId → grade
        Map<String, Course> courseMap = DataStore.getCourseMap();

        // ── Compute earned credits and GPA from history ──────────
        double totalPoints  = 0.0;
        double totalCredits = 0.0;
        int    earnedCredits = 0;

        for (Map.Entry<String, String> entry : history.entrySet()) {
            Course course = courseMap.get(entry.getKey());
            if (course == null) continue;

            String grade   = entry.getValue();
            int    credits = course.getCredits();

            if (isCompleted(grade)) {
                earnedCredits += credits;
                totalPoints   += gradeToPoints(grade) * credits;
                totalCredits  += credits;
            }
        }

        double gpa = totalCredits > 0
                ? Math.round((totalPoints / totalCredits) * 100.0) / 100.0
                : 0.0;

        // Persist the computed values back to the student object
        loggedIn.setEarnedCredits(earnedCredits);
        loggedIn.setGpa(gpa);

        if (totalCreditsLabel != null) {
            totalCreditsLabel.setText(String.valueOf(earnedCredits));
        }
        if (cumulativeGpaLabel != null) {
            cumulativeGpaLabel.setText(String.valueOf(gpa));
        }

        // ── Current Enrollments ─────────────────────────────────
        if (currentEnrollmentsContainer != null) {
            currentEnrollmentsContainer.getChildren().clear();

            // Enrollments for this student from DataStore
            List<Enrollment> myEnrollments = DataStore.getAllEnrollments().stream()
                    .filter(e -> e.getStudentId().equals(loggedIn.getId()))
                    .collect(Collectors.toList());

            for (Enrollment enrollment : myEnrollments) {
                Course course = courseMap.get(enrollment.getCourseId());
                if (course == null) continue;
                currentEnrollmentsContainer.getChildren().add(
                        buildEnrollmentRow(course, enrollment.getStatus()));
            }
        }

        // ── Course History ──────────────────────────────────────
        if (courseHistoryContainer != null) {
            courseHistoryContainer.getChildren().clear();

            for (Map.Entry<String, String> entry : history.entrySet()) {
                Course course = courseMap.get(entry.getKey());
                if (course == null) continue;

                String grade = entry.getValue();
                boolean completed = isCompleted(grade);
                courseHistoryContainer.getChildren().add(
                        buildHistoryRow(course, grade, completed));
            }
        }

    }

    // ── Row builders ────────────────────────────────────────────

    private HBox buildEnrollmentRow(Course course, String status) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("mc-table-row");

        Label code = new Label(course.getCourseId());
        code.setMinWidth(130); code.setPrefWidth(130);
        code.getStyleClass().add("mc-td");
        code.setStyle("-fx-text-fill: #5B21B6; -fx-font-weight: bold;");

        Label name = new Label(course.getName());
        name.setMinWidth(350); name.setPrefWidth(350);
        name.getStyleClass().addAll("mc-td", "course-title");
        name.setStyle("-fx-text-fill: #111827; -fx-font-weight: bold; -fx-wrap-text: true;");

        Label inst = new Label(course.getInstructor());
        inst.setMinWidth(200); inst.setPrefWidth(200);
        inst.getStyleClass().add("mc-td");

        Label creds = new Label(String.valueOf(course.getCredits()));
        creds.setMinWidth(100); creds.setPrefWidth(100);
        creds.getStyleClass().add("mc-td");

        // Status badge
        HBox statusBox = new HBox();
        statusBox.setAlignment(Pos.CENTER_LEFT);
        statusBox.setMinWidth(150); statusBox.setPrefWidth(150);

        String displayStatus = status != null ? status : "Enrolled";
        Label statusLabel = new Label(displayStatus);
        String badgeClass;
        switch (displayStatus) {
            case "Enrolled":
                badgeClass = "mc-badge-enrolled";
                break;
            case "Waitlisted":
                badgeClass = "mc-badge-waitlisted";
                break;
            case "Waiting for approval":
                badgeClass = "mc-badge-waitlisted";
                break;
            default:
                badgeClass = "mc-badge-enrolled";
        }
        statusLabel.getStyleClass().addAll("mc-badge", badgeClass);
        statusBox.getChildren().add(statusLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(code, name, inst, creds, statusBox, spacer);
        return row;
    }

    private HBox buildHistoryRow(Course course, String grade, boolean completed) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("mc-table-row");

        Label codeLabel = new Label(course.getCourseId());
        codeLabel.setMinWidth(130); codeLabel.setPrefWidth(130);
        codeLabel.getStyleClass().add("mc-td");
        codeLabel.setStyle("-fx-text-fill: #5B21B6; -fx-font-weight: bold;");

        Label nameLabel = new Label(course.getName());
        nameLabel.setMinWidth(400); nameLabel.setPrefWidth(400);
        nameLabel.getStyleClass().add("mc-td");
        nameLabel.setStyle("-fx-text-fill: #111827; -fx-font-weight: bold;");

        Label credsLabel = new Label(String.valueOf(course.getCredits()));
        credsLabel.setMinWidth(100); credsLabel.setPrefWidth(100);
        credsLabel.getStyleClass().add("mc-td");

        Label gradeLabel = new Label(grade);
        gradeLabel.setMinWidth(100); gradeLabel.setPrefWidth(100);
        gradeLabel.getStyleClass().add("mc-td");
        gradeLabel.setStyle("-fx-text-fill: #7C3AED; -fx-font-weight: bold;");

        // Status badge
        HBox statusBox = new HBox();
        statusBox.setAlignment(Pos.CENTER_LEFT);
        statusBox.setMinWidth(150); statusBox.setPrefWidth(150);
        Label statusLabel = new Label(completed ? "COMPLETED" : "NOT COMPLETED");
        statusLabel.getStyleClass().addAll("mc-badge",
                completed ? "mc-badge-completed" : "mc-badge-failed");
        statusBox.getChildren().add(statusLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(codeLabel, nameLabel, credsLabel, gradeLabel, statusBox, spacer);
        return row;
    }

    @FXML
    public void goToMyCourses(ActionEvent event) {
        navigateTo(event, "MyCourses.fxml");
    }
}
