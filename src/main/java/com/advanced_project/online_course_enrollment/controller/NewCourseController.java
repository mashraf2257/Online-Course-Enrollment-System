package com.advanced_project.online_course_enrollment.controller;

import com.advanced_project.online_course_enrollment.model.Course;
import com.advanced_project.online_course_enrollment.service.CourseService;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class NewCourseController extends BaseController {
    private final CourseService courseService = new CourseService();

    @FXML
    private TextField courseNameField;
    @FXML
    private TextField courseIdField;
    @FXML
    private ComboBox<String> majorChoiceBox;
    @FXML
    private TextField instructorField;
    @FXML
    private TextField creditsField;
    @FXML
    private TextField capacityField;
    @FXML
    private TextField prerequisitesField;

    @FXML
    public void initialize() {
        majorChoiceBox.getItems().addAll("Computer Science", "Engineering", "Business", "Bioinformatics", "Arts");
    }

    @FXML
    private void handleSave() {
        try {
            String id = getText(courseIdField).toUpperCase();
            String name = getText(courseNameField);
            String instructor = getText(instructorField);
            String major = majorChoiceBox.getValue();

            if (id.isEmpty() || name.isEmpty() || instructor.isEmpty() || major == null) {
                showError("Please fill in all fields.");
                return;
            }

            int capacity = Integer.parseInt(capacityField.getText());
            int credits = Integer.parseInt(creditsField.getText());

            if (capacity <= 0) {
                showError("Capacity must be greater than zero.");
                return;
            }

            if (credits < 0) {
                showError("Credits cannot be negative.");
                return;
            }

            Course newCourse = new Course(id, name, instructor, major, capacity, 0);
            newCourse.setCredits(credits);

            // Handle Prerequisites
            String preqsText = getText(prerequisitesField);
            if (!preqsText.isEmpty()) {
                java.util.List<String> preqsList = java.util.Arrays.stream(preqsText.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(java.util.stream.Collectors.toList());
                newCourse.setPrerequisites(preqsList);
            }
            courseService.addCourse(newCourse);
            showAlert("Course added successfully!");
            handleCancel();
        } catch (NumberFormatException e) {
            showError("Capacity and Credits must be numbers.");
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) courseNameField.getScene().getWindow();
        stage.close();
    }

    private String getText(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }
}
