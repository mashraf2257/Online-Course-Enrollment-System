package com.example.student_uifx;

/**
 * Represents a course in the academic catalog.
 */
public class Course {

    public enum Status { OPEN, FULL, LOCKED }

    private final String code;
    private final String name;
    private final String instructor;
    private final double credits;
    private final int totalSeats;
    private int enrolledSeats;
    private final String prerequisites;
    private final String schedule;
    private final String department;
    private final String yearLevel;  // "First Year", "Second Year", etc.
    private Status status;

    public Course(String code, String name, String instructor, double credits,
                  int totalSeats, int enrolledSeats, String prerequisites,
                  String schedule, String department, String yearLevel) {
        this.code = code;
        this.name = name;
        this.instructor = instructor;
        this.credits = credits;
        this.totalSeats = totalSeats;
        this.enrolledSeats = enrolledSeats;
        this.prerequisites = prerequisites;
        this.schedule = schedule;
        this.department = department;
        this.yearLevel = yearLevel;
        this.status = computeStatus();
    }

    private Status computeStatus() {
        if (prerequisites != null && !prerequisites.isEmpty()) {
            // For demo: courses with unfulfilled prereqs are LOCKED
            // This gets overridden by StudentData based on completed history
        }
        if (enrolledSeats >= totalSeats) return Status.FULL;
        return Status.OPEN;
    }

    // --- Getters ---
    public String getCode()          { return code; }
    public String getName()          { return name; }
    public String getInstructor()    { return instructor; }
    public double getCredits()       { return credits; }
    public int getTotalSeats()       { return totalSeats; }
    public int getEnrolledSeats()    { return enrolledSeats; }
    public String getPrerequisites() { return prerequisites; }
    public String getSchedule()      { return schedule; }
    public String getDepartment()    { return department; }
    public String getYearLevel()     { return yearLevel; }
    public Status getStatus()        { return status; }

    public void setStatus(Status status)         { this.status = status; }
    public void setEnrolledSeats(int seats)       { this.enrolledSeats = seats; }

    public String getSeatsDisplay() {
        return enrolledSeats + " / " + totalSeats + " Seats";
    }

    /**
     * Returns a searchable string containing all relevant fields.
     */
    public String toSearchString() {
        return (code + " " + name + " " + instructor + " " + department + " "
                + prerequisites + " " + schedule).toLowerCase();
    }

    @Override
    public String toString() {
        return code + ": " + name;
    }
}
