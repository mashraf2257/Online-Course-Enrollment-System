package com.advanced_project.online_course_enrollment.controller;

import com.advanced_project.online_course_enrollment.model.Course;
import com.advanced_project.online_course_enrollment.service.CourseService;
import com.advanced_project.online_course_enrollment.util.StageBranding;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import java.util.List;
import java.util.stream.Collectors;

public class CourseManagementController extends BaseController {

    private final CourseService courseService = new CourseService();

    @FXML
    private TableView<Course> coursesTable;
    @FXML
    private TableColumn<Course, String> colCourseId;
    @FXML
    private TableColumn<Course, String> colCourseName;
    @FXML
    private TableColumn<Course, String> colMajor;
    @FXML
    private TableColumn<Course, String> colInstructor;
    @FXML
    private TableColumn<Course, Course> colCapacity;
    @FXML
    private TableColumn<Course, Integer> colEnrolled;
    @FXML
    private TableColumn<Course, Integer> colCredits;
    @FXML
    private TableColumn<Course, String> colPrerequisites;
    @FXML
    private TableColumn<Course, Course> colStatus;
    @FXML
    private TableColumn<Course, Course> colActions;

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> majorFilter;
    @FXML
    private ComboBox<String> statusFilter;

    @FXML
    public void initialize() {
        setupTable();
        loadData();
        setupFilters();
    }

