package com.example.qfoodly.utils;

import com.example.qfoodly.data.Product;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CSVExporter {

    private static final String CSV_HEADER = "id,name,price,expirationDate,category,description,store,purchaseDate,isUsed";

    public static void exportProductsToCSV(List<Product> products, File outputFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            StringBuilder csv = new StringBuilder();
            csv.append(CSV_HEADER).append("\n");

            for (Product product : products) {
                csv.append(escapeCSVField(String.valueOf(product.getId()))).append(",");
                csv.append(escapeCSVField(product.getName())).append(",");
                csv.append(escapeCSVField(String.valueOf(product.getPrice()))).append(",");
                csv.append(escapeCSVField(product.getExpirationDate())).append(",");
                csv.append(escapeCSVField(product.getCategory())).append(",");
                csv.append(escapeCSVField(product.getDescription())).append(",");
                csv.append(escapeCSVField(product.getStore())).append(",");
                csv.append(escapeCSVField(product.getPurchaseDate())).append(",");
                csv.append(product.isUsed() ? "1" : "0").append("\n");
            }

            fos.write(csv.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    private static String escapeCSVField(String field) {
        if (field == null) {
            return "";
        }

        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    public static List<Product> parseCSV(String csvContent) throws IllegalArgumentException {
        List<Product> products = new java.util.ArrayList<>();
        String[] lines = csvContent.split("\n");

        if (lines.length < 2) {
            throw new IllegalArgumentException("CSV file is empty or contains only header");
        }

        // Skip header (line 0)
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }

            try {
                String[] fields = parseCSVLine(line);
                if (fields.length != 9) {
                    continue; // Skip invalid rows
                }

                long id = Long.parseLong(fields[0]);
                String name = fields[1];
                double price = Double.parseDouble(fields[2]);
                String expirationDate = fields[3];
                String category = fields[4];
                String description = fields[5];
                String store = fields[6];
                String purchaseDate = fields[7];
                boolean isUsed = fields[8].equals("1");

                Product product = new Product(id, name, price, expirationDate, category, description, store, purchaseDate, isUsed);
                products.add(product);
            } catch (Exception e) {
                // Skip invalid row
                continue;
            }
        }

        return products;
    }

    private static String[] parseCSVLine(String line) {
        java.util.List<String> fields = new java.util.ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (insideQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    currentField.append('"');
                    i++; // Skip next quote
                } else {
                    insideQuotes = !insideQuotes;
                }
            } else if (c == ',' && !insideQuotes) {
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }

        fields.add(currentField.toString());
        return fields.toArray(new String[0]);
    }
}