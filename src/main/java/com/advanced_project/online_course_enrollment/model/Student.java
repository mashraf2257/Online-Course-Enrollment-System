
package com.advanced_project.online_course_enrollment.model;

import java.util.HashMap;
import java.util.Map;

public class Student {

    private String id;
    private String name;
    private String password;
    private String email;
    private String major;
    private String academicYear;
    private double gpa;
    private int earnedCredits;
    private Map<String, String> courseHistory = new HashMap<>(); // courseId → grade

    public Student() {
    }

    public Student(String id, String name, String password, String major, String academicYear, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.major = major;
        this.academicYear = academicYear;
        this.gpa = 0.0;
        this.earnedCredits = 0;
        this.courseHistory = new HashMap<>();
    }

    // ── Getters ──────────────────────────────────────────────────
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getMajor() {
        return major;
    }

    public String getEmail() {
        return email;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public double getGpa() {
        return gpa;
    }

    public int getEarnedCredits() {
        return earnedCredits;
    }

    public Map<String, String> getCourseHistory() {
        return courseHistory;
    }

    // ── Setters ──────────────────────────────────────────────────
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public void setAcademicYear(String year) {
        this.academicYear = year;
    }

    public void setGpa(double gpa) {
        this.gpa = gpa;
    }

    public void setEarnedCredits(int credits) {
        this.earnedCredits = credits;
    }

    public void setCourseHistory(Map<String, String> courseHistory) {
        this.courseHistory = courseHistory != null ? courseHistory : new HashMap<>();
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Student{id='" + id + "', name='" + name + "', major='" + major + "'}";
    }
}
