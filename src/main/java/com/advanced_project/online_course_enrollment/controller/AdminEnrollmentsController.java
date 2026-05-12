package com.advanced_project.online_course_enrollment.controller;

import com.advanced_project.online_course_enrollment.model.Course;
import com.advanced_project.online_course_enrollment.model.Enrollment;
import com.advanced_project.online_course_enrollment.model.Student;
import com.advanced_project.online_course_enrollment.service.EnrollmentService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

public class AdminEnrollmentsController {

    private static final String ALL_STUDENTS = "All students";
    private static final String ALL_COURSES = "All courses";
    private static final String ALL_STATUSES = "All statuses";
    private static final String ALL_MAJORS = "All majors";
    private static final String ALL_YEARS = "All years";
    private static final java.util.List<String> MAJORS = Arrays.asList(
            "Computer Science",
            "Engineering",
            "Business",
            "Bioinformatics",
            "Arts"
    );
    private static final java.util.List<String> YEARS = Arrays.asList("1", "2", "3", "4", "5");

    @FXML private Label registryCountLabel;

    @FXML private Label enrollmentMessageLabel;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> studentFilterComboBox;
    @FXML private ComboBox<String> courseFilterComboBox;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private ComboBox<String> majorFilterComboBox;
    @FXML private ComboBox<String> yearFilterComboBox;
    @FXML private TableView<EnrollmentRow> enrollmentsTable;
    @FXML private TableColumn<EnrollmentRow, String> studentColumn;
    @FXML private TableColumn<EnrollmentRow, String> courseColumn;
    @FXML private TableColumn<EnrollmentRow, String> majorColumn;
    @FXML private TableColumn<EnrollmentRow, String> yearColumn;
    @FXML private TableColumn<EnrollmentRow, String> gpaColumn;
    @FXML private TableColumn<EnrollmentRow, String> creditsColumn;
    @FXML private TableColumn<EnrollmentRow, String> enrolledAtColumn;
    @FXML private TableColumn<EnrollmentRow, String> statusColumn;
    @FXML private TableColumn<EnrollmentRow, Void> actionsColumn;

    private final EnrollmentService enrollmentService = new EnrollmentService();
    private final ObservableList<EnrollmentRow> enrollmentRows = FXCollections.observableArrayList();
    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("M/d/yyyy, h:mm:ss a", Locale.US);

