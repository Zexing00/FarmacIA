package com.example.farmacia.model;

public class Medicamento {
    private int id;
    private String nombre;
    private String prospecto;

    public Medicamento(int id, String nombre, String prospecto) {
        this.id = id;
        this.nombre = nombre;
        this.prospecto = prospecto;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getProspecto() {
        return prospecto;
    }

    public void setProspecto(String prospecto) {
        this.prospecto = prospecto;
    }
}
