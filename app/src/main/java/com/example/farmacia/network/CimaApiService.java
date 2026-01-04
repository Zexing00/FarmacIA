package com.example.farmacia.network;

import com.example.farmacia.model.cima.CimaResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CimaApiService {
    @GET("medicamentos")
    Call<CimaResponse> searchMedicationsByName(@Query("nombre") String name);
}
