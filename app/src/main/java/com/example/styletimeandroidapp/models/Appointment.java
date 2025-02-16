package com.example.styletimeandroidapp.models;

public class Appointment {
    private String appointmentId;
    private String clientId; // Will be null if the appointment is available
    private String treatmentId;
    private String startTime;
    private String endTime;
    private boolean isCancelled;
    private boolean isAvailable; // New field to track availability

    public Appointment(String appointmentId, String treatmentId, String startTime, String endTime, boolean isAvailable) {
        this.appointmentId = appointmentId;
        this.treatmentId = treatmentId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isAvailable = isAvailable;
        this.isCancelled = false;
        this.clientId = null; // No client assigned initially
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
        this.isAvailable = false; // Once a client is assigned, the slot is no longer available
    }

    public String getTreatmentId() {
        return treatmentId;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void cancelAppointment() {
        this.isCancelled = true;
        this.isAvailable = true; // Make the slot available again if canceled
        this.clientId = null; // Remove the client
    }
}
