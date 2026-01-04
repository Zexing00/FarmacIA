package com.example.farmacia.model.cima;

import com.google.gson.annotations.SerializedName;

public class CimaDocument {

    @SerializedName("tipo")
    private int type;

    @SerializedName("url")
    private String url;

    @SerializedName("urlHtml")
    private String urlHtml;

    @SerializedName("secc")
    private boolean isSectional;

    @SerializedName("fecha")
    private long date;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrlHtml() {
        return urlHtml;
    }

    public void setUrlHtml(String urlHtml) {
        this.urlHtml = urlHtml;
    }

    public boolean isSectional() {
        return isSectional;
    }

    public void setSectional(boolean sectional) {
        isSectional = sectional;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
