package com.example.student_uifx;

import com.advanced_project.online_course_enrollment.model.Student;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Central in-memory data store for the student portal.
 * Singleton — all controllers share the same data instance.
 * Contains mock data for courses, enrollments, history, and cart.
 */
public class StudentData {

    private static final StudentData INSTANCE = new StudentData();

    // --- Student Profile ---
    private final String currentTerm = "Fall 2023";
    private final double maxCredits = 21.0;

    // --- Data Collections ---
    private final ObservableList<Course> allCourses = FXCollections.observableArrayList();
    private final ObservableList<Enrollment> currentEnrollments = FXCollections.observableArrayList();
    private final ObservableList<HistoryEntry> courseHistory = FXCollections.observableArrayList();
    private final ObservableList<Course> enrollmentCart = FXCollections.observableArrayList();

    private StudentData() {
        initializeMockData();
    }

    public static StudentData getInstance() {
        return INSTANCE;
    }

    // ─────────────────────────────────────────────────────────────
    //  Mock Data Initialization
    // ─────────────────────────────────────────────────────────────

    private void initializeMockData() {
        // ── Completed courses (history) ──
        Course cs101 = new Course("CS 101", "Foundations of Computing", "Dr. K. Thompson",
                3.0, 50, 50, "", "Mon/Wed 9:00 AM", "Computer Science", "First Year");
        Course cs201 = new Course("CS 201", "Intro to Programming", "Prof. D. Knuth",
                4.0, 45, 45, "CS 101", "Tue/Thu 10:00 AM", "Computer Science", "First Year");
        Course phys101 = new Course("PHYS 101", "General Physics I", "Dr. M. Curie",
                4.0, 60, 60, "", "Mon/Wed/Fri 11:00 AM", "Engineering", "First Year");
        Course engl110 = new Course("ENGL 110", "Academic Writing", "Prof. J. Austen",
                3.0, 30, 30, "", "Tue/Thu 1:00 PM", "Business", "First Year");
        Course math101 = new Course("MATH 101", "Calculus I", "Dr. L. Euler",
                4.0, 50, 50, "", "Mon/Wed 2:00 PM", "Engineering", "First Year");

        courseHistory.addAll(
            new HistoryEntry("Spring 2023", cs201, "A"),
            new HistoryEntry("Spring 2023", phys101, "A-"),
            new HistoryEntry("Fall 2022", engl110, "B+"),
            new HistoryEntry("Fall 2022", math101, "A"),
            new HistoryEntry("Spring 2022", cs101, "B+")
        );

        // ── Currently enrolled courses ──
        Course cs301 = new Course("CS 301", "Data Structures and Algorithms", "Dr. A. Turing",
                4.0, 50, 45, "CS 201", "Mon/Wed 10:00 AM", "Computer Science", "Second Year");
        Course cs305 = new Course("CS 305", "Operating Systems", "Prof. G. Hopper",
                4.0, 40, 38, "CS 201", "Tue/Thu 1:00 PM", "Computer Science", "Second Year");
        Course math210 = new Course("MATH 210", "Discrete Mathematics", "Dr. J. Nash",
                4.0, 35, 35, "MATH 101", "Mon/Wed/Fri 9:00 AM", "Engineering", "Second Year");
        math210.setStatus(Course.Status.FULL);
        Course phys201 = new Course("PHYS 201", "Classical Mechanics", "Dr. R. Feynman",
                3.0, 30, 24, "PHYS 101", "Tue/Thu 11:00 AM", "Engineering", "Second Year");


        // ── Catalog courses (available for enrollment) ──
        Course cs341 = new Course("CS 341", "Database Systems", "Dr. E. Codd",
                4.0, 45, 32, "CS 201", "Mon/Wed 2:00 PM", "Computer Science", "Third Year");
        Course cs450 = new Course("CS 450", "Machine Learning Foundations", "Dr. A. Lovelace",
                3.0, 30, 30, "CS 301", "Tue/Thu 2:00 PM", "Computer Science", "Third Year");
        cs450.setStatus(Course.Status.FULL);
        Course cs470 = new Course("CS 470", "Intro to Artificial Intelligence", "Prof. H. Simon",
                4.0, 40, 28, "CS 301", "Mon/Wed 10:00 AM", "Computer Science", "Third Year");
        Course swe305 = new Course("SWE 305", "Software Architecture & Design", "Prof. F. Brooks",
                3.0, 35, 21, "CS 201", "Tue/Thu 9:00 AM", "Computer Science", "Third Year");
        Course math220 = new Course("MATH 220", "Linear Algebra", "Dr. D. Hilbert",
                3.0, 40, 33, "MATH 101", "Mon/Wed/Fri 1:00 PM", "Engineering", "Second Year");
        Course phys402 = new Course("PHYS 402", "Quantum Mechanics II", "Dr. N. Bohr",
                4.0, 20, 12, "PHYS 301", "Tue/Thu 3:00 PM", "Engineering", "Fourth Year");
        phys402.setStatus(Course.Status.LOCKED);
        Course eng210 = new Course("ENG 210", "Technical Writing", "Prof. S. King",
                3.0, 30, 18, "", "Mon/Wed 3:00 PM", "Business", "Second Year");
        Course bio101 = new Course("BIO 101", "Intro to Biology", "Dr. C. Darwin",
                4.0, 60, 42, "", "Tue/Thu 10:00 AM", "Bioinformatics", "First Year");
        Course bio301 = new Course("BIO 301", "Molecular Biology", "Dr. R. Franklin",
                4.0, 25, 25, "BIO 101", "Mon/Wed 11:00 AM", "Bioinformatics", "Third Year");
        bio301.setStatus(Course.Status.FULL);
        Course bus201 = new Course("BUS 201", "Principles of Management", "Prof. P. Drucker",
                3.0, 50, 37, "", "Tue/Thu 1:00 PM", "Business", "Second Year");

        allCourses.addAll(cs301, cs305, math210, phys201, cs341, cs450, cs470, swe305, math220, phys402, eng210, bio101, bio301, bus201);
    }

