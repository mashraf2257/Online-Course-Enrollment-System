package com.advanced_project.online_course_enrollment.model;

public class Admin {
    public String adminId;
    private String password;
    public String role;

    public Admin() {
    }

    public Admin(String adminId, String password, String role) {
        this.adminId = adminId;
        this.password = password;
        this.role = role;
    }

    public String getAdminId() {
        return adminId;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isSuper() {
        return ("Super".equals(role));
    }

    public boolean isAdmin() {
        return ("Admin".equals(role));
    }
}
