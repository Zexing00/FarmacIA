package com.example.farmacia.model.cima;

import java.util.List;

public class CimaResponse {
    private int totalFilas;
    private int pagina;
    private int tamanoPagina;
    private List<CimaMedicamento> resultados;

    public int getTotalFilas() {
        return totalFilas;
    }

    public void setTotalFilas(int totalFilas) {
        this.totalFilas = totalFilas;
    }

    public List<CimaMedicamento> getResultados() {
        return resultados;
    }

    public void setResultados(List<CimaMedicamento> resultados) {
        this.resultados = resultados;
    }
}