    // ─────────────────────────────────────────────────────────────
    //  Computed Properties
    // ─────────────────────────────────────────────────────────────

    /** Total credits currently enrolled (not waitlisted). */
    public double getEnrolledCredits() {
        return currentEnrollments.stream()
                .filter(e -> e.getStatus() == Enrollment.Status.ENROLLED)
                .mapToDouble(e -> e.getCourse().getCredits())
                .sum();
    }

    /** Total credits including waitlisted. */
    public double getTotalRegistrationCredits() {
        return currentEnrollments.stream()
                .mapToDouble(e -> e.getCourse().getCredits())
                .sum();
    }

    /** Cumulative GPA computed from course history. */
    public double getCumulativeGPA() {
        if (courseHistory.isEmpty()) return 0.0;
        double totalPoints = 0;
        double totalCredits = 0;
        for (HistoryEntry entry : courseHistory) {
            double credits = entry.getCourse().getCredits();
            totalPoints += entry.getGradePoints() * credits;
            totalCredits += credits;
        }
        return totalCredits > 0 ? Math.round((totalPoints / totalCredits) * 100.0) / 100.0 : 0.0;
    }

    /** Total credits in the enrollment cart. */
    public double getCartCredits() {
        return enrollmentCart.stream().mapToDouble(Course::getCredits).sum();
    }

    /** Completed course credits from history. */
    public double getCompletedCredits() {
        return courseHistory.stream().mapToDouble(e -> e.getCourse().getCredits()).sum();
    }

    /** Check if a course code has been completed (for prerequisite validation). */
    public boolean hasCompleted(String courseCode) {
        return courseHistory.stream()
                .anyMatch(h -> h.getCourse().getCode().equalsIgnoreCase(courseCode));
    }

    /** Check if a course is already in the cart. */
    public boolean isInCart(Course course) {
        return enrollmentCart.contains(course);
    }

    /** Check if a course is currently enrolled. */
    public boolean isEnrolled(Course course) {
        return currentEnrollments.stream()
                .anyMatch(e -> e.getCourse().getCode().equals(course.getCode()));
    }

    // ─────────────────────────────────────────────────────────────
    //  Actions
    // ─────────────────────────────────────────────────────────────

    /** Add a course to the enrollment cart. */
    public boolean addToCart(Course course) {
        if (isInCart(course) || isEnrolled(course)) return false;
        enrollmentCart.add(course);
        return true;
    }

    /** Remove a course from the enrollment cart. */
    public void removeFromCart(Course course) {
        enrollmentCart.remove(course);
    }

    /** Submit all cart items as new enrollments and clear the cart. */
    public int submitEnrollment() {
        int count = 0;
        for (Course course : enrollmentCart) {
            Enrollment.Status status = (course.getStatus() == Course.Status.FULL)
                    ? Enrollment.Status.WAITLISTED
                    : Enrollment.Status.ENROLLED;
            currentEnrollments.add(new Enrollment(course, status, currentTerm));
            if (status == Enrollment.Status.ENROLLED) {
                course.setEnrolledSeats(course.getEnrolledSeats() + 1);
            }
            count++;
        }
        enrollmentCart.clear();
        return count;
    }

    // ─────────────────────────────────────────────────────────────
    //  Getters
    // ─────────────────────────────────────────────────────────────

    public String getStudentName() {
        Student student = StudentSession.getLoggedInStudent();
        return student == null ? "" : student.getName();
    }

    public String getStudentMajor() {
        Student student = StudentSession.getLoggedInStudent();
        return student == null ? "" : student.getMajor();
    }
    public String getCurrentTerm()        { return currentTerm; }
    public double getMaxCredits()         { return maxCredits; }

    public ObservableList<Course> getAllCourses()               { return allCourses; }
    public ObservableList<Enrollment> getCurrentEnrollments()   { return currentEnrollments; }
    public ObservableList<HistoryEntry> getCourseHistory()      { return courseHistory; }
    public ObservableList<Course> getEnrollmentCart()            { return enrollmentCart; }
}
