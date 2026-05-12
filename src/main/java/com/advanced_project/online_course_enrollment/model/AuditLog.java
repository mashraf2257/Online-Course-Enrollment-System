package com.advanced_project.online_course_enrollment.model;

import java.time.LocalDateTime;

public class AuditLog {
    private String adminId;
    private String action;
    private String targetId;
    private LocalDateTime timestamp;

    public AuditLog(){}

    public AuditLog(String adminId, String action, String targetId, LocalDateTime timestamp) {
        this.adminId = adminId;
        this.action = action;
        this.targetId = targetId;
        this.timestamp = timestamp;
    }

    // getters
    public String getAdminId() {return adminId;}
    public String getAction() {return action;}
    public String getTargetId() {return targetId;}
    public LocalDateTime getTimestamp() {return timestamp;}

    // setters
    public void setAdminId(String adminId) {this.adminId = adminId;}
    public void setAction(String action) {this.action = action;}
    public void setTargetId(String targetId) {this.targetId = targetId;}
    public void setTimestamp(LocalDateTime timestamp) {this.timestamp = timestamp;}

    public String toLogLine() {
        return timestamp + " | " + adminId + " | " + action + " | " + targetId;
    }
}
