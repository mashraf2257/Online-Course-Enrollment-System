package com.advanced_project.online_course_enrollment.ai;

import java.util.ArrayList;
import java.util.List;

public class GenericUserBasedRecommender implements Recommender {

    private GenericDataModel dataModel;
    private UserNeighborhood neighborhood;
    private UserSimilarity similarity;

    public GenericUserBasedRecommender(GenericDataModel dataModel, UserNeighborhood neighborhood,
            UserSimilarity similarity) {
        this.dataModel = dataModel;
        this.neighborhood = neighborhood;
        this.similarity = similarity;
    }

    @Override
    public List<RecommendedItem> recommend(String userId, int n) {
        List<RecommendedItem> recommendations = new ArrayList<>();

        if (dataModel == null || neighborhood == null || similarity == null) {
            return recommendations;
        }

        // 1. Get the neighbors
        String[] neighbors = neighborhood.getUserNeighborhood(userId);

        // 2. Loop through every neighbor
        for (int i = 0; i < neighbors.length; i++) {
            String neighborId = neighbors[i];
            float simScore = (float) similarity.userSimilarity(userId, neighborId);

            // Look at all courses this specific neighbor is taking
            LongPrimitiveIterator courses = dataModel.getItemIDsFromUser(neighborId);

            while (courses != null && courses.hasNext()) {
                String courseId = courses.next();

                // Check if we already added this course to our recommendations list
                boolean alreadyAdded = false;
                for (RecommendedItem rec : recommendations) {
                    if (rec.getItemID().equals(courseId)) {
                        alreadyAdded = true;
                        // If we find the same course from a more similar neighbor, keep the higher score
                        // (Though neighbors are usually sorted by similarity already)
                        break;
                    }
                }

                // If it is a new course, add it with the neighbor's similarity score
                if (!alreadyAdded) {
                    final float finalScore = simScore;
                    recommendations.add(new RecommendedItem() {
                        @Override
                        public String getItemID() {
                            return courseId;
                        }

                        @Override
                        public float getValue() {
                            return finalScore;
                        }
                    });
                }
            }
        }
        
        // Sort recommendations by score descending
        recommendations.sort((a, b) -> Float.compare(b.getValue(), a.getValue()));

        // If the list is larger than n, chop it down to exactly n items!
        if (recommendations.size() > n) {
            recommendations = recommendations.subList(0, n);
        }

        return recommendations;
    }

}
