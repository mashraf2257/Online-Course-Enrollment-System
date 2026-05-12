package com.advanced_project.online_course_enrollment.controller;

import com.advanced_project.online_course_enrollment.ai.*;
import com.advanced_project.online_course_enrollment.model.Course;
import com.advanced_project.online_course_enrollment.model.Student;
import com.advanced_project.online_course_enrollment.service.EnrollmentService;
import com.advanced_project.online_course_enrollment.util.StageBranding;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class RecommendationController extends BaseController {

    @FXML
    private ComboBox<Student> studentComboBox;
    @FXML
    private HBox selectedStudentCard;
    @FXML
    private Label studentInitialsLabel;
    @FXML
    private Label selectedStudentNameLabel;
    @FXML
    private Label selectedStudentMetaLabel;
    @FXML
    private Label enrollmentMessageLabel;
    @FXML
    private VBox recommendationsContainer;

    private final EnrollmentService enrollmentService = new EnrollmentService();

    @FXML
    public void initialize() {
        configureComboBoxes();
        loadComboBoxData();

        studentComboBox.valueProperty()
                .addListener((observable, oldValue, newValue) -> updateSelectedStudent(newValue));
    }

    @FXML
    private void generateRecommendations() {
        Student selected = studentComboBox.getValue();
        if (selected == null) {
            showMessage("Please select a student first", true);
            return;
        }

        try {
            recommendationsContainer.getChildren().clear();
            showMessage("Generating AI recommendations for " + selected.getName() + "...", false);

            // 1. Build Model from students' course history
            MahoutDataModelBuilder builder = new MahoutDataModelBuilder();
            GenericDataModel dataModel = builder.buildModel(enrollmentService.getStudents());

            // 2. Setup AI Components (Similarity + GPA-aware Neighborhood)
            UserSimilarity similarity = new EuclideanDistanceSim(dataModel);
            Map<String, Double> gpas = enrollmentService.getStudents().stream()
                    .collect(Collectors.toMap(Student::getId, Student::getGpa));
            UserNeighborhood neighborhood = new KNNUserNeighborhood(5, similarity, dataModel, gpas);
            Recommender recommender = new GenericUserBasedRecommender(dataModel, neighborhood, similarity);

            // 3. Get Raw Recommendations
            List<RecommendedItem> results = recommender.recommend(selected.getId(), 10);

            // 4. Post-Filtering (Remove duplicates & filter by major)
            List<Course> allCourses = enrollmentService.getCourses();
            DefaultPostFilter filter = new DefaultPostFilter(results, dataModel);
            filter.removeAlreadyEnrolled(selected.getId());

            Map<String, String> courseMajors = allCourses.stream()
                    .collect(Collectors.toMap(Course::getCourseId, Course::getMajor, (a, b) -> a));
            filter.removeOtherMajors(selected.getMajor(), courseMajors);

            filter.limitResults(5);
            List<RecommendedItem> filteredResults = filter.getRecommendations();

            // 5. Dynamic UI Rendering
            if (filteredResults.isEmpty()) {
                Label noRecs = new Label(
                        "No recommendations found based on similar students in " + selected.getMajor());
                noRecs.getStyleClass().add("enrollment-small-text");
                recommendationsContainer.getChildren().add(noRecs);
            } else {
                for (RecommendedItem item : filteredResults) {
                    Course course = enrollmentService.findCourse(item.getItemID());
                    if (course != null) {
                        // Dynamic Match Percentage
                        int matchPercentage = Math.round(item.getValue() * 100);
                        String matchText = matchPercentage + "% Match";

                        recommendationsContainer.getChildren().add(createRecommendationCard(course, matchText));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Recommendation engine error: " + e.getMessage(), true);
        }
    }

    private VBox createRecommendationCard(Course course, String matchText) {
        VBox card = new VBox(14);
        card.getStyleClass().add("ai-card");
        card.setPadding(new javafx.geometry.Insets(14));

        // Header (Course Code & Match Badge)
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label codeLabel = new Label(course.getCourseId());
        codeLabel.getStyleClass().add("course-badge");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label matchLabel = new Label(matchText);
        matchLabel.getStyleClass().add("match-badge");
        header.getChildren().addAll(codeLabel, spacer, matchLabel);

        // Body (Course Title)
        Label titleLabel = new Label(course.getName());
        titleLabel.getStyleClass().add("course-name");
        titleLabel.setWrapText(true);

        // Footer (Metadata Tags & Action)
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);
        Label creditsTag = new Label(course.getCredits() + " Credits");
        creditsTag.getStyleClass().add("tag-badge");
        Label instructorLabel = new Label("Instructor: " + course.getInstructor());
        instructorLabel.getStyleClass().add("tag-badge");
        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);
        Button enrollBtn = new Button(course.isfull() ? "Join Waitlist" : "Enroll");
        enrollBtn.getStyleClass().add("admin-button");
        enrollBtn.setPickOnBounds(true);
        enrollBtn.setOnAction(e -> handleEnroll(course));
        footer.getChildren().addAll(creditsTag, instructorLabel, footerSpacer, enrollBtn);

        card.getChildren().addAll(header, titleLabel, footer);
        card.setPickOnBounds(true);
        return card;
    }

    private void handleEnroll(Course course) {
        Student student = studentComboBox.getValue();
        if (student == null) {
            showMessage("Please select a student first", true);
            return;
        }
        javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        StageBranding.apply(confirm);
        confirm.setHeaderText("Enroll " + student.getName() + "?");
        confirm.setContentText("Are you sure you want to enroll this student in " + course.getCourseId() + "?");
        
        java.util.Optional<javafx.scene.control.ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
            try {
                enrollmentService.enroll(student, course);
                String action = course.isfull() ? "waitlisted" : "enrolled";
                String msg = "Successfully " + action + " " + student.getName() + " in " + course.getCourseId();
                showMessage(msg, false);
                showAlert(msg); // Popup confirmation
                javafx.application.Platform.runLater(this::generateRecommendations);
            } catch (Exception e) {
                showMessage("Error: " + e.getMessage(), true);
                e.printStackTrace();
            }
        }
    }

    private void configureComboBoxes() {
        studentComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Student student) {
                if (student == null)
                    return "";
                return student.getName() + " - " + student.getId() + " - " + student.getMajor();
            }

            @Override
            public Student fromString(String value) {
                return null;
            }
        });
    }

    private void loadComboBoxData() {
        studentComboBox.getItems().setAll(enrollmentService.getStudents());
    }

    private void updateSelectedStudent(Student student) {
        boolean hasStudent = student != null;
        selectedStudentCard.setManaged(hasStudent);
        selectedStudentCard.setVisible(hasStudent);
        if (hasStudent) {
            studentInitialsLabel.setText(initials(student.getName()));
            selectedStudentNameLabel.setText(student.getName());
            selectedStudentMetaLabel.setText("GPA " + student.getGpa() + " - Year " + student.getAcademicYear());
        }
    }

    private void showMessage(String message, boolean error) {
        enrollmentMessageLabel.setText((error ? "! " : "\u2713 ") + message);
        enrollmentMessageLabel.getStyleClass().removeAll("enrollment-message-success", "enrollment-message-error");
        enrollmentMessageLabel.getStyleClass().add(error ? "enrollment-message-error" : "enrollment-message-success");
        enrollmentMessageLabel.setManaged(true);
        enrollmentMessageLabel.setVisible(true);
    }

    private String initials(String name) {
        if (name == null || name.isBlank())
            return "--";
        String[] parts = name.trim().split("\\s+");
        String first = parts[0].substring(0, 1);
        String second = parts.length > 1 ? parts[1].substring(0, 1) : "";
        return (first + second).toUpperCase(Locale.ROOT);
    }
}
