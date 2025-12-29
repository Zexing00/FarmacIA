package com.example.farmacia.network;

import com.example.farmacia.BuildConfig;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GeminiApiClient {

    private static final String BASE_URL = "https://generativelanguage.googleapis.com/";
    private static Retrofit retrofit = null;

    public static GeminiService getGeminiService() {
        if (retrofit == null) {

            OkHttpClient client = new OkHttpClient.Builder()
                    .protocols(Arrays.asList(Protocol.HTTP_1_1))
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request newReq = original.newBuilder()
                                .addHeader("x-goog-api-key", BuildConfig.GEMINI_API_KEY)
                                .addHeader("Content-Type", "application/json")
                                .build();
                        return chain.proceed(newReq);
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(GeminiService.class);
    }
}
