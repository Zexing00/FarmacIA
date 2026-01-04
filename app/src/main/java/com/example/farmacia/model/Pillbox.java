package com.example.farmacia.model;

import java.util.ArrayList;
import java.util.List;

public class Pillbox {
    private int id;
    private int userId;
    private List<Medication> medicationList;

    public Pillbox(int id, int userId) {
        this.id = id;
        this.userId = userId;
        this.medicationList = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public List<Medication> getMedicationList() {
        return medicationList;
    }

    public void setMedicationList(List<Medication> medicationList) {
        this.medicationList = medicationList;
    }
    
    public void addMedication(Medication medication) {
        this.medicationList.add(medication);
    }
}
