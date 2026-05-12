package com.advanced_project.online_course_enrollment.ai;

public abstract class PostFilter {
    public abstract void removeAlreadyEnrolled(String sid);

    public abstract void removeFullCourses();

    public abstract void removeOtherMajors(String studentMajor, java.util.Map<String, String> courseMajors);

    public abstract void limitResults(int n);
}
