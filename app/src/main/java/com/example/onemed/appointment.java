package com.example.onemed;


public class appointment implements HistoryItem {

    private boolean istaken;
    private String doctorName;
    private String date;
    private String specialty;
    private String time;
    private String hospital;
    private String userEmail;
    private String userId;
    private String id;

    public appointment() {}

    public appointment(String doctorName, String specialty, String date, String time, String hospital,
                       boolean istaken, String userEmail, String userId, String id) {
        this.doctorName = doctorName;
        this.specialty = specialty;
        this.date = date;
        this.time = time;
        this.hospital = hospital;
        this.istaken = istaken;
        this.userEmail = userEmail;
        this.userId = userId;
        this.id = id;
    }

    @Override
    public boolean isValid() {
        return istaken;
    }

    public String getDoctorName() { return doctorName; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getHospital() { return hospital; }
    public String getSpecialty() { return specialty; }
    public boolean getIstaken() { return istaken; }
    public String getUserEmail() { return userEmail; }
    public String getUserId() { return userId; }
    public String getId() { return id; }

    //setters
    public void setId(String id) { this.id = id; }

    public void setDate(String date){
        this.date=date;
    }

    public void setTime(String time){
        this.time=time;
    }
    public void setHospital(String hospital){
        this.hospital=hospital;
    }
    public boolean matchesQuery(String query) {
        query = query.toLowerCase();
        return (doctorName != null && doctorName.toLowerCase().contains(query)) ||
                (date != null && date.toLowerCase().contains(query)) ||
                (specialty != null && specialty.toLowerCase().contains(query)) ||
                (time != null && time.toLowerCase().contains(query)) ||
                (hospital != null && hospital.toLowerCase().contains(query));
    }
}

