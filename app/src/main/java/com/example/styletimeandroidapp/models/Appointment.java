package com.example.styletimeandroidapp.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Appointment {
    private String id;
    private String userId;
    private String clientName; // Add this field
    private String treatment;
    private String date;
    private String time;
    private int isAvailable;
    private Date parsedDate;

    public Appointment() {}

    public Appointment(String id, String userId, String treatment, String date, String time, int isAvailable) {
        this.id = id;
        this.userId = userId;
        this.treatment = treatment;
        this.date = date;
        this.time = time;
        this.isAvailable = isAvailable;
        this.parsedDate = parseDate(date, time);
    }

    public Appointment(String id, String userId, String clientName, String treatment, String date, String time, int isAvailable) {
        this.id = id;
        this.userId = userId;
        this.clientName = clientName; // New constructor with clientName
        this.treatment = treatment;
        this.date = date;
        this.time = time;
        this.isAvailable = isAvailable;
        this.parsedDate = parseDate(date, time);
    }

    public Appointment(String id, String userId, String treatment, String date, String time, int isAvailable, Date parsedDate) {
        this.id = id;
        this.userId = userId;
        this.treatment = treatment;
        this.date = date;
        this.time = time;
        this.isAvailable = isAvailable;
        this.parsedDate = parsedDate;
    }

    private Date parseDate(String date, String time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            return sdf.parse(date + " " + time);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date();
        }
    }

    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getClientName() { return clientName; } // Add this getter
    public String getTreatment() { return treatment; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public int getIsAvailable() { return isAvailable; }
    public Date getParsedDate() { return parsedDate; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setClientName(String clientName) { this.clientName = clientName; } // Add this setter
    public void setTreatment(String treatment) { this.treatment = treatment; }
    public void setIsAvailable(int isAvailable) { this.isAvailable = isAvailable; }

    // Set Date and update parsedDate
    public void setDate(String date) {
        this.date = date;
        this.parsedDate = parseDate(this.date, this.time);
    }

    // Set Time and update parsedDate
    public void setTime(String time) {
        this.time = time;
        this.parsedDate = parseDate(this.date, this.time);
    }
}