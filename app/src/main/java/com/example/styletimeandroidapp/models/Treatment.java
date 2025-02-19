package com.example.styletimeandroidapp.models;

public class Treatment {
    private String treatmentId;
    private String name;
    private int duration; // Duration in minutes

    public Treatment() {
    }

    public Treatment(String treatmentId, String name, int duration) {
        this.treatmentId = treatmentId;
        this.name = name;
        this.duration = duration;
    }

    public Treatment(String name, int duration) {
        this.name = name;
        this.duration = duration;
    }

    public String getTreatmentId() {
        return treatmentId;
    }

    public void setTreatmentId(String treatmentId) {
        this.treatmentId = treatmentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
