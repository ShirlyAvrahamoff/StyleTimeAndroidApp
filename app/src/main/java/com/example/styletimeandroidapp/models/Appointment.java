package com.example.styletimeandroidapp.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Appointment model representing an appointment in the system.
 */
public class Appointment {
    private String id;
    private String userId;
    private String clientName; // Used for manager view
    private String treatment;
    private String date; // Stored as dd/MM/yyyy
    private String time;
    private int isAvailable;
    private Date parsedDate; // Used for chronological sorting

    /**
     * Default constructor required for Firestore deserialization.
     */
    public Appointment() {}

    /**
     * Constructor without client name.
     *
     * @param id          Appointment ID.
     * @param userId      User ID who booked the appointment.
     * @param treatment   Treatment name.
     * @param date        Appointment date (dd/MM/yyyy).
     * @param time        Appointment time (HH:mm).
     * @param isAvailable Availability status (1 = booked, 0 = available).
     */
    public Appointment(String id, String userId, String treatment, String date, String time, int isAvailable) {
        this.id = id;
        this.userId = userId;
        this.treatment = treatment;
        this.date = date;
        this.time = time;
        this.isAvailable = isAvailable;
        this.parsedDate = parseDate(date, time);
    }

    /**
     * Constructor with client name (for manager view).
     *
     * @param id          Appointment ID.
     * @param userId      User ID who booked the appointment.
     * @param clientName  Client's name.
     * @param treatment   Treatment name.
     * @param date        Appointment date (dd/MM/yyyy).
     * @param time        Appointment time (HH:mm).
     * @param isAvailable Availability status (1 = booked, 0 = available).
     */
    public Appointment(String id, String userId, String clientName, String treatment, String date, String time, int isAvailable) {
        this.id = id;
        this.userId = userId;
        this.clientName = clientName;
        this.treatment = treatment;
        this.date = date;
        this.time = time;
        this.isAvailable = isAvailable;
        this.parsedDate = parseDate(date, time);
    }

    /**
     * Constructor with parsed date (for advanced sorting).
     *
     * @param id          Appointment ID.
     * @param userId      User ID who booked the appointment.
     * @param treatment   Treatment name.
     * @param date        Appointment date.
     * @param time        Appointment time.
     * @param isAvailable Availability status.
     * @param parsedDate  Parsed Date object for sorting.
     */
    public Appointment(String id, String userId, String treatment, String date, String time, int isAvailable, Date parsedDate) {
        this.id = id;
        this.userId = userId;
        this.treatment = treatment;
        this.date = date;
        this.time = time;
        this.isAvailable = isAvailable;
        this.parsedDate = parsedDate;
    }

    /**
     * Parses date and time into a Date object for sorting.
     *
     * @param date Date in dd/MM/yyyy format.
     * @param time Time in HH:mm format.
     * @return Parsed Date object or current date if parsing fails.
     */
    private Date parseDate(String date, String time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return sdf.parse(date + " " + time);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date(); // Fallback to current date
        }
    }

    // =======================
    // Getters
    // =======================

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getClientName() { return clientName; } // For manager use
    public String getTreatment() { return treatment; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public int getIsAvailable() { return isAvailable; }
    public Date getParsedDate() { return parsedDate; }

    // =======================
    // Setters
    // =======================

    public void setId(String id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public void setTreatment(String treatment) { this.treatment = treatment; }
    public void setIsAvailable(int isAvailable) { this.isAvailable = isAvailable; }

    /**
     * Sets the date and updates the parsed date.
     */
    public void setDate(String date) {
        this.date = date;
        this.parsedDate = parseDate(this.date, this.time);
    }

    /**
     * Sets the time and updates the parsed date.
     */
    public void setTime(String time) {
        this.time = time;
        this.parsedDate = parseDate(this.date, this.time);
    }
}
