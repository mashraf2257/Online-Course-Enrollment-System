package com.example.student_uifx;

import com.advanced_project.online_course_enrollment.ai.*;
import com.advanced_project.online_course_enrollment.data.DataStore;
import com.advanced_project.online_course_enrollment.model.Student;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for the Dashboard (hello-view.fxml).
 * IMPORTANT: We use fully qualified names for Model classes (Course, Student,
 * Enrollment)
 * because this package contains local UI versions of the same names.
 */
public class HelloController extends NavigationController {

    private static final double MAX_CREDITS = 21.0;
    @FXML
    private Label profileNameLabel;
    @FXML
    private Label profileSubtitleLabel;
    @FXML
    private Label cumulativeGpaLabel;
    @FXML
    private Label registrationCreditsLabel;
    @FXML
    private Label registrationCreditsMaxLabel;
    @FXML
    private ProgressBar creditProgressBar;
    @FXML
    private VBox enrollmentListContainer;
    @FXML
    private VBox aiRecommendationsContainer;

    @FXML
    public void initialize() {
        com.advanced_project.online_course_enrollment.model.Student loggedIn = StudentSession.getLoggedInStudent();
        if (loggedIn == null)
            return;

        refreshDashboard(loggedIn);
    }

    /**
     * Refreshes the dashboard data: profile, credits, progress bar, enrollment
     * list.
     */
    private void refreshDashboard(Student loggedIn) {
        List<com.advanced_project.online_course_enrollment.model.Enrollment> enrollments = DataStore.getAllEnrollments()
                .stream()
                .filter(enrollment -> loggedIn.getId().equals(enrollment.getStudentId()))
                .collect(Collectors.toList());

        // -- Profile --
        if (profileNameLabel != null)
            profileNameLabel.setText(loggedIn.getName());
        if (profileSubtitleLabel != null)
            profileSubtitleLabel.setText(loggedIn.getMajor() + " Student");

        // -- GPA --
        if (cumulativeGpaLabel != null)
            cumulativeGpaLabel.setText(String.valueOf(loggedIn.getGpa()));

        double enrolledCredits = enrollments.stream()
                .map(com.advanced_project.online_course_enrollment.model.Enrollment::getCourseId)
                .map(DataStore::getCourse)
                .filter(c -> c != null)
                .mapToDouble(com.advanced_project.online_course_enrollment.model.Course::getCredits)
                .sum();
        int maxCredits = DataStore.getMaxRegisteredCredits(loggedIn);

        loggedIn.setEarnedCredits((int) enrolledCredits);

        if (registrationCreditsLabel != null)
            registrationCreditsLabel.setText(String.valueOf(loggedIn.getEarnedCredits()));
        if (registrationCreditsMaxLabel != null)
            registrationCreditsMaxLabel.setText(String.valueOf(maxCredits));
        if (creditProgressBar != null)
            creditProgressBar.setProgress(maxCredits == 0 ? 0 : enrolledCredits / maxCredits);

        // -- Enrollment List --
        if (enrollmentListContainer != null) {
            enrollmentListContainer.getChildren().clear();
            for (com.advanced_project.online_course_enrollment.model.Enrollment enrollment : enrollments) {
                enrollmentListContainer.getChildren().add(buildEnrollmentRow(enrollment));
            }
        }

        generateAIRecommendations(loggedIn);
    }

