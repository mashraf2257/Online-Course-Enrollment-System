package com.advanced_project.online_course_enrollment.service;

import com.advanced_project.online_course_enrollment.data.DataStore;
import com.advanced_project.online_course_enrollment.model.Course;
import com.advanced_project.online_course_enrollment.model.Enrollment;
import com.advanced_project.online_course_enrollment.model.Student;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages the FIFO waitlist queue for full courses.
 * <p>
 * Responsibilities:
 * - Joining the waitlist with an auto-assigned queue position
 * - Leaving the waitlist (with position shift)
 * - Promoting the next student when a seat opens
 * - Querying waitlist state for a course or student
 */
public class WaitlistService {

    private WaitlistService() {
    }

    /**
     * Adds a student to the waitlist for a full course.
     * The student is assigned the next queue position (1-based).
     *
     * @return the created Enrollment with status "Waitlisted" and a queue position
     */
    public static Enrollment joinWaitlist(Student student, String courseId) {
        int currentCount = getWaitlistSize(courseId);

        Enrollment enrollment = new Enrollment(student.getId(), courseId);
        enrollment.setStatus("Waitlisted");
        enrollment.setQueuePosition(currentCount + 1);

        return enrollment;
    }

    /**
     * Returns all waitlisted enrollments for a course, sorted by queue position (ascending).
     */
    public static List<Enrollment> getWaitlistForCourse(String courseId) {
        return DataStore.getAllEnrollments().stream()
                .filter(e -> e.getCourseId().equals(courseId))
                .filter(e -> "Waitlisted".equals(e.getStatus()))
                .sorted(Comparator.comparingInt(Enrollment::getQueuePosition))
                .collect(Collectors.toList());
    }

    /**
     * Returns the number of students currently waitlisted for a course.
     */
    public static int getWaitlistSize(String courseId) {
        return (int) DataStore.getAllEnrollments().stream()
                .filter(e -> e.getCourseId().equals(courseId))
                .filter(e -> "Waitlisted".equals(e.getStatus()))
                .count();
    }

    /**
     * Returns the queue position for a specific student in a specific course.
     *
     * @return queue position (1-based), or 0 if the student is not waitlisted
     */
    public static int getQueuePosition(String studentId, String courseId) {
        return DataStore.getAllEnrollments().stream()
                .filter(e -> e.getStudentId().equals(studentId))
                .filter(e -> e.getCourseId().equals(courseId))
                .filter(e -> "Waitlisted".equals(e.getStatus()))
                .mapToInt(Enrollment::getQueuePosition)
                .findFirst()
                .orElse(0);
    }

    /**
     * Promotes the next student in the waitlist queue (position #1) to "Waiting for approval".
     * <p>
     * This method:
     * 1. Finds the waitlisted enrollment with queuePosition == 1
     * 2. Changes its status to "Waiting for approval" and queuePosition to 0
     * 3. Reserves the seat (enrolledCount++)
     * 4. Shifts all remaining waitlisted students' positions down by 1
     * <p>
     * Should be called whenever a seat opens (student drops or admin rejects).
     */
    public static void promoteNextInQueue(String courseId) {
        List<Enrollment> waitlisted = getWaitlistForCourse(courseId);

        if (waitlisted.isEmpty()) {
            return;
        }

        // Promote position #1
        Enrollment first = waitlisted.get(0);
        first.setStatus("Waiting for approval");
        first.setQueuePosition(0);

        // Reserve the seat
        Course course = DataStore.getCourse(courseId);
        if (course != null) {
            course.setEnrolledCount(course.getEnrolledCount() + 1);
        }

        // Shift everyone else down by 1
        for (int i = 1; i < waitlisted.size(); i++) {
            Enrollment e = waitlisted.get(i);
            e.setQueuePosition(e.getQueuePosition() - 1);
        }
    }
}
