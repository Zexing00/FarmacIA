package com.example.farmacia.model.cima;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CimaResponse {

    @SerializedName("totalFilas")
    private int totalRows;

    @SerializedName("pagina")
    private int page;

    @SerializedName("tamanoPagina")
    private int pageSize;

    @SerializedName("resultados")
    private List<CimaMedication> results;

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public List<CimaMedication> getResults() {
        return results;
    }

    public void setResults(List<CimaMedication> results) {
        this.results = results;
    }
}
