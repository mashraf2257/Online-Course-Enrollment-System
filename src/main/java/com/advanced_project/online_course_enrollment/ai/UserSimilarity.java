package com.advanced_project.online_course_enrollment.ai;

public interface UserSimilarity {
    double userSimilarity(String u1, String u2);

    LongPrimitiveIterator allSimilarItemIDs(String id);
}
