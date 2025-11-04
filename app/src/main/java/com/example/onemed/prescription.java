package com.example.onemed;

public class prescription implements HistoryItem{
    private String id; // Firestore document ID
    private String medicineName;
    private String dosage;
    private String instruction;
    private int maxDoseCount;
    private int currentDoseCount;
    private boolean isFinished;

    // Required public no-arg constructor for Firestore
    public prescription() {
    }

    @Override
    public boolean isValid() {
        return isFinished;
    }
    public prescription(String id, String medicineName, String dosage, String instruction,
                        int maxDoseCount, int currentDoseCount,boolean isFinished) {
        this.id = id;
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.instruction = instruction;
        this.maxDoseCount = maxDoseCount;
        this.currentDoseCount = currentDoseCount;
        this.isFinished=isFinished;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public String getDosage() {
        return dosage;
    }

    public String getInstruction() {
        return instruction;
    }

    public int getDoseCount() {
        return maxDoseCount;
    }

    public int getCurrentDoseCount() {
        return currentDoseCount;
    }

    public void setCurrentDoseCount(int currentDoseCount) {
        this.currentDoseCount = currentDoseCount;
    }

    public boolean getisFinished(){
        return isFinished;
    }

    public void DoseCounter() {
        currentDoseCount++;
    }
    public boolean matchesQuery(String query) {
        query = query.toLowerCase();
        return (medicineName != null && medicineName.toLowerCase().contains(query)) ||
                (dosage != null && dosage.toLowerCase().contains(query)) ||
                (instruction != null && instruction.toLowerCase().contains(query)) ||
                String.valueOf(maxDoseCount).contains(query) ||
                String.valueOf(currentDoseCount).contains(query) ||
                (isFinished && "finished".contains(query)) ||
                (!isFinished && "ongoing".contains(query));
    }

}
