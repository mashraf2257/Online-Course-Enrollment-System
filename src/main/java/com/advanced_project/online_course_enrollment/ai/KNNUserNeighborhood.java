package com.advanced_project.online_course_enrollment.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KNNUserNeighborhood implements UserNeighborhood {
    public int k;
    private final UserSimilarity userSimilarity;
    private final GenericDataModel dataModel;
    private final Map<String, Double> studentGpas;

    public KNNUserNeighborhood(int k, UserSimilarity userSimilarity, GenericDataModel dataModel, Map<String, Double> studentGpas) {
        this.k = k;
        this.userSimilarity = userSimilarity;
        this.dataModel = dataModel;
        this.studentGpas = studentGpas;
    }

    @Override
    public String[] getUserNeighborhood(String userId) {
        if (dataModel == null || userSimilarity == null || studentGpas == null) {
            return new String[0];
        }

        // We must know the target student's GPA to do the comparison
        if (!studentGpas.containsKey(userId)) {
            return new String[0];
        }

        double targetGpa = studentGpas.get(userId);

        LongPrimitiveIterator allUsers = dataModel.getUserIDs();
        List<UserScore> scores = new ArrayList<>();

        while (allUsers != null && allUsers.hasNext()) {
            String otherUserId = allUsers.next();
            if (otherUserId.equals(userId))
                continue;

            // 1. Check if we know the other student's GPA
            if (!studentGpas.containsKey(otherUserId)) {
                continue;
            }

            // 2. Check if the GPA difference is within 0.3
            double otherGpa = studentGpas.get(otherUserId);
            if (Math.abs(otherGpa - targetGpa) > 0.3) {
                // If the difference is larger than 0.3, skip this student completely!
                continue;
            }

            // 3. Only calculate similarity for students who passed the GPA check
            double sim = userSimilarity.userSimilarity(userId, otherUserId);

            if (sim > 0.0) {
                scores.add(new UserScore(otherUserId, sim));
            }
        }

        scores.sort((a, b) -> Double.compare(b.score, a.score));
        int limit = Math.min(k, scores.size());
        String[] neighbors = new String[limit];
        for (int i = 0; i < limit; i++) {
            neighbors[i] = scores.get(i).userId;
        }

        return neighbors;
    }

    private static class UserScore {
        String userId;
        double score;

        UserScore(String userId, double score) {
            this.userId = userId;
            this.score = score;
        }
    }
}
