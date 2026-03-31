package org.example.services;


public class BaseService {
    protected String getCurrentUserName() {
        // In a real application, this would retrieve the username from the security context or session
        return "test_user";
    }

    protected String getCurrentUserId() {
        // In a real application, this would retrieve the user ID from the security context or session
        return "12345";
    }
}
