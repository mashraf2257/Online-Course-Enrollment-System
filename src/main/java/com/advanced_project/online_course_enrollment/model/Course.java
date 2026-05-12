package com.advanced_project.online_course_enrollment.model;

public class Course{
    public String courseId;
    public String name;
    public String instructor;
    public String major;
    public int capacity;
    public int enrolledCount;
    public int credits;

    public java.util.List<String> prerequisites = new java.util.ArrayList<>();
    public Course() {
    }
    
    public Course(String courseId,String name, String instructor, String major, int capacity, int enrolledCount){
        this.courseId = courseId;
        this.name = name;
        this.instructor = instructor;
        this.major = major;
        this.capacity = capacity;
        this.enrolledCount = enrolledCount;
    }

    public String getCourseId() {
        return courseId;
    }

    public String getName() {
        return name;
    }

    public String getInstructor() {
        return instructor;
    }

    public String getMajor() {
        return major;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getEnrolledCount() {
        return enrolledCount;
    }

    public int getCredits() {
        return credits;
    }

    public java.util.List<String> getPrerequisites() {
        return prerequisites;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setEnrolledCount(int enrolledCount) {
        this.enrolledCount = enrolledCount;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }
    public void setPrerequisites(java.util.List<String> prerequisites) {
        this.prerequisites = prerequisites;
    }

    public int remainingSeats(){
        return capacity - enrolledCount;
    }
    public boolean isfull(){
        return (capacity == enrolledCount);
    }
}
