package com.example.farmacia.model;

public class Medication {
    private int id;
    private String name;
    private String leaflet;
    private String expiryDate;
    private String weeklyDose;

    public Medication(int id, String name, String leaflet) {
        this.id = id;
        this.name = name;
        this.leaflet = leaflet;
    }

    public Medication(int id, String name, String leaflet, String expiryDate, String weeklyDose) {
        this.id = id;
        this.name = name;
        this.leaflet = leaflet;
        this.expiryDate = expiryDate;
        this.weeklyDose = weeklyDose;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLeaflet() {
        return leaflet;
    }

    public void setLeaflet(String leaflet) {
        this.leaflet = leaflet;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getWeeklyDose() {
        return weeklyDose;
    }

    public void setWeeklyDose(String weeklyDose) {
        this.weeklyDose = weeklyDose;
    }
}
