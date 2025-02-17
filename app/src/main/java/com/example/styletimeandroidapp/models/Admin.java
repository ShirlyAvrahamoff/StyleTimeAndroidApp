package com.example.styletimeandroidapp.models;

public class Admin extends User {
    public Admin(String userId, String name, String email) {
        super(userId, name, email);
    }

    @Override
    public String getRole() {
        return "admin";
    }
}
