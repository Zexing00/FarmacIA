package com.example.farmacia.model;

public class Medicamento {
    private int id;
    private String nombre;
    private String prospecto;
    
    // Campos específicos del pastillero
    private String fechaCaducidad;
    private String dosisSemanal;

    public Medicamento(int id, String nombre, String prospecto) {
        this.id = id;
        this.nombre = nombre;
        this.prospecto = prospecto;
    }

    // Constructor completo
    public Medicamento(int id, String nombre, String prospecto, String fechaCaducidad, String dosisSemanal) {
        this.id = id;
        this.nombre = nombre;
        this.prospecto = prospecto;
        this.fechaCaducidad = fechaCaducidad;
        this.dosisSemanal = dosisSemanal;
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

    public String getFechaCaducidad() {
        return fechaCaducidad;
    }

    public void setFechaCaducidad(String fechaCaducidad) {
        this.fechaCaducidad = fechaCaducidad;
    }

    public String getDosisSemanal() {
        return dosisSemanal;
    }

    public void setDosisSemanal(String dosisSemanal) {
        this.dosisSemanal = dosisSemanal;
    }
}
