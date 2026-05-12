package com.example.student_uifx;

import com.advanced_project.online_course_enrollment.model.Student;

public final class StudentSession {

    private static Student loggedInStudent;

    private StudentSession() {
    }

    public static void setLoggedInStudent(Student student) {
        loggedInStudent = student;
    }

    public static Student getLoggedInStudent() {
        return loggedInStudent;
    }

    public static void clear() {
        loggedInStudent = null;
    }
}