    @FXML
    public void initialize() {
        configureTable();
        configureFilters();
        refreshAll();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> refreshAll());
    }



    private void configureTable() {
        studentColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().studentDisplay()));
        studentColumn.setCellFactory(column -> new TableCell<>() {
            private final HBox rowBox = new HBox(10);
            private final StackPane avatar = new StackPane();
            private final Label initials = new Label();
            private final VBox textBox = new VBox(2);
            private final Label name = new Label();
            private final Label id = new Label();

            {
                avatar.getStyleClass().add("enrollment-avatar");
                initials.getStyleClass().add("enrollment-avatar-text");
                avatar.getChildren().add(initials);

                name.getStyleClass().add("enrollment-selected-title");
                id.getStyleClass().add("enrollment-small-text");

                textBox.getChildren().addAll(name, id);
                rowBox.setAlignment(Pos.CENTER_LEFT);
                rowBox.getChildren().addAll(avatar, textBox);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                EnrollmentRow row = getTableView().getItems().get(getIndex());
                Student student = row.student();

                if (student == null) {
                    initials.setText("--");
                    name.setText(row.enrollment().getStudentId());
                    id.setText("");
                } else {
                    initials.setText(initials(student.getName()));
                    name.setText(student.getName());
                    id.setText(student.getId());
                }

                setGraphic(rowBox);
                setText(null);
            }
        });
        courseColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().courseDisplay()));
        majorColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().studentMajor()));
        yearColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().studentYear()));
        gpaColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().studentGpa()));
        creditsColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().courseCredits()));
        enrolledAtColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().formattedTimestamp()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().statusDisplay()));

        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button acceptButton = new Button("✓");
            private final Button rejectButton = new Button("✗");
            private final Button removeButton = new Button("⌫");
            private final Button forceSeatButton = new Button("Force");
            private final HBox box = new HBox(8, acceptButton, rejectButton, removeButton, forceSeatButton);

            {
                acceptButton.setText("\u2713");
                rejectButton.setText("\u2715");
                forceSeatButton.setText("Force");
                removeButton.setText("\u232B");

                // Style classes
                acceptButton.getStyleClass().add("enrollment-confirm-button");
                rejectButton.getStyleClass().add("btn-dangerr");
                forceSeatButton.getStyleClass().add("enrollment-force-seat-button");
                removeButton.getStyleClass().add("enrollment-remove-button");

                // Tooltips
                acceptButton.setTooltip(new Tooltip("Accept enrollment"));
                rejectButton.setTooltip(new Tooltip("Reject enrollment"));
                forceSeatButton.setTooltip(new Tooltip("Force seat for waitlisted student"));
                removeButton.setTooltip(new Tooltip("Remove enrollment"));

                // Layout
                box.setAlignment(Pos.CENTER_LEFT);
                box.setPadding(new Insets(0, 4, 0, 4));

                // Actions
                acceptButton.setOnAction(event -> {
                    EnrollmentRow row = getTableView().getItems().get(getIndex());
                    try {
                        enrollmentService.acceptEnrollment(row.enrollment());
                        showMessage("Enrollment accepted", false);
                        refreshAll();
                    } catch (IllegalArgumentException e) {
                        showMessage(e.getMessage(), true);
                    }
                });

                rejectButton.setOnAction(event -> {
                    EnrollmentRow row = getTableView().getItems().get(getIndex());
                    enrollmentService.rejectEnrollment(row.enrollment());
                    showMessage("Enrollment rejected", false);
                    refreshAll();
                });

                forceSeatButton.setOnAction(event -> {
                    EnrollmentRow row = getTableView().getItems().get(getIndex());
                    try {
                        enrollmentService.forceSeat(row.enrollment());
                        showMessage("Waitlisted student enrolled with a forced seat", false);
                        refreshAll();
                    } catch (IllegalArgumentException e) {
                        showMessage(e.getMessage(), true);
                    }
                });

                removeButton.setOnAction(event -> {
                    EnrollmentRow row = getTableView().getItems().get(getIndex());
                    enrollmentService.removeEnrollment(row.enrollment());
                    registryCountLabel.setText(
                            String.valueOf(enrollmentService.getEnrollments().size())
                    );
                    showMessage("Enrollment removed", false);
                    refreshAll();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }
                EnrollmentRow row = getTableView().getItems().get(getIndex());
                boolean pending = row.isPending();
                acceptButton.setDisable(!pending);
                rejectButton.setDisable(!pending);
                forceSeatButton.setDisable(!row.isWaitlisted());
                setGraphic(box);
            }
        });

        enrollmentsTable.setItems(enrollmentRows);
        enrollmentsTable.setPlaceholder(new Label("No enrollments yet"));
    }

    private void configureFilters() {
        populateFilters();

        studentFilterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> refreshAll());
        courseFilterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> refreshAll());
        statusFilterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> refreshAll());
        majorFilterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> refreshAll());
        yearFilterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> refreshAll());
    }

    private void populateFilters() {
        studentFilterComboBox.getItems().setAll(Stream.concat(
                        Stream.of(ALL_STUDENTS),
                        enrollmentService.getStudents().stream()
                                .map(student -> displayStudentOption(student)))
                .toList());
        studentFilterComboBox.getSelectionModel().select(ALL_STUDENTS);

        courseFilterComboBox.getItems().setAll(Stream.concat(
                        Stream.of(ALL_COURSES),
                        enrollmentService.getCourses().stream()
                                .map(course -> displayCourseOption(course)))
                .toList());
        courseFilterComboBox.getSelectionModel().select(ALL_COURSES);

        statusFilterComboBox.getItems().setAll(Stream.concat(
                        Stream.of(ALL_STATUSES),
                        Stream.of("Waiting for approval", "Waitlisted", "Enrolled", "Rejected"))
                .toList());
        statusFilterComboBox.getSelectionModel().select(ALL_STATUSES);

        majorFilterComboBox.getItems().setAll(Stream.concat(
                        Stream.of(ALL_MAJORS),
                        MAJORS.stream())
                .toList());
        majorFilterComboBox.getSelectionModel().select(ALL_MAJORS);

        yearFilterComboBox.getItems().setAll(Stream.concat(
                        Stream.of(ALL_YEARS),
                        YEARS.stream())
                .toList());
        yearFilterComboBox.getSelectionModel().select(ALL_YEARS);
    }

    @FXML
    private void clearFilters() {
        searchField.clear();
        studentFilterComboBox.getSelectionModel().select(ALL_STUDENTS);
        courseFilterComboBox.getSelectionModel().select(ALL_COURSES);
        statusFilterComboBox.getSelectionModel().select(ALL_STATUSES);
        majorFilterComboBox.getSelectionModel().select(ALL_MAJORS);
        yearFilterComboBox.getSelectionModel().select(ALL_YEARS);
        refreshAll();
    }

    private void refreshAll() {
        String query = searchField == null || searchField.getText() == null
                ? ""
                : searchField.getText().trim().toLowerCase(Locale.ROOT);

        enrollmentRows.setAll(enrollmentService.getEnrollments().stream()
                .map(enrollment -> new EnrollmentRow(
                        enrollment,
                        enrollmentService.findStudent(enrollment.getStudentId()),
                        enrollmentService.findCourse(enrollment.getCourseId())))
                .filter(row -> row.matches(query))
                .filter(this::matchesSelectedFilters)
                .toList());

        registryCountLabel.setText(String.valueOf(enrollmentRows.size()));
    }

    private boolean matchesSelectedFilters(EnrollmentRow row) {
        return matchesStudentFilter(row)
                && matchesCourseFilter(row)
                && matchesExactFilter(row.statusDisplay(), statusFilterComboBox, ALL_STATUSES)
                && matchesMajorFilter(row)
                && matchesExactFilter(row.studentYear(), yearFilterComboBox, ALL_YEARS);
    }

    private boolean matchesStudentFilter(EnrollmentRow row) {
        String selectedStudent = selectedValue(studentFilterComboBox);
        if (selectedStudent == null || ALL_STUDENTS.equals(selectedStudent)) {
            return true;
        }
        return Objects.equals(row.enrollment().getStudentId(), extractValueInParentheses(selectedStudent));
    }

    private boolean matchesCourseFilter(EnrollmentRow row) {
        String selectedCourse = selectedValue(courseFilterComboBox);
        if (selectedCourse == null || ALL_COURSES.equals(selectedCourse)) {
            return true;
        }
        return Objects.equals(row.enrollment().getCourseId(), selectedCourse.split(" - ", 2)[0]);
    }

    private boolean matchesMajorFilter(EnrollmentRow row) {
        String selectedMajor = selectedValue(majorFilterComboBox);
        if (selectedMajor == null || ALL_MAJORS.equals(selectedMajor)) {
            return true;
        }
        return equalsIgnoreCase(row.studentMajor(), selectedMajor) || equalsIgnoreCase(row.courseMajor(), selectedMajor);
    }

    private boolean matchesExactFilter(String rowValue, ComboBox<String> comboBox, String allValue) {
        String selected = selectedValue(comboBox);
        return selected == null || allValue.equals(selected) || equalsIgnoreCase(rowValue, selected);
    }

    private String selectedValue(ComboBox<String> comboBox) {
        return comboBox == null ? null : comboBox.getSelectionModel().getSelectedItem();
    }

    private boolean equalsIgnoreCase(String first, String second) {
        return first != null && second != null && first.equalsIgnoreCase(second);
    }

    private String displayStudentOption(Student student) {
        return student.getName() + " (" + student.getId() + ")";
    }

    private String displayCourseOption(Course course) {
        return course.getCourseId() + " - " + course.getName();
    }

    private String extractValueInParentheses(String value) {
        int open = value.lastIndexOf('(');
        int close = value.lastIndexOf(')');
        if (open < 0 || close <= open) {
            return value;
        }
        return value.substring(open + 1, close);
    }

