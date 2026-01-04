package com.example.farmacia.model.cima;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CimaMedication {

    @SerializedName("nregistro")
    private String registryNumber;

    @SerializedName("nombre")
    private String name;

    @SerializedName("labtitular")
    private String laboratory;

    @SerializedName("cpresc")
    private String prescriptionConditions;

    @SerializedName("docs")
    private List<CimaDocument> documents;

    public String getRegistryNumber() {
        return registryNumber;
    }

    public void setRegistryNumber(String registryNumber) {
        this.registryNumber = registryNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLaboratory() {
        return laboratory;
    }

    public void setLaboratory(String laboratory) {
        this.laboratory = laboratory;
    }

    public String getPrescriptionConditions() {
        return prescriptionConditions;
    }

    public void setPrescriptionConditions(String prescriptionConditions) {
        this.prescriptionConditions = prescriptionConditions;
    }

    public List<CimaDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(List<CimaDocument> documents) {
        this.documents = documents;
    }

    // Helper method to get the leaflet URL if it exists
    public String getLeafletUrl() {
        if (documents != null) {
            for (CimaDocument doc : documents) {
                if (doc.getType() == 2 && doc.getUrl() != null) { // Type 2 is usually the leaflet
                    return doc.getUrl();
                }
            }
        }
        return null;
    }
}
