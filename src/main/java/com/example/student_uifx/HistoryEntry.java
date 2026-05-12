package com.example.student_uifx;

/**
 * Represents a completed course in the student's academic history.
 */
public class HistoryEntry {

    private final String term;
    private final Course course;
    private final String grade;

    public HistoryEntry(String term, Course course, String grade) {
        this.term = term;
        this.course = course;
        this.grade = grade;
    }

    public String getTerm()   { return term; }
    public Course getCourse() { return course; }
    public String getGrade()  { return grade; }

    /**
     * Converts letter grade to GPA points for calculation.
     */
    public double getGradePoints() {
        switch (grade) {
            case "A+": return 4.0;
            case "A":  return 4.0;
            case "A-": return 3.7;
            case "B+": return 3.3;
            case "B":  return 3.0;
            case "B-": return 2.7;
            case "C+": return 2.3;
            case "C":  return 2.0;
            case "C-": return 1.7;
            case "D":  return 1.0;
            case "F":  return 0.0;
            default:   return 0.0;
        }
    }
}