    private <T> void setColumnStyle(TableColumn<Course, T> column, String styleClass, String alignment) {
        column.setCellFactory(col -> new TableCell<Course, T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle(""); // Reset style
                } else {
                    setText(item.toString());

                    // 1. Manage classes safely (prevents duplicates)
                    if (!getStyleClass().contains(styleClass)) {
                        getStyleClass().add(styleClass);
                    }

                    // 2. Force alignment (Guaranteed to work)
                    setStyle("-fx-alignment: " + alignment + ";");
                }
            }
        });
    }

    private void setupTable() {

        colCourseId.setCellValueFactory(new PropertyValueFactory<>("courseId"));
        colCourseName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colMajor.setCellValueFactory(new PropertyValueFactory<>("major"));
        colEnrolled.setCellValueFactory(new PropertyValueFactory<>("enrolledCount"));
        colCredits.setCellValueFactory(new PropertyValueFactory<>("credits"));
        colInstructor.setCellValueFactory(new PropertyValueFactory<>("instructor"));
        setColumnStyle(colCourseId, "id-column", "CENTER-LEFT");
        setColumnStyle(colCourseName, "name-column", "CENTER");
        setColumnStyle(colMajor, "major-column", "CENTER");
        setColumnStyle(colEnrolled, "enrolled-column", "CENTER");
        setColumnStyle(colCredits, "credits-column", "CENTER");
        setColumnStyle(colPrerequisites, "prerequisites-column", "CENTER");
        setColumnStyle(colInstructor, "instructor-column", "CENTER");
        coursesTable.getStyleClass().add("centered-table");

        colCapacity.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue()));
        colStatus.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue()));
        colActions.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue()));
        colPrerequisites.setCellValueFactory(cellData -> {
            List<String> preqs = cellData.getValue().getPrerequisites();
            return new javafx.beans.property.SimpleStringProperty(
                    preqs == null || preqs.isEmpty() ? "None" : String.join(", ", preqs));
        });

        // Capacity Bar Column
        colCapacity.setCellFactory(column -> new TableCell<Course, Course>() {
            private final ProgressBar bar = new ProgressBar();

            @Override
            protected void updateItem(Course course, boolean empty) {
                super.updateItem(course, empty);
                if (empty || course == null) {
                    setGraphic(null);
                    setStyle("");
                } else {
                    double progress = (double) course.getEnrolledCount() / course.getCapacity();
                    bar.setProgress(progress);
                    bar.setPrefWidth(column.getWidth() - 20);

                    // Apply styles from theme.css
                    bar.getStyleClass().setAll("progress-bar", "capacity-progress");
                    if (course.isfull()) {
                        bar.getStyleClass().add("capacity-progress-full");
                    }

                    setGraphic(bar);
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });

        colStatus.setCellFactory(column -> new TableCell<Course, Course>() {
            @Override
            protected void updateItem(Course course, boolean empty) {
                super.updateItem(course, empty);
                if (empty || course == null) {
                    setGraphic(null);
                    setStyle("");
                } else {
                    Label badge = new Label(course.isfull() ? "FULL" : "OPEN");
                    badge.getStyleClass().setAll("badge", course.isfull() ? "badge-full" : "badge-open");
                    setGraphic(badge);
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });

        colActions.setCellFactory(column -> new TableCell<Course, Course>() {
            private final HBox container = new HBox(10);
            private final Button deleteBtn = new Button("Delete");

            {
                deleteBtn.getStyleClass().addAll("btn-danger");
                deleteBtn.setPrefWidth(80);
                container.getChildren().addAll(deleteBtn);
                container.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Course course, boolean empty) {
                super.updateItem(course, empty);
                if (empty || course == null) {
                    setGraphic(null);
                    setStyle("");
                } else {
                    deleteBtn.setOnAction(e -> handleDeleteCourse(course.getCourseId()));
                    setGraphic(container);
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });
    }

    private void setupFilters() {
        if (majorFilter == null || statusFilter == null)
            return;

        String selectedMajor = majorFilter.getValue();
        String selectedStatus = statusFilter.getValue();

        List<String> majors = courseService.getAllCourses().stream()
                .map(Course::getMajor)
                .distinct()
                .collect(Collectors.toList());
        majorFilter.setItems(FXCollections.observableArrayList(majors));
        majorFilter.getItems().add(0, "All Majors");
        statusFilter.setItems(FXCollections.observableArrayList("All Statuses", "Open", "Full"));

        majorFilter.setValue(majorFilter.getItems().contains(selectedMajor) ? selectedMajor : "All Majors");
        statusFilter.setValue(statusFilter.getItems().contains(selectedStatus) ? selectedStatus : "All Statuses");
    }

    private void loadData() {
        coursesTable.setItems(FXCollections.observableArrayList(courseService.getAllCourses()));
    }

    @FXML
    public void handleSearch() {
        filterCourses();
    }

    @FXML
    public void handleFilterByMajor() {
        filterCourses();
    }

    @FXML
    public void handleFilterByStatus() {
        filterCourses();
    }

    private void filterCourses() {
        String query = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        String major = majorFilter.getValue();
        String status = statusFilter.getValue();

        List<Course> filtered = courseService.getAllCourses().stream()
                .filter(c -> c.getCourseId().toLowerCase().contains(query) || c.getName().toLowerCase().contains(query))
                .filter(c -> major == null || major.equals("All Majors") || c.getMajor().equals(major))
                .filter(c -> {
                    if (status == null || status.equals("All Statuses"))
                        return true;
                    return status.equals("Open") ? !c.isfull() : c.isfull();
                })
                .collect(Collectors.toList());
        coursesTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    public void handleCreateCourse(ActionEvent event) {
        handleAddCourse();
    }

    private void handleAddCourse() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass()
                    .getResource("/com/advanced_project/online_course_enrollment/fxml/admin/new_course.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = new javafx.stage.Stage();
            StageBranding.apply(stage);
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
            setupFilters();
            filterCourses();
        } catch (java.io.IOException e) {
            showError("Could not open New Course window: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void handleEditCourse(String cid) {
        System.out.println("Editing: " + cid);
    }

    public void handleDeleteCourse(String cid) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete course " + cid + "?", ButtonType.YES,
                ButtonType.NO);
        StageBranding.apply(alert);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    courseService.removeCourse(cid);
                    setupFilters();
                    filterCourses();
                } catch (IllegalArgumentException e) {
                    showError(e.getMessage());
                }
            }
        });
    }
}
