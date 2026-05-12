package com.advanced_project.online_course_enrollment.service;

import com.advanced_project.online_course_enrollment.data.DataStore;
import com.advanced_project.online_course_enrollment.model.Course;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CourseService extends BaseService {

    public List<Course> getAllCourses() {
        return DataStore.getAllCourses().stream()
                .sorted(Comparator.comparing(Course::getCourseId))
                .collect(Collectors.toList());
    }

    public List<Course> searchByName(String query) {
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase();
        return getAllCourses().stream()
                .filter(course -> course.getName().toLowerCase().contains(normalizedQuery))
                .collect(Collectors.toList());
    }

    public void addCourse(Course course) {
        validateCourse(course);
        DataStore.addCourse(course);
    }

    public void removeCourse(String courseId) {
        if (courseId == null || courseId.trim().isEmpty()) {
            throw new IllegalArgumentException("Course ID is required.");
        }
        DataStore.removeCourse(courseId);
    }

    private void validateCourse(Course course) {
        if (course == null) {
            throw new IllegalArgumentException("Course is required.");
        }

        course.setCourseId(required(course.getCourseId(), "Course ID").toUpperCase());
        course.setName(required(course.getName(), "Course name"));
        course.setInstructor(required(course.getInstructor(), "Instructor"));
        course.setMajor(required(course.getMajor(), "Major"));

        if (course.getCapacity() <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than zero.");
        }

        if (course.getCredits() < 0) {
            throw new IllegalArgumentException("Credits cannot be negative.");
        }

        if (course.getEnrolledCount() < 0) {
            throw new IllegalArgumentException("Enrolled count cannot be negative.");
        }

        if (course.getEnrolledCount() > course.getCapacity()) {
            throw new IllegalArgumentException("Enrolled count cannot exceed capacity.");
        }
    }

    private String required(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return value.trim();
    }
}
