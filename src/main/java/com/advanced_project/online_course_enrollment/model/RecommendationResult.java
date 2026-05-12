package com.advanced_project.online_course_enrollment.model;

public class RecommendationResult {
    private String courseId;
    private String courseName;
    private double matchScore;
    private String reason;
    private int seatRemaining;
    private boolean majorMatch;

    public RecommendationResult(){}

    public RecommendationResult(boolean majorMatch, int seatRemaining, double matchScore, String courseName, String courseId) {
        this.majorMatch = majorMatch;
        this.seatRemaining = seatRemaining;
        this.matchScore = matchScore;
        this.courseName = courseName;
        this.courseId = courseId;
    }

    public String getCourseId() {return courseId;}
    public String getCourseName() {return courseName;}
    public double getMatchScore() {return matchScore;}
    public int getSeatRemaining() {return seatRemaining;}
    public boolean isMajorMatch() {return majorMatch;}
    public String getReason() {return reason;}

    public void setCourseId(String courseId) {this.courseId = courseId;}
    public void setCourseName(String courseName) {this.courseName = courseName;}
    public void setMatchScore(double matchScore) {this.matchScore = matchScore;}
    public void setSeatRemaining(int seatRemaining) {this.seatRemaining = seatRemaining;}
    public void setMajorMatch(boolean majorMatch) {this.majorMatch = majorMatch;}

    public void setReason(String reason) {this.reason = reason;}

    public String getMatchPercent() {
        return Math.round(matchScore * 100) + "%";
    }

    public boolean isAvailable() {
        return seatRemaining > 0;
    }

    @Override
    public String toString() {
        return "RecommendationResult{" +
                "courseId='" + courseId + '\'' +
                ", courseName='" + courseName + '\'' +
                ", matchScore=" + getMatchPercent() +
                ", seatsRemaining=" + seatRemaining +
                ", majorMatch=" + majorMatch +
                '}';
    }

}
