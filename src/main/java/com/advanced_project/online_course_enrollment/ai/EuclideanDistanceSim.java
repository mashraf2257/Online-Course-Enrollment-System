package com.advanced_project.online_course_enrollment.ai;

public class EuclideanDistanceSim implements UserSimilarity {
    private final GenericDataModel dataModel;

    public EuclideanDistanceSim(GenericDataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public double userSimilarity(String u1, String u2) {
        if (dataModel == null) return 0.0;

        LongPrimitiveIterator it1 = dataModel.getItemIDsFromUser(u1);
        LongPrimitiveIterator it2 = dataModel.getItemIDsFromUser(u2);

        java.util.Set<String> items1 = new java.util.HashSet<>();
        while (it1 != null && it1.hasNext()) items1.add(it1.next());

        java.util.Set<String> items2 = new java.util.HashSet<>();
        while (it2 != null && it2.hasNext()) items2.add(it2.next());

        if (items1.isEmpty() && items2.isEmpty()) return 0.0;

        // Calculate squared differences
        double sumSquaredDiffs = 0.0;

        java.util.Set<String> allItems = new java.util.HashSet<>(items1);
        allItems.addAll(items2);

        for (String item : allItems) {
            float pref1 = items1.contains(item) ? 1.0f : 0.0f;
            float pref2 = items2.contains(item) ? 1.0f : 0.0f;
            sumSquaredDiffs += Math.pow(pref1 - pref2, 2);
        }

        double distance = Math.sqrt(sumSquaredDiffs);

        // Convert distance to similarity score in [0.0 - 1.0] range
        // Smaller distance means higher similarity
        return 1.0 / (1.0 + distance);
    }

    @Override
    public LongPrimitiveIterator allSimilarItemIDs(String id) {
        return null; // Not typically used for UserSimilarity
    }
}
