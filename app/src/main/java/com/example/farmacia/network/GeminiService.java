package com.example.farmacia.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface GeminiService {

    // Endpoint REST generateContent:
    // POST https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent
    @POST("v1beta/models/{model}:generateContent")
    Call<GeminiResponse> generateContent(
            @Path("model") String model,
            @Body GeminiRequest body
    );
}
