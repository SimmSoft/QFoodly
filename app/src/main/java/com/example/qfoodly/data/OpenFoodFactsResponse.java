package com.example.qfoodly.data;

import com.google.gson.annotations.SerializedName;

public class OpenFoodFactsResponse {
    @SerializedName("product")
    private OpenFoodFactsProduct product;

    @SerializedName("status")
    private int status;

    public OpenFoodFactsProduct getProduct() {
        return product;
    }

    public void setProduct(OpenFoodFactsProduct product) {
        this.product = product;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}