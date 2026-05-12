package com.advanced_project.online_course_enrollment.service;

import com.advanced_project.online_course_enrollment.data.DataStore;

/**
 * Cannot be instantiated – subclasses must complete implementation
 * All services extend this – provides shared DataStore access
 */
public abstract class BaseService {
    protected DataStore dataStore;

    protected BaseService() {
        this.dataStore = DataStore.getInstance();
    }

    protected DataStore getDataStore() {
        return dataStore;
    }
}