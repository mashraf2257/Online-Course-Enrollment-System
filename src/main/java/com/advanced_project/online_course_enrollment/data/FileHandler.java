package com.advanced_project.online_course_enrollment.data;

import com.advanced_project.online_course_enrollment.model.Admin;
import com.advanced_project.online_course_enrollment.model.Student;
import com.advanced_project.online_course_enrollment.model.Enrollment;
import com.advanced_project.online_course_enrollment.model.Course;
import com.advanced_project.online_course_enrollment.model.AuditLog;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FileHandler {

    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    // JSON file paths
    private static final String STUDENTS_FILE = "data/students.json";
    private static final String COURSES_FILE = "data/courses.json";
    private static final String ADMINS_FILE = "data/admins.json";
    private static final String ENROLLMENTS_FILE = "data/enrollments.json";
    private static final String CART_FILE = "data/Enrollment_cart.json";
    private static final String AUDIT_LOGS_FILE = "data/audit_log.json";

    private FileHandler() {
    }

    public static void loadAllData() {
        DataStore store = DataStore.getInstance();
        store.clearAll();

        List<Student> students = loadList(STUDENTS_FILE, new TypeReference<List<Student>>() {
        });
        for (Student student : students) {
            store.getStudentMap().put(student.getId(), student);
        }

        List<Course> courses = loadList(COURSES_FILE, new TypeReference<List<Course>>() {
        });
        for (Course course : courses) {
            store.getCourseMap().put(course.courseId, course);
        }

        List<Admin> admins = loadList(ADMINS_FILE, new TypeReference<List<Admin>>() {
        });
        for (Admin admin : admins) {
            store.getAdminMap().put(admin.adminId, admin);
        }

        List<Enrollment> enrollments = loadList(ENROLLMENTS_FILE, new TypeReference<List<Enrollment>>() {
        });
        store.getEnrollmentLog().addAll(enrollments);
        store.recalculateEarnedCredits();

        List<AuditLog> logs = loadList(AUDIT_LOGS_FILE, new TypeReference<List<AuditLog>>() {
        });
        store.getAuditLogList().addAll(logs);

        List<Enrollment> cartItems = loadList(CART_FILE, new TypeReference<List<Enrollment>>() {});
        store.getCartItems().addAll(cartItems);

        System.out.println("DataStore successfully populated from JSON files.");
    }

    public static void saveAllData() {
        DataStore store = DataStore.getInstance();

        // Convert Maps to Lists for clean JSON array representation
        saveList(STUDENTS_FILE, new ArrayList<>(store.getStudentMap().values()));
        saveList(COURSES_FILE, new ArrayList<>(store.getCourseMap().values()));
        saveList(ADMINS_FILE, new ArrayList<>(store.getAdminMap().values()));
        saveList(ENROLLMENTS_FILE, store.getEnrollmentLog());
        saveList(CART_FILE, store.getCartItems());
        saveList(AUDIT_LOGS_FILE, store.getAuditLogList());

        System.out.println("All modifications successfully flushed to JSON files.");
    }

    private static <T> List<T> loadList(String filePath, TypeReference<List<T>> typeRef) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return new ArrayList<>();
            }
            JsonNode root = mapper.readTree(file);
            if (root == null || root.isNull()) {
                return new ArrayList<>();
            }
            if (root.isObject()) {
                List<T> values = new ArrayList<>();
                JavaType valueType = mapper.getTypeFactory()
                        .constructType(typeRef)
                        .containedTypeOrUnknown(0);
                root.elements().forEachRemaining(node -> values.add(mapper.convertValue(node, valueType)));
                return values;
            }
            return mapper.readValue(file, typeRef);
        } catch (IOException e) {
            System.err.println("Failed to read " + filePath + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private static <T> void saveList(String filePath, List<T> list) {
        try {
            mapper.writeValue(new File(filePath), list);
        } catch (IOException e) {
            System.err.println("Failed to write to " + filePath + ": " + e.getMessage());
        }
    }
}