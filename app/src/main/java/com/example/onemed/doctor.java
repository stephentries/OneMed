package com.example.onemed;

import java.util.List;

public class doctor {
    private String docName;
    private String specialty;
    private List<String> qualifications;
    private float rating;
    private List<String> hospitals;
    private List<Integer> availableDays;
    private List<String> availableTimeSlots;
    private String docNumber;
    private List<String> languages;
    private String doctorImg;     // changed from 'imageName'

    public doctor() {}

    public doctor(String docName, String specialty, List<String> qualifications, float rating,
                  List<String> hospitals, List<Integer> availableDays, List<String> availableTimeSlots,
                  String doctorImg, String docNumber, List<String> languages) {
        this.docName = docName;
        this.specialty = specialty;
        this.qualifications = qualifications;
        this.rating = rating;
        this.hospitals = hospitals;
        this.availableDays = availableDays;
        this.availableTimeSlots = availableTimeSlots;
        this.doctorImg = doctorImg;
        this.docNumber = docNumber;
        this.languages = languages;
    }

    // Getters
    public String getDocName() { return docName; }
    public String getSpecialty() { return specialty; }
    public List<String> getHospitals() { return hospitals; }
    public String getDoctorImg() { return doctorImg; }
    public List<Integer> getAvailableDays() { return availableDays; }
    public List<String> getAvailableTimeSlots() { return availableTimeSlots; }
    public float getRating() { return rating; }
    public List<String> getQualifications() { return qualifications; }
    public String getDocNumber() { return docNumber; }
    public List<String> getLanguages() { return languages; }
}
