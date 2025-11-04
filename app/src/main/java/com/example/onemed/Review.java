package com.example.onemed;

public class Review {
    private String appointmentId;
    private String userId;
    private String doctorName;
    private float rating;
    private String review;
    private long timestamp;

    public Review() {

    }

    public Review(String appointmentId, String userId, String doctorName, float rating, String review, long timestamp) {
        this.appointmentId = appointmentId;
        this.userId = userId;
        this.doctorName = doctorName;
        this.rating = rating;
        this.review = review;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getAppointmentId() { return appointmentId; }
    public String getUserId() { return userId; }
    public String getdoctorName() { return doctorName; }
    public float getRating() { return rating; }
    public String getReview() { return review; }
    public long getTimestamp() { return timestamp; }

    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public void setRating(float rating) { this.rating = rating; }
    public void setReview(String review) { this.review = review; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
