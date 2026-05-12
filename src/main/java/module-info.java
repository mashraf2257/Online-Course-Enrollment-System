module com.advanced_project.online_course_enrollment {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires java.sql;

    exports com.example.student_uifx;

    opens com.example.student_uifx to javafx.fxml;

    opens com.advanced_project.online_course_enrollment.data to com.fasterxml.jackson.databind;

    opens com.advanced_project.online_course_enrollment to javafx.fxml;
    opens com.advanced_project.online_course_enrollment.controller to javafx.fxml;
    opens com.advanced_project.online_course_enrollment.model
            to javafx.base, com.google.gson, com.fasterxml.jackson.databind;

    exports com.advanced_project.online_course_enrollment;
}