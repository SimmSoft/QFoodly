package com.example.qfoodly.data;

import com.google.gson.annotations.SerializedName;

public class OpenFoodFactsProduct {
    @SerializedName("product_name")
    private String productName;

    @SerializedName("categories")
    private String categories;

    @SerializedName("energy_kcal_100g")
    private Double energyKcal100g;

    @SerializedName("status")
    private String status;

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCategories() {
        return categories;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }

    public Double getEnergyKcal100g() {
        return energyKcal100g;
    }

    public void setEnergyKcal100g(Double energyKcal100g) {
        this.energyKcal100g = energyKcal100g;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}