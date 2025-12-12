package com.example.farmacia.model.cima;

public class CimaDocumento {
    private int tipo;
    private String url;
    private String urlHtml;
    private boolean secc;
    private long fecha;

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
