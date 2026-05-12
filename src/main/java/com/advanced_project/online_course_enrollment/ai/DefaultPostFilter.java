package com.advanced_project.online_course_enrollment.ai;

import java.util.List;

public class DefaultPostFilter extends PostFilter {
    private List<RecommendedItem> recommendations;
    private GenericDataModel dataModel;

    public DefaultPostFilter(List<RecommendedItem> recommendations, GenericDataModel dataModel) {
        this.recommendations = recommendations;
        this.dataModel = dataModel;
    }

    public List<RecommendedItem> getRecommendations() {
        return recommendations;
    }

    @Override
    public void removeAlreadyEnrolled(String studentId) {
        if (recommendations == null)
            return;

        java.util.Set<String> enrolledSet = new java.util.HashSet<>();
        
        // 1. From DataModel (Historical/History)
        if (dataModel != null) {
            LongPrimitiveIterator historyItems = dataModel.getItemIDsFromUser(studentId);
            while (historyItems != null && historyItems.hasNext()) {
                enrolledSet.add(historyItems.next());
            }
        }

        // 2. From DataStore (Active Enrollments)
        List<com.advanced_project.online_course_enrollment.model.Enrollment> active = 
            com.advanced_project.online_course_enrollment.data.DataStore.getAllEnrollments();
        if (active != null) {
            for (com.advanced_project.online_course_enrollment.model.Enrollment e : active) {
                if (e.getStudentId().equals(studentId)) {
                    enrolledSet.add(e.getCourseId());
                }
            }
        }

        recommendations.removeIf(item -> enrolledSet.contains(item.getItemID()));
    }

    @Override
    public void removeFullCourses() {
        // Left empty as per user request to let full courses appear in recommendations
    }

    @Override
    public void removeOtherMajors(String studentMajor, java.util.Map<String, String> courseMajors) {
        if (recommendations == null || studentMajor == null || courseMajors == null)
            return;

        recommendations.removeIf(item -> {
            String courseMajor = courseMajors.get(item.getItemID());
            // Filter by major using case-insensitive comparison
            return courseMajor != null && !courseMajor.equalsIgnoreCase(studentMajor);
        });
    }

    @Override
    public void limitResults(int n) {
        if (recommendations != null && recommendations.size() > n) {
            recommendations = recommendations.subList(0, n);
        }
    }
}
