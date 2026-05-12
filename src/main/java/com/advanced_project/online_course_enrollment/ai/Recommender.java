package com.advanced_project.online_course_enrollment.ai;

import java.util.List;

public interface Recommender {
    List<RecommendedItem> recommend(String userID, int howMany);
}
