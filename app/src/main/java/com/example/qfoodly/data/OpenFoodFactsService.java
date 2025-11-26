package com.example.qfoodly.data;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface OpenFoodFactsService {
    @GET("api/v2/product/{barcode}.json")
    Call<OpenFoodFactsResponse> getProduct(@Path("barcode") String barcode);
}