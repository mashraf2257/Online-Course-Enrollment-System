package com.advanced_project.online_course_enrollment.data;

import com.advanced_project.online_course_enrollment.model.Admin;
import com.advanced_project.online_course_enrollment.model.AuditLog;
import com.advanced_project.online_course_enrollment.model.Course;
import com.advanced_project.online_course_enrollment.model.Enrollment;
import com.advanced_project.online_course_enrollment.model.Student;

import java.util.*;
import java.util.stream.Collectors;
import com.advanced_project.online_course_enrollment.service.WaitlistService;

public class DataStore {

    private static final int MAX_REGISTERED_CREDITS_GOOD_STANDING = 21;
    private static final int MAX_REGISTERED_CREDITS_PROBATION = 12;
    private static final double GOOD_STANDING_GPA = 2.0;
    private static DataStore instance;
    private static boolean loaded;
    private static boolean loading;

    // storages
    private static final Map<String, Student> studentMap = new HashMap<>();
    private static final Map<String, Course> courseMap = new HashMap<>();
    private static final Map<String, Admin> adminMap = new HashMap<>();
    private static final List<Enrollment> enrollmentLog = new ArrayList<>();
    private static final List<Enrollment> cartItems = new ArrayList<>();
    private static final List<AuditLog> logs = new ArrayList<>();

    private DataStore() {
    }

    public static DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    private static void ensureLoaded() {
        if (loaded || loading) {
            return;
        }

        loading = true;
        FileHandler.loadAllData();
        loaded = true;
        loading = false;
    }

    private static void saveChanges() {
        ensureLoaded();
        FileHandler.saveAllData();
    }

    public static void reloadFromFiles() {
        loaded = false;
        ensureLoaded();
    }

    // student methods
    public static Map<String, Student> getStudentMap() {
        ensureLoaded();
        return studentMap;
    }

    public static List<Student> getAllStudents() {
        ensureLoaded();
        return new ArrayList<>(studentMap.values());
    }

    public static Student getStudent(String id) {
        ensureLoaded();
        return studentMap.get(id);
    }

    public static boolean studentExists(String id) {
        ensureLoaded();
        return studentMap.containsKey(id);
    }

