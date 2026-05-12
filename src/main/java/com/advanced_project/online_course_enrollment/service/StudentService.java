package com.advanced_project.online_course_enrollment.service;

import com.advanced_project.online_course_enrollment.data.DataStore;
import com.advanced_project.online_course_enrollment.model.Student;
import javafx.scene.chart.PieChart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class StudentService {

    private static final List<String> MAJORS = Arrays.asList(
            "Computer Science",
            "Engineering",
            "Business",
            "Bioinformatics",
            "Arts"
    );

    private static final List<String> ACADEMIC_YEARS = Arrays.asList("1", "2", "3", "4", "5");

    public List<Student> getAllStudents() {
        return DataStore.getAllStudents();
    }

    public List<Student> findStudents(String searchText, String majorFilter, String yearFilter) {
        String normalizedSearch = normalize(searchText);
        String normalizedMajor = normalizeFilter(majorFilter);
        String normalizedYear = normalizeFilter(yearFilter);

        return getAllStudents().stream()
                .filter(student -> matchesSearch(student, normalizedSearch))
                .filter(student -> normalizedMajor.isEmpty()
                        || normalize(student.getMajor()).equals(normalizedMajor))
                .filter(student -> normalizedYear.isEmpty()
                        || normalize(student.getAcademicYear()).equals(normalizedYear))
                .collect(Collectors.toList());
    }

    public void addStudent(Student student) {
        validateStudent(student);

        if (DataStore.studentExists(student.getId())) {
            throw new IllegalArgumentException("Student ID already exists.");
        }

        DataStore.addStudent(student);
    }

    public void updateStudent(String originalId, Student updatedStudent) {
        validateStudent(updatedStudent);

        if (!DataStore.studentExists(originalId)) {
            throw new IllegalArgumentException("Selected student no longer exists.");
        }
        if (!originalId.equals(updatedStudent.getId()) && DataStore.studentExists(updatedStudent.getId())) {
            throw new IllegalArgumentException("Student ID already exists.");
        }

        DataStore.updateStudent(originalId, updatedStudent);
    }

    public void deleteStudent(String studentId) {
        if (!DataStore.studentExists(studentId)) {
            throw new IllegalArgumentException("Selected student no longer exists.");
        }

        DataStore.removeStudent(studentId);
    }

    public int countEnrolledCourses(String studentId) {
        return DataStore.getEnrolledCourseCount(studentId);
    }

    public List<String> getMajors() {
        return MAJORS;
    }

    public List<String> getAcademicYears() {
        return ACADEMIC_YEARS;
    }

    private void validateStudent(Student student) {
        require(student.getId(), "Student ID is required.");
        require(student.getName(), "Name is required.");
        require(student.getPassword(), "Password is required.");
        require(student.getMajor(), "Major is required.");
        require(student.getAcademicYear(), "Academic year is required.");

        if (student.getGpa() < 0.0 || student.getGpa() > 4.0) {
            throw new IllegalArgumentException("GPA must be between 0.0 and 4.0.");
        }
        if (student.getEarnedCredits() < 0) {
            throw new IllegalArgumentException("Earned credits cannot be negative.");
        }

        if(Integer.parseInt(student.getAcademicYear()) < 1 || Integer.parseInt(student.getAcademicYear()) > 5)
        {
            throw new IllegalArgumentException("Invalid academic year.");
        }
        if (!MAJORS.contains(student.getMajor())) {
            throw new IllegalArgumentException("Invalid major.");
        }
    }

    private void require(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    private boolean matchesSearch(Student student, String searchText) {
        if (searchText.isEmpty()) {
            return true;
        }

        return normalize(student.getId()).contains(searchText)
                || normalize(student.getName()).contains(searchText)
                || normalize(student.getMajor()).contains(searchText)
                || normalize(student.getAcademicYear()).contains(searchText);
    }

    private String normalizeFilter(String value) {
        String normalized = normalize(value);
        return "all".equals(normalized) ? "" : normalized;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private List<String> distinctValues(Collection<String> values) {
        TreeSet<String> sortedValues = new TreeSet<>();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                sortedValues.add(value);
            }
        }
        return new ArrayList<>(sortedValues);
    }
}