//        studentInitialsLabel.setText(initials(student.getName()));


    private void showMessage(String message, boolean error) {
        enrollmentMessageLabel.setText((error ? "! " : "\u2713 ") + message);
        enrollmentMessageLabel.getStyleClass().removeAll("enrollment-message-success", "enrollment-message-error");
        enrollmentMessageLabel.getStyleClass().add(error ? "enrollment-message-error" : "enrollment-message-success");
        enrollmentMessageLabel.setManaged(true);
        enrollmentMessageLabel.setVisible(true);
    }



    private String initials(String name) {
        if (name == null || name.isBlank()) {
            return "--";
        }
        String[] parts = name.trim().split("\\s+");
        String first = parts[0].substring(0, 1);
        String second = parts.length > 1 ? parts[1].substring(0, 1) : "";
        return (first + second).toUpperCase();
    }

    public record EnrollmentRow(Enrollment enrollment, Student student, Course course) {
        String studentDisplay() {
            if (student == null) {
                return enrollment.getStudentId();
            }
            return student.getName() + "\n" + student.getId();
        }

        String courseDisplay() {
            if (course == null) {
                return enrollment.getCourseId();
            }
            return course.getCourseId() + "  " + course.getName();
        }

        String formattedTimestamp() {
            LocalDateTime timestamp = enrollment.getTimestamp();
            return timestamp == null ? "" : DATE_TIME_FORMAT.format(timestamp);
        }

        boolean matches(String query) {
            if (query == null || query.isBlank()) {
                return true;
            }
            return studentDisplay().toLowerCase(Locale.ROOT).contains(query)
                    || courseDisplay().toLowerCase(Locale.ROOT).contains(query)
                    || formattedTimestamp().toLowerCase(Locale.ROOT).contains(query)
                    || statusDisplay().toLowerCase(Locale.ROOT).contains(query);
        }

        String statusDisplay() {
            return enrollment.getStatus() == null ? "" : enrollment.getStatus();
        }

        String studentMajor() {
            return student == null ? "" : student.getMajor();
        }

        String courseMajor() {
            return course == null ? "" : course.getMajor();
        }

        String studentYear() {
            return student == null ? "" : student.getAcademicYear();
        }

        String studentGpa() {
            return student == null ? "" : String.format(Locale.US, "%.2f", student.getGpa());
        }

        String courseCredits() {
            return course == null ? "" : String.valueOf(course.getCredits());
        }

        boolean isPending() {
            return "Waiting for approval".equals(statusDisplay()) || "Waitlisted".equals(statusDisplay());
        }

        boolean isWaitlisted() {
            return "Waitlisted".equals(statusDisplay());
        }
    }
}
