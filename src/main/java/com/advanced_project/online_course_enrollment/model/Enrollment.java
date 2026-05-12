package com.advanced_project.online_course_enrollment.model;

import com.advanced_project.online_course_enrollment.data.DataStore;

import java.time.LocalDateTime;

public class Enrollment {
    private String studentId;
    private String courseId;
    private LocalDateTime timestamp;
    private String status;
    private int queuePosition; // 0 = not queued, 1+ = position in waitlist

    public Enrollment() {
    }

    public Enrollment(String studentId, String courseId) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.timestamp = LocalDateTime.now();
        Course course = DataStore.getCourse(courseId);
        this.status = course != null && course.isfull()
                ? "Waitlisted"
                : "Waiting for approval";
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getQueuePosition() {
        return queuePosition;
    }

    public void setQueuePosition(int queuePosition) {
        this.queuePosition = queuePosition;
    }
}

