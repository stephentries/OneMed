package com.example.onemed;

import com.google.firebase.firestore.GeoPoint;

import java.util.List;

public class Hospital {
    private String hospitalname;
    private String phoneNumber;
    private GeoPoint location;
    private List<String> doctorNames;

    private String hospital_Img;

    private float rating;

    public Hospital() {
    }

    public Hospital(String hospitalname, String phoneNumber,GeoPoint location, List<String> doctorNames,String hospital_Img, float rating
    ) {
        this.hospitalname = hospitalname;
        this.phoneNumber = phoneNumber;
        this.location=location;
        this.doctorNames = doctorNames;
        this.hospital_Img=hospital_Img;
        this.rating=rating;
    }

    // Getters
    public String getHospitalname() {
        return hospitalname;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public GeoPoint getLocation(){
        return location;
    }

    public List<String> getDoctorNames() {
        return doctorNames;
    }

    public String getHospital_Img() {
        return hospital_Img;
    }

    public float getRating(){
        return rating;
    }

    // Setters (optional, in case you need to update data)
    public void setHospitalname(String hospitalname) {
        this.hospitalname = hospitalname;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

   public void setLocation(GeoPoint location){
        this.location=location;
   }

    public void setDoctorNames(List<String> doctorNames) {
        this.doctorNames = doctorNames;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}
