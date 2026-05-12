package com.advanced_project.online_course_enrollment.service;

import com.advanced_project.online_course_enrollment.data.DataStore;
import com.advanced_project.online_course_enrollment.model.Course;
import com.advanced_project.online_course_enrollment.model.Enrollment;
import com.advanced_project.online_course_enrollment.model.Student;

import java.util.Comparator;
import java.util.List;

public class EnrollmentService {

    private final DataStore dataStore = DataStore.getInstance();

    public List<Student> getStudents() {
        return DataStore.getAllStudents().stream()
                .sorted(Comparator.comparing(Student::getName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Student::getId, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public List<Course> getCourses() {
        return DataStore.getAllCourses().stream()
                .sorted(Comparator.comparing(Course::getCourseId, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public List<Enrollment> getEnrollments() {
        return DataStore.getAllEnrollments().stream()
                .sorted(Comparator.comparing(Enrollment::getTimestamp,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public Enrollment enroll(Student student, Course course) {
        if (student == null) {
            throw new IllegalArgumentException("Select a student first.");
        }
        if (course == null) {
            throw new IllegalArgumentException("Select a course first.");
        }
        if (DataStore.isEnrolled(student.getId(), course.getCourseId())) {
            throw new IllegalArgumentException("This student is already enrolled in that course.");
        }
        DataStore.validateRegisteredCreditLimit(student, course);
        return DataStore.registerCourse(student, course.getCourseId());
    }

    public void removeEnrollment(Enrollment enrollment) {
        if (enrollment == null) {
            return;
        }
        dataStore.removeEnrollment(enrollment.getStudentId(), enrollment.getCourseId());
    }

    public void acceptEnrollment(Enrollment enrollment) {
        if (enrollment == null) {
            throw new IllegalArgumentException("Select an enrollment first.");
        }
        dataStore.updateEnrollmentStatus(enrollment, "Enrolled");
    }

    public void rejectEnrollment(Enrollment enrollment) {
        if (enrollment == null) {
            return;
        }
        dataStore.updateEnrollmentStatus(enrollment, "Rejected");
    }

    public void forceSeat(Enrollment enrollment) {
        if (enrollment == null) {
            throw new IllegalArgumentException("Select an enrollment first.");
        }
        dataStore.forceSeat(enrollment);
    }

    public Student findStudent(String studentId) {
        return DataStore.getStudent(studentId);
    }

    public Course findCourse(String courseId) {
        return DataStore.getCourse(courseId);
    }

    public int countEnrollments() {
        return DataStore.getAllEnrollments().size();
    }

    public long countAcceptedEnrollments() {
        return DataStore.getAllEnrollments().stream()
                .filter(enrollment -> "Enrolled".equals(enrollment.getStatus()))
                .count();
    }

    public int countStudents() {
        return DataStore.getAllStudents().size();
    }

    public int countCourses() {
        return DataStore.getAllCourses().size();
    }
}
