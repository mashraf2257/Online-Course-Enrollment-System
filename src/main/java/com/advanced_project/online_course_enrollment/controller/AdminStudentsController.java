package com.advanced_project.online_course_enrollment.controller;

import com.advanced_project.online_course_enrollment.model.Student;
import com.advanced_project.online_course_enrollment.service.StudentService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class AdminStudentsController {

    @FXML
    private TableView<Student> studentsTable;
    @FXML
    private TableColumn<Student, String> idColumn;
    @FXML
    private TableColumn<Student, String> nameColumn;
    @FXML
    private TableColumn<Student, String> majorColumn;
    @FXML
    private TableColumn<Student, String> yearColumn;
    @FXML
    private TableColumn<Student, Number> gpaColumn;
    @FXML
    private TableColumn<Student, Number> creditsColumn;
    @FXML
    private TableColumn<Student, Number> enrolledColumn;

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> majorFilter;
    @FXML
    private ComboBox<String> yearFilter;

    @FXML
    private TextField idField;
    @FXML
    private TextField nameField;
    @FXML
    private ComboBox<String> majorField;
    @FXML
    private ComboBox<String> yearField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField gpaField;
    @FXML
    private TextField creditsField;
    @FXML
    private Label statusLabel;
    @FXML
    private Label resultCountLabel;

    private final StudentService studentService = new StudentService();
    private final ObservableList<Student> students = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configureTable();
        configureFilters();
        refreshStudents();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> refreshStudents());
        majorFilter.valueProperty().addListener((observable, oldValue, newValue) -> refreshStudents());
        yearFilter.valueProperty().addListener((observable, oldValue, newValue) -> refreshStudents());
        studentsTable.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldStudent, selectedStudent) -> fillForm(selectedStudent));
    }


    @FXML
    private void updateStudent() {
        Student selectedStudent = studentsTable.getSelectionModel().getSelectedItem();
        if (selectedStudent == null) {
            showStatus("Select a student to update.", true);
            return;
        }

        try {
            Student updatedStudent = readForm(selectedStudent.getPassword());
            studentService.updateStudent(selectedStudent.getId(), updatedStudent);
            refreshAfterSave(updatedStudent.getId());
            showStatus("Student updated.", false);
        } catch (IllegalArgumentException e) {
            showStatus(e.getMessage(), true);
        }
    }

    @FXML
    private void deleteStudent() {
        Student selectedStudent = studentsTable.getSelectionModel().getSelectedItem();
        if (selectedStudent == null) {
            showStatus("Select a student to delete.", true);
            return;
        }

        try {
            studentService.deleteStudent(selectedStudent.getId());
            refreshStudents();
            clearForm();
            showStatus("Student deleted.", false);
        } catch (IllegalArgumentException e) {
            showStatus(e.getMessage(), true);
        }
    }

    @FXML
    private void clearForm() {
        studentsTable.getSelectionModel().clearSelection();
        idField.clear();
        nameField.clear();
        emailField.clear();
        majorField.getSelectionModel().clearSelection();
        yearField.getSelectionModel().clearSelection();
        gpaField.clear();
        creditsField.clear();
        showStatus("", false);
    }

    @FXML
    private void clearFilters() {
        searchField.clear();
        majorFilter.setValue("All");
        yearFilter.setValue("All");
    }

    private void configureTable() {
        idColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        nameColumn.setCellFactory(column -> new TableCell<>() {
            private final HBox rowBox = new HBox(10);
            private final StackPane avatar = new StackPane();
            private final Label initials = new Label();
            private final VBox textBox = new VBox(2);
            private final Label name = new Label();
            private final Label email = new Label();

            {
                avatar.getStyleClass().add("student-avatar");
                initials.getStyleClass().add("student-avatar-text");
                avatar.getChildren().add(initials);

                name.getStyleClass().add("student-name-text");
                email.getStyleClass().add("student-email-text");

                textBox.getChildren().addAll(name, email);
                rowBox.setAlignment(Pos.CENTER_LEFT);
                rowBox.getChildren().addAll(avatar, textBox);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                Student student = getTableView().getItems().get(getIndex());
                initials.setText(initials(student.getName()));
                name.setText(student.getName());
                email.setText(student.getEmail());
                setGraphic(rowBox);
                setText(null);
            }
        });
        majorColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMajor()));
        yearColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAcademicYear()));
        gpaColumn.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getGpa()));
        creditsColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getEarnedCredits()));
        enrolledColumn.setCellValueFactory(
                data -> new SimpleIntegerProperty(studentService.countEnrolledCourses(data.getValue().getId())));

        studentsTable.setItems(students);
    }

    private void configureFilters() {
        majorFilter.getItems().setAll("All");
        majorFilter.getItems().addAll(studentService.getMajors());
        majorFilter.setValue("All");
        majorField.getItems().setAll(studentService.getMajors());

        yearFilter.getItems().setAll("All");
        yearFilter.getItems().addAll(studentService.getAcademicYears());
        yearFilter.setValue("All");
        yearField.getItems().setAll(studentService.getAcademicYears());
    }

    private void refreshStudents() {
        String searchText = searchField == null ? "" : searchField.getText();
        String major = majorFilter == null ? "All" : majorFilter.getValue();
        String year = yearFilter == null ? "All" : yearFilter.getValue();

        students.setAll(studentService.findStudents(searchText, major, year));
        resultCountLabel.setText(students.size() + " student(s)");
    }

    private void refreshFilters() {
        String selectedMajor = majorFilter.getValue();
        String selectedYear = yearFilter.getValue();

        majorFilter.getItems().setAll("All");
        majorFilter.getItems().addAll(studentService.getMajors());
        majorFilter.setValue(majorFilter.getItems().contains(selectedMajor) ? selectedMajor : "All");

        yearFilter.getItems().setAll("All");
        yearFilter.getItems().addAll(studentService.getAcademicYears());
        yearFilter.setValue(yearFilter.getItems().contains(selectedYear) ? selectedYear : "All");
    }

    private void refreshAfterSave(String studentId) {
        refreshFilters();
        refreshStudents();

        for (Student student : students) {
            if (student.getId().equals(studentId)) {
                studentsTable.getSelectionModel().select(student);
                studentsTable.scrollTo(student);
                return;
            }
        }
    }

    private void fillForm(Student student) {
        if (student == null) {
            return;
        }

        idField.setText(student.getId());
        nameField.setText(student.getName());
        emailField.setText(student.getEmail());
        majorField.setValue(student.getMajor());
        yearField.setValue(student.getAcademicYear());
        gpaField.setText(String.valueOf(student.getGpa()));
        creditsField.setText(String.valueOf(student.getEarnedCredits()));
        showStatus("", false);
    }

    private Student readForm(String pass) {
        String id = requireText(idField, "Student ID is required.");
        String name = requireText(nameField, "Name is required.");
        String major = requireCombo(majorField, "Major is required.");
        String academicYear = requireCombo(yearField, "Academic year is required.");
        String email = requireText(emailField, "Email is required.");

        double gpa = parseGpa();
        int credits = parseCredits();

        Student student = new Student(id, name, pass, major, academicYear, email);
        student.setGpa(gpa);
        student.setEarnedCredits(credits);
        return student;
    }

    private String requireText(TextField field, String message) {
        String value = field.getText() == null ? "" : field.getText().trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private String requireCombo(ComboBox<String> comboBox, String message) {
        String value = comboBox.getValue() == null ? "" : comboBox.getValue().trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private double parseGpa() {
        String value = gpaField.getText() == null ? "" : gpaField.getText().trim();
        if (value.isEmpty()) {
            return 0.0;
        }

        try {
            double gpa = Double.parseDouble(value);
            if (gpa < 0.0 || gpa > 4.0) {
                throw new IllegalArgumentException("GPA must be between 0.0 and 4.0.");
            }
            return gpa;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("GPA must be a number.");
        }
    }

    private int parseCredits() {
        String value = creditsField.getText() == null ? "" : creditsField.getText().trim();
        if (value.isEmpty()) {
            return 0;
        }

        try {
            int credits = Integer.parseInt(value);
            if (credits < 0) {
                throw new IllegalArgumentException("Earned credits cannot be negative.");
            }
            return credits;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Earned credits must be a whole number.");
        }
    }

    private void showStatus(String message, boolean error) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("label-error", "label-success");
        if (message == null || message.isBlank()) {
            return;
        }
        statusLabel.getStyleClass().add(error ? "label-error" : "label-success");
    }

    private String initials(String name) {
        if (name == null || name.isBlank()) {
            return "--";
        }
        String[] parts = name.trim().split("\\s+");
        String first = parts[0].substring(0, 1);
        String second = parts.length > 1 ? parts[1].substring(0, 1) : "";
        return (first + second).toUpperCase();
    }
}
