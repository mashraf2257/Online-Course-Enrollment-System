package com.example.student_uifx;

/**
 * Represents a student's enrollment in a course for a specific term.
 */
public class Enrollment {

    public enum Status { ENROLLED, WAITLISTED, DROPPED }

    private final Course course;
    private Status status;
    private final String term;

    public Enrollment(Course course, Status status, String term) {
        this.course = course;
        this.status = status;
        this.term = term;
    }

    public Course getCourse()   { return course; }
    public Status getStatus()   { return status; }
    public String getTerm()     { return term; }

    public void setStatus(Status status) { this.status = status; }
}
