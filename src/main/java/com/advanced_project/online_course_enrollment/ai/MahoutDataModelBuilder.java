package com.advanced_project.online_course_enrollment.ai;

import java.util.List;
import com.advanced_project.online_course_enrollment.model.Student;

public class MahoutDataModelBuilder {
    public GenericDataModel buildModel(List<Student> students) {
        GenericDataModel dataModel = new GenericDataModel();
        if (students != null) {
            for (Student student : students) {
                if (student.getCourseHistory() != null) {
                    for (String courseId : student.getCourseHistory().keySet()) {
                        // Using course history as implicit feedback for recommendations
                        dataModel.addPreference(student.getId(), courseId);
                    }
                }
            }
        }
        return dataModel;
    }
}
