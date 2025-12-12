package com.example.farmacia.model.cima;

import com.google.gson.annotations.SerializedName;

public class CimaMedicamento {
    private String nregistro;
    private String nombre;
    private String labtitular;
    private String cpresc; // Condiciones de prescripción
    
    @SerializedName("docs")
    private java.util.List<CimaDocumento> documentos;

    public String getNregistro() {
        return nregistro;
    }

    public void setNregistro(String nregistro) {
        this.nregistro = nregistro;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getLabtitular() {
        return labtitular;
    }

    public void setLabtitular(String labtitular) {
        this.labtitular = labtitular;
    }

    public java.util.List<CimaDocumento> getDocumentos() {
        return documentos;
    }

    public void setDocumentos(java.util.List<CimaDocumento> documentos) {
        this.documentos = documentos;
    }

    // Método auxiliar para obtener la URL del prospecto si existe
    public String getUrlProspecto() {
        if (documentos != null) {
            for (CimaDocumento doc : documentos) {
                if (doc.getTipo() == 2 && doc.getUrl() != null) { // 2 suele ser prospecto
                    return doc.getUrl();
                }
            }
        }
        return null;
    }
}
