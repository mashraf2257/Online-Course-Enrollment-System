package com.advanced_project.online_course_enrollment.controller;

import com.advanced_project.online_course_enrollment.data.DataStore;
import com.advanced_project.online_course_enrollment.model.Course;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;

import java.util.Comparator;
import java.util.List;

public class AdminDashboardController {

    @FXML private Label coursesCountLabel;
    @FXML private Label studentsCountLabel;
    @FXML private Label enrollmentsCountLabel;
//    @FXML private Label capacitySummaryLabel;
    @FXML private GridPane capacityGrid;

    @FXML
    public void initialize() {
        List<Course> courses = DataStore.getAllCourses().stream()
                .sorted(Comparator.comparing(Course::getCourseId))
                .toList();

        coursesCountLabel.setText(String.valueOf(courses.size()));
        studentsCountLabel.setText(String.valueOf(DataStore.getAllStudents().size()));
        enrollmentsCountLabel.setText(String.valueOf(DataStore.getAllEnrollments().size()));

        populateCapacityRows(courses);
    }

    private void populateCapacityRows(List<Course> courses) {
        capacityGrid.getChildren().clear();

        for (int row = 0; row < courses.size(); row++) {
            Course course = courses.get(row);
            int capacity = course.getCapacity();
            int enrolled = course.getEnrolledCount();
            double progress = capacity <= 0 ? 0.0 : Math.min(1.0, (double) enrolled / capacity);

            Label courseId = new Label(course.getCourseId());
            courseId.getStyleClass().addAll("label-muted", "capacity-course-code");

            Label courseName = new Label(course.getName());
            courseName.getStyleClass().addAll("label-main", "course-name");

            ProgressBar capacityBar = new ProgressBar(progress);
            capacityBar.setMaxWidth(Double.MAX_VALUE);
            capacityBar.getStyleClass().add("capacity-progress");
            if (capacity > 0 && enrolled >= capacity) {
                capacityBar.getStyleClass().add("capacity-progress-full");
            }

            Label count = new Label(enrolled + "/" + capacity);
            count.getStyleClass().addAll("label-sub", "capacity-count");

            capacityGrid.add(courseId, 0, row);
            capacityGrid.add(courseName, 1, row);
            capacityGrid.add(capacityBar, 2, row);
            capacityGrid.add(count, 3, row);
        }
    }

    private String buildCapacitySummary(List<Course> courses) {
        long fullCourses = courses.stream()
                .filter(course -> course.getCapacity() > 0)
                .filter(course -> course.getEnrolledCount() >= course.getCapacity())
                .count();

        if (fullCourses == 1) {
            return "1 course at full capacity.";
        }
        return fullCourses + " courses at full capacity.";
    }
}
