package com.advanced_project.online_course_enrollment.ai;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class GenericDataModel {
    private final Map<String, Set<String>> userToCourseMap;
    private final Map<String, Set<String>> courseToUserMap;

    public GenericDataModel() {
        this.userToCourseMap = new HashMap<>();
        this.courseToUserMap = new HashMap<>();
    }

    public void addPreference(String userId, String courseId) {
        userToCourseMap.computeIfAbsent(userId, k -> new HashSet<>()).add(courseId);
        courseToUserMap.computeIfAbsent(courseId, k -> new HashSet<>()).add(userId);
    }

    public LongPrimitiveIterator getUserIDs() {
        return createIterator(userToCourseMap.keySet().iterator());
    }

    public LongPrimitiveIterator getItemIDsFromUser(String userID) {
        Set<String> items = userToCourseMap.get(userID);
        if (items == null) {
            items = new HashSet<>();
        }
        return createIterator(items.iterator());
    }

    public LongPrimitiveIterator getUserIDsFromItem(String itemID) {
        Set<String> users = courseToUserMap.get(itemID);
        if (users == null) {
            users = new HashSet<>();
        }
        return createIterator(users.iterator());
    }

    public float getPreferenceValue(String userID, String itemID) {
        Set<String> items = userToCourseMap.get(userID);
        if (items != null && items.contains(itemID)) {
            return 1.0f; // Implicit feedback: 1.0 if enrolled, 0.0 otherwise
        }
        return 0.0f;
    }

    private LongPrimitiveIterator createIterator(Iterator<String> baseIterator) {
        return new LongPrimitiveIterator() {
            @Override
            public boolean hasNext() {
                return baseIterator.hasNext();
            }

            @Override
            public String next() {
                return baseIterator.next();
            }
        };
    }
}
