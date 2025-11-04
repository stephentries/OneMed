package com.example.onemed;

public class MedicalRecord {
    private String Recordtitle;
    private String Description;
    private String RecordDate;
    private boolean isVerified;
    private String verifiedBy;
    private boolean isOfficial;

    //empty firestore holder
    public MedicalRecord(){}

    public MedicalRecord(String Recordtitle,String Description,String Date,boolean isVerified,String verifiedBy,boolean isOfficial){
    this.Recordtitle=Recordtitle;
    this.Description=Description;
    this.RecordDate =Date;
    this.isVerified=isVerified;
    this.verifiedBy=verifiedBy;
    this.isOfficial=isOfficial;
    }

    public String getRecordtitle() {
        return Recordtitle;
    }
    public String getDescription(){
        return Description;
    }
    public String getRecordDate(){
        return RecordDate;
    }
    public boolean getVerified(){
        return isVerified;
    }
    public String getVerifiedBy(){
        return verifiedBy;
    }
    public boolean getIsOfficial(){
        return isOfficial;
    }
}


