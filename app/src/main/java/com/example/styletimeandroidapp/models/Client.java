package com.example.styletimeandroidapp.models;

public class Client extends User {
    public Client(String userId, String name, String email) {
        super(userId, name, email);
    }

    @Override
    public String getRole() {
        return "client";
    }
}
