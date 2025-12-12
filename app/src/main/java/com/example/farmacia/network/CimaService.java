package com.example.farmacia.network;

import com.example.farmacia.model.cima.CimaResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CimaService {
    @GET("medicamentos")
    Call<CimaResponse> buscarMedicamentos(@Query("nombre") String nombre);
}