    private void generateAIRecommendations(com.advanced_project.online_course_enrollment.model.Student loggedIn) {
        if (aiRecommendationsContainer == null)
            return;
        aiRecommendationsContainer.getChildren().clear();

        try {
            MahoutDataModelBuilder builder = new MahoutDataModelBuilder();
            GenericDataModel dataModel = builder.buildModel(DataStore.getAllStudents());

            UserSimilarity similarity = new EuclideanDistanceSim(dataModel);
            Map<String, Double> gpas = DataStore.getAllStudents().stream()
                    .collect(Collectors.toMap(com.advanced_project.online_course_enrollment.model.Student::getId,
                            com.advanced_project.online_course_enrollment.model.Student::getGpa, (a, b) -> a));
            UserNeighborhood neighborhood = new KNNUserNeighborhood(5, similarity, dataModel, gpas);
            Recommender recommender = new GenericUserBasedRecommender(dataModel, neighborhood, similarity);

            // 3. Get Raw Recommendations - Search deep to ensure we find candidates
            List<RecommendedItem> results = recommender.recommend(loggedIn.getId(), 15);

            DefaultPostFilter filter = new DefaultPostFilter(results, dataModel);
            filter.removeAlreadyEnrolled(loggedIn.getId());
            
            // Clone results for fallback before major filtering
            List<RecommendedItem> preMajorResults = new java.util.ArrayList<>(results);
            
            Map<String, String> courseMajors = DataStore.getAllCourses().stream()
                    .collect(Collectors.toMap(com.advanced_project.online_course_enrollment.model.Course::getCourseId,
                            com.advanced_project.online_course_enrollment.model.Course::getMajor, (a, b) -> a));
            filter.removeOtherMajors(loggedIn.getMajor(), courseMajors);
            
            List<RecommendedItem> filteredResults = filter.getRecommendations();
            
            // FALLBACK: If major filtering left us with nothing, show general recommendations
            if (filteredResults.isEmpty()) {
                filteredResults = preMajorResults;
            }
            
            filter.limitResults(3);
            filteredResults = filter.getRecommendations();

            if (filteredResults.isEmpty()) {
                Label placeholder = new Label("Take more courses to see personalized recommendations!");
                placeholder.setWrapText(true);
                placeholder.getStyleClass().add("label-muted");
                placeholder.setStyle("-fx-font-style: italic; -fx-padding: 20 0 0 0;");
                aiRecommendationsContainer.getChildren().add(placeholder);
            } else {
                for (RecommendedItem item : filteredResults) {
                    com.advanced_project.online_course_enrollment.model.Course course = DataStore
                            .getCourse(item.getItemID());
                    if (course != null) {
                        int matchPercentage = Math.round(item.getValue() * 100);
                        aiRecommendationsContainer.getChildren()
                                .add(createStudentRecCard(course, matchPercentage + "% Match"));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("AI Recommendation failed: " + e.getMessage());
            Label errorLabel = new Label("Recommendation engine is warming up...");
            errorLabel.getStyleClass().add("label-muted");
            aiRecommendationsContainer.getChildren().add(errorLabel);
        }
    }

    private VBox createStudentRecCard(com.advanced_project.online_course_enrollment.model.Course course,
            String matchText) {
        VBox card = new VBox(10);
        card.getStyleClass().add("ai-card");
        card.setPadding(new javafx.geometry.Insets(16));

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label codeLabel = new Label(course.getCourseId());
        codeLabel.getStyleClass().add("course-badge");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label matchLabel = new Label(matchText);
        matchLabel.getStyleClass().add("match-badge");
        header.getChildren().addAll(codeLabel, spacer, matchLabel);

        // Content
        VBox content = new VBox(4);
        Label titleLabel = new Label(course.getName());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: -color-text-primary;");
        titleLabel.setWrapText(true);
        Label instructorLabel = new Label("Instructor: " + course.getInstructor());
        instructorLabel.getStyleClass().add("label-muted");
        instructorLabel.setStyle("-fx-font-size: 12px;");
        content.getChildren().addAll(titleLabel, instructorLabel);

        // Footer
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);
        Label creditsLabel = new Label(course.getCredits() + " Credits");
        creditsLabel.getStyleClass().add("tag-badge");

        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);

        Button viewBtn = new Button("View Details");
        viewBtn.getStyleClass().addAll("action-button", "action-button-primary");
        viewBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4 12;");
        viewBtn.setOnAction(e -> goToCourseCatalog(e));

        footer.getChildren().addAll(creditsLabel, footerSpacer, viewBtn);
        card.getChildren().addAll(header, content, footer);

        return card;
    }

    private Pane buildEnrollmentRow(com.advanced_project.online_course_enrollment.model.Enrollment enrollment) {
        com.advanced_project.online_course_enrollment.model.Course course = DataStore
                .getCourse(enrollment.getCourseId());
        if (course == null) {
            course = new com.advanced_project.online_course_enrollment.model.Course(enrollment.getCourseId(),
                    "Unknown Course", "Unknown Instructor", "General", 0, 0);
        }

        Pane row = new Pane();
        row.setPrefHeight(106.0);
        row.setPrefWidth(769.0);

        Pane iconBox = new Pane();
        iconBox.setLayoutX(46.0);
        iconBox.setLayoutY(25.0);
        iconBox.setPrefHeight(57.0);
        iconBox.setPrefWidth(75.0);
        iconBox.setStyle(
                "-fx-background-color: -app-primary-soft; -fx-border-radius: 20px; -fx-background-radius: 20px;");

        try {
            ImageView bookIcon = new ImageView(new Image(getClass().getResourceAsStream("Icons/book.png")));
            bookIcon.setFitHeight(48.0);
            bookIcon.setFitWidth(52.0);
            bookIcon.setLayoutX(13.0);
            bookIcon.setLayoutY(4.0);
            bookIcon.setPreserveRatio(true);
            iconBox.getChildren().add(bookIcon);
        } catch (Exception e) {
        }

        Label titleLabel = new Label(course.getCourseId() + ": " + course.getName());
        titleLabel.getStyleClass().add("course-title");
        titleLabel.setLayoutX(132.0);
        titleLabel.setLayoutY(20.0);
        titleLabel.setPrefHeight(37.0);
        titleLabel.setPrefWidth(233.0);

        Label subtitleLabel = new Label(course.getInstructor());
        subtitleLabel.getStyleClass().add("course-subtitle");
        subtitleLabel.setLayoutX(132.0);
        subtitleLabel.setLayoutY(50.0);
        subtitleLabel.setPrefHeight(48.0);
        subtitleLabel.setPrefWidth(193.0);
        subtitleLabel.setWrapText(true);

        Label creditsLabel = new Label(String.valueOf(course.getCredits()));
        creditsLabel.getStyleClass().add("general-number");
        creditsLabel.setLayoutX(386.0);
        creditsLabel.setLayoutY(38.0);
        creditsLabel.setStyle("-fx-border-width: 0; -fx-font-size: 20;");

        // Status
        String statusText = enrollment.getStatus();
        if ("Waitlisted".equals(statusText) && enrollment.getQueuePosition() > 0) {
            statusText = "Waitlisted #" + enrollment.getQueuePosition();
        } else if ("Waiting for approval".equals(statusText)) {
            statusText = "Awaiting Approval";
        }
        Label statusLabel = new Label(statusText);
        statusLabel.setLayoutX(560.0);
        statusLabel.setLayoutY(38.0);
        statusLabel.getStyleClass().add(getStatusStyleClass(enrollment.getStatus()));

        // Drop button
        Button dropBtn = new Button("Drop");
        dropBtn.setLayoutX(720.0);
        dropBtn.setLayoutY(38.0);
        dropBtn.getStyleClass().addAll("action-button", "action-button-danger");

        final String courseId = enrollment.getCourseId();
        final String courseName = course.getName();
        dropBtn.setOnAction(e -> {
            Stage stage = (Stage) dropBtn.getScene().getWindow();
            StyledDialog.showConfirm(
                    stage,
                    "Drop Course",
                    "Are you sure you want to drop " + courseId + ": " + courseName
                            + "? This action cannot be undone.",
                    "Drop Course",
                    () -> {
                        DataStore.getInstance().removeEnrollment(enrollment.getStudentId(), courseId);
                        Student loggedIn = StudentSession.getLoggedInStudent();
                        if (loggedIn != null) {
                            refreshDashboard(loggedIn);
                        }
                    });
        });

        row.getChildren().addAll(iconBox, titleLabel, subtitleLabel, creditsLabel, statusLabel, dropBtn);
        return row;
    }

    private String getStatusStyleClass(String status) {
        switch (status) {
            case "Waitlisted":
                return "status-waitlist";
            case "Waiting for approval":
                return "status-pending";
            case "Rejected":
                return "status-rejected";
            default:
                return "status-enrolled";
        }
    }
}
