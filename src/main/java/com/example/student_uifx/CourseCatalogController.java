package com.example.student_uifx;

import com.advanced_project.online_course_enrollment.data.DataStore;
import com.advanced_project.online_course_enrollment.model.Course;
import com.advanced_project.online_course_enrollment.model.Student;
import com.advanced_project.online_course_enrollment.service.WaitlistService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Map;
import java.util.stream.Collectors;

public class CourseCatalogController extends NavigationController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> departmentFilter;
    @FXML private ComboBox<String> yearFilter;
    @FXML private ComboBox<String> availabilityFilter;
    @FXML private Button applyFiltersBtn;
    @FXML private VBox coursesContainer;
    @FXML private Label profileNameLabel;
    @FXML private Label profileSubtitleLabel;

    @FXML
    public void initialize() {
        populateProfile();
        populateFilters();
        renderCourses(DataStore.getCourseMap());

        // Live search
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        }

        // Apply Button
        if (applyFiltersBtn != null) {
            applyFiltersBtn.setOnAction(e -> applyFilters());
        }
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

    private void populateFilters() {
        if (departmentFilter != null) {
            departmentFilter.setItems(FXCollections.observableArrayList(
                    "All Departments", "Computer Science", "Engineering", "Business", "Bioinformatics"
            ));
            departmentFilter.setValue("All Departments");
        }
        if (yearFilter != null) {
            yearFilter.setItems(FXCollections.observableArrayList(
                    "All Years", "First Year", "Second Year", "Third Year", "Fourth Year"
            ));
            yearFilter.setValue("All Years");
        }
        if (availabilityFilter != null) {
            availabilityFilter.setItems(FXCollections.observableArrayList(
                    "All Courses", "Open Seats Only"
            ));
            availabilityFilter.setValue("All Courses");
        }
    }

    private void applyFilters() {
        String query = searchField != null && searchField.getText() != null ? searchField.getText().toLowerCase() : "";
        String dept = departmentFilter != null ? departmentFilter.getValue() : "All Departments";
        String year = yearFilter != null ? yearFilter.getValue() : "All Years";
        String avail = availabilityFilter != null ? availabilityFilter.getValue() : "All Courses";

        Map<String, Course> filtered =
                DataStore.getCourseMap().entrySet().stream()
                        .filter(entry -> query.isEmpty() || toSearchString(entry.getValue()).contains(query))
                        .filter(entry -> dept.equals("All Departments") || entry.getValue().getMajor().equals(dept))
                        .filter(entry -> year.equals("All Years"))
                        .filter(entry -> avail.equals("All Courses") || !entry.getValue().isfull())
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue
                        ));

        renderCourses(filtered);
    }

    private void renderCourses(Map<String, Course> courses) {
        if (coursesContainer == null) return;
        coursesContainer.getChildren().clear();

        // Display in pairs (2 per row)
        HBox currentRow = null;
        int i = 0;
        for (Course course : courses.values()) {
            if (i % 2 == 0) {
                currentRow = new HBox(14.0);
                coursesContainer.getChildren().add(currentRow);
            }
            VBox card = buildCourseCard(course);
            HBox.setHgrow(card, Priority.ALWAYS);
            currentRow.getChildren().add(card);
            i++;
        }

        // Add a spacer region to the last row if it has only one item to keep it aligned
        if (courses.size() % 2 != 0 && currentRow != null) {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            currentRow.getChildren().add(spacer);
        }
    }

    private VBox buildCourseCard(Course course) {
        VBox card = new VBox();
        card.getStyleClass().add("catalog-card");

        // Top Accent
        Pane accent = new Pane();
        accent.setMinHeight(4.0); accent.setMaxHeight(4.0);
        String accentClass = "card-accent-open";
        if (course.isfull()) accentClass = "card-accent-full";
        accent.getStyleClass().addAll("card-accent", accentClass);

        VBox body = new VBox(12.0);
        body.getStyleClass().addAll("catalog-card-body", "border-top");

        // Header Row (Badges)
        HBox headRow = new HBox();
        headRow.setAlignment(Pos.CENTER_LEFT);
        headRow.getStyleClass().add("card-head-row");

        String status = course.isfull() ? "FULL" : "OPEN";
        Label codeBadge = new Label(course.getCourseId());
        codeBadge.getStyleClass().addAll("code-badge", "code-badge-" + status.toLowerCase());

        Label statusBadge = new Label(status);
        statusBadge.getStyleClass().addAll("availability-badge", "availability-badge-" + status.toLowerCase());

        headRow.getChildren().addAll(codeBadge, statusBadge);

        // Titles
        Label title = new Label(course.getName());
        title.getStyleClass().add("catalog-course-title");
        title.setWrapText(true);

        Label desc = new Label(course.getMajor());
        desc.getStyleClass().add("course-desc");
        desc.setWrapText(true);

        // Meta Rows
        HBox meta1 = createMetaRow(course.getInstructor(), "teacher.png", getSeatsDisplay(course), "student.png", course.isfull());
        HBox meta2 = createMetaRow("Schedule TBD", "settings.png",
                course.getPrerequisites().isEmpty() ? "No Prereqs" : "Prereq: " + String.join(", ", course.getPrerequisites()), "check.png", false);

        // Waitlist count row (only shown for full courses with a waitlist)
        HBox waitlistRow = null;
        if (course.isfull()) {
            int waitlistSize = WaitlistService.getWaitlistSize(course.getCourseId());
            if (waitlistSize > 0) {
                waitlistRow = new HBox(6.0);
                waitlistRow.setAlignment(Pos.CENTER_LEFT);
                waitlistRow.getStyleClass().add("course-meta-row");
                ImageView queueIcon = createIcon("queue.png");
                Label waitlistLabel = new Label(waitlistSize + " in waitlist");
                waitlistLabel.getStyleClass().add("course-meta-line");
                waitlistLabel.setStyle("-fx-text-fill: #D97706;");
                waitlistLabel.setGraphic(queueIcon);
                waitlistRow.getChildren().add(waitlistLabel);
            }
        }

        // Footer
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.getStyleClass().add("course-footer");

        Label credits = new Label("Credits: " + course.getCredits());
        credits.getStyleClass().add("credit-label");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button actionBtn = new Button();
        actionBtn.setGraphic(createIcon("check.png"));

        boolean inCart = isInCart(course);
        boolean enrolled = isEnrolled(course);
        boolean meetsPrereqs = hasCompletedPrerequisites(course);

        if (enrolled) {
            actionBtn.setText("Enrolled");
            actionBtn.getStyleClass().add("action-button-disabled");
            actionBtn.setDisable(true);
        } else if (inCart) {
            actionBtn.setText("In Cart ✓");
            actionBtn.getStyleClass().add("action-button-disabled");
            actionBtn.setDisable(true);
        } else if (!meetsPrereqs) {
            actionBtn.setText("Locked (Prereq)");
            actionBtn.getStyleClass().add("action-button-danger");
            actionBtn.setDisable(true);
        } else if (course.isfull()) {
            actionBtn.setText("Join Waitlist");
            actionBtn.getStyleClass().add("action-button-secondary");
            actionBtn.setOnAction(e -> addToCart(course));
        } else {
            actionBtn.setText("Enroll Now");
            actionBtn.getStyleClass().add("action-button-secondary");
            actionBtn.setOnAction(e -> addToCart(course));
        }

        footer.getChildren().addAll(credits, spacer, actionBtn);

        body.getChildren().addAll(headRow, title, desc, meta1, meta2);
        if (waitlistRow != null) {
            body.getChildren().add(waitlistRow);
        }
        body.getChildren().add(footer);
        card.getChildren().addAll(accent, body);

        return card;
    }

    private HBox createMetaRow(String text1, String icon1, String text2, String icon2, boolean highlightText2) {
        HBox row = new HBox(42.0);
        row.getStyleClass().add("course-meta-row");

        Label l1 = new Label(text1);
        l1.getStyleClass().add("course-meta-line");
        l1.setGraphic(createIcon(icon1));

        Label l2 = new Label(text2);
        l2.getStyleClass().add("course-meta-line");
        l2.setGraphic(createIcon(icon2));
        if (highlightText2) l2.setStyle("-fx-text-fill: #DC2626;");

        row.getChildren().addAll(l1, l2);
        return row;
    }

    private ImageView createIcon(String filename) {
        ImageView img = new ImageView(new Image(getClass().getResourceAsStream("Icons/" + filename)));
        img.setFitHeight(14.0); img.setFitWidth(14.0);
        img.setPreserveRatio(true);
        img.getStyleClass().add("course-meta-icon");
        return img;
    }

    private String getSeatsDisplay(Course course) {
        return course.getEnrolledCount() + " / " + course.getCapacity() + " Seats";
    }

    private String toSearchString(Course course) {
        return (course.getCourseId() + " " + course.getName() + " " + course.getInstructor() + " "
                + course.getMajor() + " " + String.join(" ", course.getPrerequisites())).toLowerCase();
    }

    private boolean isInCart(Course course) {
        Student student = StudentSession.getLoggedInStudent();
        if (student == null) return false;
        return DataStore.getCartForStudent(student.getId()).stream()
                .anyMatch(e -> e.getCourseId().equals(course.getCourseId()));
    }

    private boolean isEnrolled(Course course) {
        Student student = StudentSession.getLoggedInStudent();
        return student != null && DataStore.isEnrolled(student.getId(), course.getCourseId());
    }

    private boolean hasCompletedPrerequisites(Course course) {
        Student student = StudentSession.getLoggedInStudent();
        if (student == null) return false;
        
        java.util.List<String> prereqs = course.getPrerequisites();
        if (prereqs == null || prereqs.isEmpty()) return true;

        Map<String, String> history = student.getCourseHistory();
        if (history == null) return false;

        for (String prereq : prereqs) {
            String grade = history.get(prereq);
            // Must have taken it AND passed it (grade > F)
            if (grade == null || gradeToPoints(grade) == 0.0) {
                return false;
            }
        }
        return true;
    }

    private double gradeToPoints(String grade) {
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

    private void addToCart(Course course) {
        try {
            Student student = StudentSession.getLoggedInStudent();
            if (student == null) {
                throw new IllegalArgumentException("No student is currently logged in.");
            }
            DataStore.addToCart(student, course.getCourseId());
            applyFilters(); // Re-render to update the button states
        } catch (IllegalArgumentException e) {
            javafx.stage.Stage stage = (javafx.stage.Stage) coursesContainer.getScene().getWindow();
            javafx.scene.layout.VBox msgContent = new javafx.scene.layout.VBox(8);
            javafx.scene.control.Label msgLabel = new javafx.scene.control.Label(e.getMessage());
            msgLabel.setWrapText(true);
            msgLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #4B5563;");
            msgContent.getChildren().add(msgLabel);
            StyledDialog.showInfo(stage, "⚠  Registration Blocked", msgContent);
        }
    }
}
