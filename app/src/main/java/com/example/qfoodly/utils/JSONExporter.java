package com.example.qfoodly.utils;

import com.example.qfoodly.data.Product;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class JSONExporter {

    public static void exportProductsToJSON(List<Product> products, File outputFile) throws IOException, JSONException {
        JSONArray jsonArray = new JSONArray();

        for (Product product : products) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", product.getId());
            jsonObject.put("name", product.getName());
            jsonObject.put("price", product.getPrice());
            jsonObject.put("expirationDate", product.getExpirationDate());
            jsonObject.put("category", product.getCategory());
            jsonObject.put("description", product.getDescription());
            jsonObject.put("store", product.getStore());
            jsonObject.put("purchaseDate", product.getPurchaseDate());
            jsonObject.put("isUsed", product.isUsed());

            jsonArray.put(jsonObject);
        }

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(jsonArray.toString(2).getBytes(StandardCharsets.UTF_8));
        }
    }

    public static List<Product> parseJSON(String jsonContent) throws JSONException {
        List<Product> products = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(jsonContent);

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                long id = jsonObject.getLong("id");
                String name = jsonObject.optString("name", "");
                double price = jsonObject.optDouble("price", 0.0);
                String expirationDate = jsonObject.optString("expirationDate", "");
                String category = jsonObject.optString("category", "");
                String description = jsonObject.optString("description", "");
                String store = jsonObject.optString("store", "");
                String purchaseDate = jsonObject.optString("purchaseDate", "");
                boolean isUsed = jsonObject.optBoolean("isUsed", false);

                Product product = new Product(id, name, price, expirationDate, category, description, store, purchaseDate, isUsed);
                products.add(product);
            } catch (JSONException e) {
                // Skip invalid entry
                continue;
            }
        }

        return products;
    }
}