    private static void normalizeStudent(Student student) {
        student.setId(student.getId().toUpperCase());
        student.setMajor(student.getMajor().toUpperCase());
        student.setAcademicYear(student.getAcademicYear().toUpperCase());

        String s = Arrays.stream(student.getName().split("\\s+"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
        student.setName(s);
    }

    public static void addStudent(Student student) {
        normalizeStudent(student);
        ensureLoaded();
        if (studentExists(student.getId())) {
            throw new IllegalArgumentException("Student already exists.");
        }
        studentMap.put(student.getId(), student);
        saveChanges();
    }

    public static void updateStudent(String originalId, Student updatedStudent) {
        ensureLoaded();
        if (!studentMap.containsKey(originalId)) {
            throw new IllegalArgumentException("Selected student no longer exists.");
        }
        if (!originalId.equals(updatedStudent.getId()) && studentMap.containsKey(updatedStudent.getId())) {
            throw new IllegalArgumentException("Student ID already exists.");
        }

        studentMap.remove(originalId);
        studentMap.put(updatedStudent.getId(), updatedStudent);
        saveChanges();
    }

    public static void removeStudent(String studentId) {
        ensureLoaded();
        if (studentMap.remove(studentId) == null) {
            throw new IllegalArgumentException("Selected student no longer exists.");
        }

        enrollmentLog.removeIf(e -> e.getStudentId().equals(studentId));
        saveChanges();
    }

    public List<Course> getEnrolledCourses(String studentId) {
        ensureLoaded();
        return enrollmentLog.stream()
                .filter(e -> e.getStudentId().equals(studentId))
                .map(e -> courseMap.get(e.getCourseId()))
                .filter(course -> course != null)
                .collect(Collectors.toList());
    }

    public static int getEnrolledCourseCount(String studentId) {
        ensureLoaded();
        int count = 0;
        for (Enrollment enrollment : enrollmentLog) {
            if (studentId.equals(enrollment.getStudentId())) {
                count++;
            }
        }
        return count;
    }

    public static int getMaxRegisteredCredits(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("Student is required.");
        }
        return student.getGpa() >= GOOD_STANDING_GPA
                ? MAX_REGISTERED_CREDITS_GOOD_STANDING
                : MAX_REGISTERED_CREDITS_PROBATION;
    }

    public static int getRegisteredCredits(String studentId) {
        ensureLoaded();
        return getRegisteredCredits(studentId, null);
    }

    private static int getRegisteredCredits(String studentId, Enrollment excludedEnrollment) {
        return enrollmentLog.stream()
                .filter(enrollment -> studentId.equals(enrollment.getStudentId()))
                .filter(DataStore::countsTowardCreditLimit)
                .filter(enrollment -> !sameEnrollment(enrollment, excludedEnrollment))
                .map(Enrollment::getCourseId)
                .map(courseMap::get)
                .filter(course -> course != null)
                .mapToInt(Course::getCredits)
                .sum();
    }

    private static boolean countsTowardCreditLimit(Enrollment enrollment) {
        return enrollment != null && !"Rejected".equals(enrollment.getStatus());
    }

    private static boolean sameEnrollment(Enrollment left, Enrollment right) {
        return left != null
                && right != null
                && Objects.equals(left.getStudentId(), right.getStudentId())
                && Objects.equals(left.getCourseId(), right.getCourseId());
    }

    public static void validateRegisteredCreditLimit(Student student, Course course) {
        validateRegisteredCreditLimit(student, course, null);
    }

    private static void validateRegisteredCreditLimit(Student student, Course course, Enrollment excludedEnrollment) {
        ensureLoaded();
        if (student == null) {
            throw new IllegalArgumentException("Student is required.");
        }
        if (course == null) {
            throw new IllegalArgumentException("Course is required.");
        }

        int maxCredits = getMaxRegisteredCredits(student);
        int registeredCredits = getRegisteredCredits(student.getId(), excludedEnrollment);
        int requestedCredits = registeredCredits + course.getCredits();
        if (requestedCredits > maxCredits) {
            throw new IllegalArgumentException("Students with GPA "
                    + (student.getGpa() >= GOOD_STANDING_GPA ? "2.0 or higher" : "below 2.0")
                    + " cannot register more than " + maxCredits + " credits.");
        }
    }

    public Student authenticate(String id, String pw) {
        ensureLoaded();
        Student student = studentMap.get(id);
        if (student != null && student.getPassword().equals(pw)) {
            return student;
        }
        return null;
    }

    public Student authenticateByUsername(String username, String password) {
        ensureLoaded();
        return studentMap.values().stream()
                .filter(s -> username.equalsIgnoreCase(s.getName()) && password.equals(s.getPassword()))
                .findFirst()
                .orElse(null);
    }

    // course methods
    public static Map<String, Course> getCourseMap() {
        ensureLoaded();
        return courseMap;
    }

    public static Course getCourse(String courseId) {
        ensureLoaded();
        return courseMap.get(courseId);
    }

    public static boolean courseExists(String courseId) {
        ensureLoaded();
        return courseMap.containsKey(courseId);
    }

    public static void addCourse(Course course) {
        ensureLoaded();
        if (courseExists(course.getCourseId())) {
            throw new IllegalArgumentException("Course already exists.");
        }
        courseMap.put(course.getCourseId(), course);
        saveChanges();
    }

    public static void removeCourse(String courseId) {
        ensureLoaded();
        courseMap.remove(courseId);
        enrollmentLog.removeIf(e -> e.getCourseId().equals(courseId));
        saveChanges();
    }

    public static List<Course> getAllCourses() {
        ensureLoaded();
        return new ArrayList<>(courseMap.values());
    }

    // admin methods
    public static Map<String, Admin> getAdminMap() {
        ensureLoaded();
        return adminMap;
    }

    public static Admin getAdmin(String adminId) {
        ensureLoaded();
        return adminMap.get(adminId);
    }

    public static boolean adminExists(String adminId) {
        ensureLoaded();
        return adminMap.containsKey(adminId);
    }

    public static void addAdmin(Admin admin) {
        ensureLoaded();
        if (adminExists(admin.getAdminId())) {
            throw new IllegalArgumentException("Admin already exists.");
        }
        adminMap.put(admin.getAdminId(), admin);
        saveChanges();
    }

    public Admin authenticateAdmin(String id, String password) {
        ensureLoaded();
        Admin admin = adminMap.get(id);
        if (admin != null && admin.isAdmin() && admin.getPassword().equals(password)) {
            return admin;
        }
        return null;
    }

    public Admin authenticateAdminByUsername(String adminId, String password) {
        ensureLoaded();
        return adminMap.values().stream()
                .filter(a -> adminId.equals(a.getAdminId()) && password.equals(a.getPassword()))
                .findFirst()
                .orElse(null);
    }

    public boolean isUsernameUnique(String username) {
        ensureLoaded();
        boolean studentExists = studentMap.values().stream().anyMatch(s -> username.equalsIgnoreCase(s.getName()));
        boolean adminExists = adminMap.values().stream().anyMatch(a -> username.equalsIgnoreCase(a.getAdminId()));
        return !studentExists && !adminExists;
    }

    public boolean isStudentIdUnique(String id) {
        ensureLoaded();
        return !studentMap.containsKey(id);
    }

    // cart methods

    public List<Enrollment> getCartItems() {
        ensureLoaded();
        return cartItems;
    }

    public static List<Enrollment> getCartForStudent(String studentId) {
        ensureLoaded();
        return cartItems.stream()
                .filter(e -> e.getStudentId().equals(studentId))
                .collect(Collectors.toList());
    }

    public static void addToCart(Student student, String courseId) {
        ensureLoaded();
        if (student == null) {
            throw new IllegalArgumentException("Student is required.");
        }

        Course course = courseMap.get(courseId);
        if (course == null) {
            throw new IllegalArgumentException("Course not found.");
        }
        if (isEnrolled(student.getId(), courseId)) {
            throw new IllegalArgumentException("This course is already registered.");
        }
        if (cartItems.stream().anyMatch(e -> e.getStudentId().equals(student.getId()) && e.getCourseId().equals(courseId))) {
            throw new IllegalArgumentException("This course is already in the cart.");
        }

        int maxCredits = getMaxRegisteredCredits(student);
        int registeredCredits = getRegisteredCredits(student.getId());
        int cartCredits = cartItems.stream()
                .filter(e -> e.getStudentId().equals(student.getId()))
                .map(Enrollment::getCourseId)
                .map(courseMap::get)
                .filter(c -> c != null)
                .mapToInt(Course::getCredits)
                .sum();
        
        if (registeredCredits + cartCredits + course.getCredits() > maxCredits) {
            throw new IllegalArgumentException("Adding this course exceeds the maximum registered credits limit (" + maxCredits + ").");
        }

        studentMap.putIfAbsent(student.getId(), student);
        Enrollment enrollment = new Enrollment(student.getId(), courseId);
        cartItems.add(enrollment);
        saveChanges();
    }

    public static void removeFromCart(String studentId, String courseId) {
        ensureLoaded();
        cartItems.removeIf(e -> e.getStudentId().equals(studentId) && e.getCourseId().equals(courseId));
        saveChanges();
    }

    public static void clearCart(String studentId) {
        ensureLoaded();
        cartItems.removeIf(e -> e.getStudentId().equals(studentId));
        saveChanges();
    }

    // enrollment methods

    public List<Enrollment> getEnrollmentLog() {
        ensureLoaded();
        return enrollmentLog;
    }

    public static List<Enrollment> getAllEnrollments() {
        ensureLoaded();
        return new ArrayList<>(enrollmentLog);
    }

    public void addEnrollment(Enrollment enrollment) {
        ensureLoaded();
        enrollmentLog.add(enrollment);

        Course course = courseMap.get(enrollment.getCourseId());
        if (course != null && "Enrolled".equals(enrollment.getStatus())) {
            course.setEnrolledCount(course.getEnrolledCount() + 1);
        }
        saveChanges();
    }

    public static Enrollment registerCourse(Student student, String courseId) {
        ensureLoaded();
        if (student == null) {
            throw new IllegalArgumentException("Student is required.");
        }

        Course course = courseMap.get(courseId);
        if (course == null) {
            throw new IllegalArgumentException("Course not found.");
        }
        if (isEnrolled(student.getId(), courseId)) {
            throw new IllegalArgumentException("This course is already registered.");
        }

        validateRegisteredCreditLimit(student, course);

        studentMap.putIfAbsent(student.getId(), student);

        Enrollment enrollment;
        if (course.isfull()) {
            // Course is full — add to waitlist queue
            enrollment = WaitlistService.joinWaitlist(student, courseId);
        } else {
            // Seat available — reserve it
            enrollment = new Enrollment(student.getId(), courseId);
            enrollment.setStatus("Waiting for approval");
            course.setEnrolledCount(course.getEnrolledCount() + 1);
        }

        enrollmentLog.add(enrollment);
        saveChanges();
        return enrollment;
    }

    public void updateEnrollmentStatus(Enrollment enrollment, String status) {
        ensureLoaded();
        if (enrollment == null) {
            throw new IllegalArgumentException("Enrollment is required.");
        }

        Enrollment storedEnrollment = enrollmentLog.stream()
                .filter(e -> e.getStudentId().equals(enrollment.getStudentId())
                        && e.getCourseId().equals(enrollment.getCourseId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Enrollment no longer exists."));

        String oldStatus = storedEnrollment.getStatus();
        if (Objects.equals(oldStatus, status)) {
            return;
        }

        Course course = courseMap.get(storedEnrollment.getCourseId());
        Student student = studentMap.get(storedEnrollment.getStudentId());

        if ("Enrolled".equals(status) && !"Enrolled".equals(oldStatus)) {
            if (course != null && course.isfull()) {
                throw new IllegalArgumentException("Course is full.");
            }
            if (student != null && course != null) {
                validateRegisteredCreditLimit(student, course, storedEnrollment);
            }
            if (course != null) {
                course.setEnrolledCount(course.getEnrolledCount() + 1);
            }
        }

        if ("Enrolled".equals(oldStatus) && !"Enrolled".equals(status)) {
            if (course != null && course.getEnrolledCount() > 0) {
                course.setEnrolledCount(course.getEnrolledCount() - 1);
            }
        }

        storedEnrollment.setStatus(status);

        // If admin rejected a "Waiting for approval" enrollment, free the seat and promote next
        if ("Rejected".equals(status) && "Waiting for approval".equals(oldStatus)) {
            if (course != null && course.getEnrolledCount() > 0) {
                course.setEnrolledCount(course.getEnrolledCount() - 1);
            }
            WaitlistService.promoteNextInQueue(storedEnrollment.getCourseId());
        }

        saveChanges();
    }

    public void forceSeat(Enrollment enrollment) {
        ensureLoaded();
        if (enrollment == null) {
            throw new IllegalArgumentException("Enrollment is required.");
        }

        Enrollment storedEnrollment = enrollmentLog.stream()
                .filter(e -> e.getStudentId().equals(enrollment.getStudentId())
                        && e.getCourseId().equals(enrollment.getCourseId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Enrollment no longer exists."));

        if (!"Waitlisted".equals(storedEnrollment.getStatus())) {
            throw new IllegalArgumentException("Only waitlisted enrollments can be forced into a seat.");
        }

        Course course = courseMap.get(storedEnrollment.getCourseId());
        Student student = studentMap.get(storedEnrollment.getStudentId());

        if (course != null) {
            course.setEnrolledCount(course.getEnrolledCount() + 1);
            if (course.getEnrolledCount() > course.getCapacity()) {
                course.setCapacity(course.getEnrolledCount());
            }
        }

        storedEnrollment.setStatus("Enrolled");
        saveChanges();
    }

    public void removeEnrollment(String studentId, String courseId) {
        ensureLoaded();
        Enrollment removedEnrollment = enrollmentLog.stream()
                .filter(e -> e.getStudentId().equals(studentId) && e.getCourseId().equals(courseId))
                .findFirst()
                .orElse(null);
        boolean removed = enrollmentLog.removeIf(e -> e.getStudentId().equals(studentId)
                && e.getCourseId().equals(courseId));

        Course course = courseMap.get(courseId);
        boolean freedSeat = false;

        // Decrement enrolledCount if the removed student had a reserved seat
        if (removed && course != null && removedEnrollment != null
                && ("Enrolled".equals(removedEnrollment.getStatus())
                    || "Waiting for approval".equals(removedEnrollment.getStatus()))
                && course.getEnrolledCount() > 0) {
            course.setEnrolledCount(course.getEnrolledCount() - 1);
            freedSeat = true;
        }

        Student student = studentMap.get(studentId);

        // If a seat was freed, promote the next person in the waitlist
        if (freedSeat) {
            WaitlistService.promoteNextInQueue(courseId);
        }

        saveChanges();
    }

    public void recalculateEarnedCredits() {
        for (Student student : studentMap.values()) {
            int earned = 0;
            double totalPoints = 0.0;
            int gpaCredits = 0;

            for (Map.Entry<String, String> entry : student.getCourseHistory().entrySet()) {
                String courseId = entry.getKey();
                String grade = entry.getValue();
                Course course = courseMap.get(courseId);

                if (course != null && grade != null) {
                    int credits = course.getCredits();
                    if (!grade.equals("F") && !grade.equals("W")) {
                        earned += credits;
                    }
                    double points = gradeToPoints(grade);
                    if (points >= 0 && !grade.equals("W")) {
                        totalPoints += (points * credits);
                        gpaCredits += credits;
                    }
                }
            }

            student.setEarnedCredits(earned);
            if (gpaCredits > 0) {
                student.setGpa(Math.round((totalPoints / gpaCredits) * 100.0) / 100.0);
            } else {
                student.setGpa(0.0);
            }
        }
    }

    private double gradeToPoints(String grade) {
        switch (grade.toUpperCase()) {
            case "A+": case "A": return 4.0;
            case "A-": return 3.7;
            case "B+": return 3.3;
            case "B": return 3.0;
            case "B-": return 2.7;
            case "C+": return 2.3;
            case "C": return 2.0;
            case "C-": return 1.7;
            case "D+": return 1.3;
            case "D": return 1.0;
            case "D-": return 0.7;
            case "F": return 0.0;
            default: return -1.0; // W or unknown
        }
    }

    public static List<Student> getEnrolledStudents(String courseId) {
        ensureLoaded();
        return enrollmentLog.stream()
                .filter(e -> e.getCourseId().equals(courseId))
                .map(e -> studentMap.get(e.getStudentId()))
                .filter(student -> student != null)
                .collect(Collectors.toList());
    }

    public static boolean isEnrolled(String studentId, String courseId) {
        ensureLoaded();
        return enrollmentLog.stream()
                .anyMatch(e -> e.getStudentId().equals(studentId) && e.getCourseId().equals(courseId));
    }

    /**
     * Returns all waitlisted enrollments for a course, sorted by queue position.
     */
    public static List<Enrollment> getWaitlistedEnrollments(String courseId) {
        ensureLoaded();
        return enrollmentLog.stream()
                .filter(e -> e.getCourseId().equals(courseId))
                .filter(e -> "Waitlisted".equals(e.getStatus()))
                .sorted(Comparator.comparingInt(Enrollment::getQueuePosition))
                .collect(Collectors.toList());
    }

    // audit log methods

    public List<AuditLog> getAuditLogList() {
        ensureLoaded();
        return logs;
    }

    public void addAuditLog(AuditLog log) {
        ensureLoaded();
        logs.add(log);
        saveChanges();
    }

    public void flushToFiles() {
        saveChanges();
    }

    public void clearAll() {
        studentMap.clear();
        courseMap.clear();
        adminMap.clear();
        enrollmentLog.clear();
        cartItems.clear();
        logs.clear();
    }
}
