package com.example.styletimeandroidapp.models;

public class Appointment {
    private String id;
    private String userId;
    private String treatment;
    private String date;
    private String time;
    private int isAvailable; // ✅ 0 = פנוי, 1 = תפוס

    public Appointment() {}

    public Appointment(String id, String userId, String treatment, String date, String time, int isAvailable) {
        this.id = id;
        this.userId = userId;
        this.treatment = treatment;
        this.date = date;
        this.time = time;
        this.isAvailable = isAvailable;
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getTreatment() { return treatment; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public int getIsAvailable() { return isAvailable; }

    public void setIsAvailable(int isAvailable) { this.isAvailable = isAvailable; }
}
