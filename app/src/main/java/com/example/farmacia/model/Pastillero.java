package com.example.farmacia.model;

import java.util.ArrayList;
import java.util.List;

public class Pastillero {
    private int id;
    private int usuarioId;
    private List<Medicamento> listaMedicamentos;

    public Pastillero(int id, int usuarioId) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.listaMedicamentos = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public List<Medicamento> getListaMedicamentos() {
        return listaMedicamentos;
    }

    public void setListaMedicamentos(List<Medicamento> listaMedicamentos) {
        this.listaMedicamentos = listaMedicamentos;
    }
    
    public void agregarMedicamento(Medicamento medicamento) {
        this.listaMedicamentos.add(medicamento);
    }
}
