package com.example.styletimeandroidapp.models;

public class Treatment {
    private String treatmentId;
    private String name;
    private int duration; // Duration in minutes

    public Treatment(String treatmentId, String name, int duration) {
        this.treatmentId = treatmentId;
        this.name = name;
        this.duration = duration;
    }

    public String getTreatmentId() {
        return treatmentId;
    }

    public String getName() {
        return name;
    }

    public int getDuration() {
        return duration;
    }
}
