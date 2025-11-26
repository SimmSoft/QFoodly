package com.example.qfoodly.data;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OpenFoodFactsClient {
    private static final String BASE_URL = "https://world.openfoodfacts.org/";
    private static Retrofit retrofit;
    private static OpenFoodFactsService service;

    public static OpenFoodFactsService getService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        if (service == null) {
            service = retrofit.create(OpenFoodFactsService.class);
        }

        return service;
    }
}