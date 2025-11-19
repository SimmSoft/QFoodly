package com.example.qfoodly.ui.settings;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.qfoodly.data.Product;
import com.example.qfoodly.data.ProductDataSource;
import com.example.qfoodly.utils.CSVExporter;
import com.example.qfoodly.utils.FileReadWriter;
import com.example.qfoodly.utils.ImportExportManager;
import com.example.qfoodly.utils.JSONExporter;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class SettingsViewModel extends AndroidViewModel {

    private final MutableLiveData<String> mText;
    private final MutableLiveData<ImportExportResult> mImportExportResult;
    private final ProductDataSource dataSource;

    public static class ImportExportResult {
        public boolean success;
        public String message;
        public int processedCount;
        public int skippedCount;

        public ImportExportResult(boolean success, String message) {
            this.success = success;
            this.message = message;
            this.processedCount = 0;
            this.skippedCount = 0;
        }

        public ImportExportResult(boolean success, String message, int processedCount, int skippedCount) {
            this.success = success;
            this.message = message;
            this.processedCount = processedCount;
            this.skippedCount = skippedCount;
        }
    }

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        mText = new MutableLiveData<>();
        mText.setValue("SETTINGS");
        mImportExportResult = new MutableLiveData<>();
        dataSource = new ProductDataSource(application.getApplicationContext());
    }

    public LiveData<String> getText() {
        return mText;
    }

    public LiveData<ImportExportResult> getImportExportResult() {
        return mImportExportResult;
    }

    public void clearImportExportResult() {
        mImportExportResult.setValue(null);
    }

    public void exportProductsToCSV() {
        new Thread(() -> {
            try {
                dataSource.open();
                List<Product> products = dataSource.getAllProducts(ProductDataSource.SortOrder.DEFAULT, null);
                dataSource.close();

                if (products.isEmpty()) {
                    mImportExportResult.postValue(new ImportExportResult(false, "No products to export"));
                    return;
                }

                File exportFile = ImportExportManager.createExportFile(getApplication(), "products", "csv");
                CSVExporter.exportProductsToCSV(products, exportFile);

                String message = String.format("Products exported to: %s", exportFile.getName());
                mImportExportResult.postValue(new ImportExportResult(true, message));
            } catch (Exception e) {
                mImportExportResult.postValue(new ImportExportResult(false, "Export failed: " + e.getMessage()));
            }
        }).start();
    }

    public void exportProductsToJSON() {
        new Thread(() -> {
            try {
                dataSource.open();
                List<Product> products = dataSource.getAllProducts(ProductDataSource.SortOrder.DEFAULT, null);
                dataSource.close();

                if (products.isEmpty()) {
                    mImportExportResult.postValue(new ImportExportResult(false, "No products to export"));
                    return;
                }

                File exportFile = ImportExportManager.createExportFile(getApplication(), "products", "json");
                JSONExporter.exportProductsToJSON(products, exportFile);

                String message = String.format("Products exported to: %s", exportFile.getName());
                mImportExportResult.postValue(new ImportExportResult(true, message));
            } catch (Exception e) {
                mImportExportResult.postValue(new ImportExportResult(false, "Export failed: " + e.getMessage()));
            }
        }).start();
    }

    public void importProductsFromCSV(File csvFile) {
        new Thread(() -> {
            try {
                String content = FileReadWriter.readFile(csvFile);
                importProductsFromCSVContent(content);
            } catch (Exception e) {
                mImportExportResult.postValue(new ImportExportResult(false, "Import failed: " + e.getMessage()));
            }
        }).start();
    }

    public void importProductsFromCSVContent(String content) {
        new Thread(() -> {
            try {
                List<Product> products = CSVExporter.parseCSV(content);

                if (products.isEmpty()) {
                    mImportExportResult.postValue(new ImportExportResult(false, "No valid products found in CSV"));
                    return;
                }

                dataSource.open();
                int imported = 0;
                for (Product product : products) {
                    dataSource.upsertProduct(product);
                    imported++;
                }
                dataSource.close();

                int skipped = content.split("\n").length - imported - 1; // -1 for header
                mImportExportResult.postValue(new ImportExportResult(true, "Import completed", imported, Math.max(0, skipped)));
            } catch (Exception e) {
                mImportExportResult.postValue(new ImportExportResult(false, "Import failed: " + e.getMessage()));
            }
        }).start();
    }

    public void importProductsFromJSON(File jsonFile) {
        new Thread(() -> {
            try {
                String content = FileReadWriter.readFile(jsonFile);
                importProductsFromJSONContent(content);
            } catch (Exception e) {
                mImportExportResult.postValue(new ImportExportResult(false, "Import failed: " + e.getMessage()));
            }
        }).start();
    }

    public void importProductsFromJSONContent(String content) {
        new Thread(() -> {
            try {
                List<Product> products = JSONExporter.parseJSON(content);

                if (products.isEmpty()) {
                    mImportExportResult.postValue(new ImportExportResult(false, "No valid products found in JSON"));
                    return;
                }

                dataSource.open();
                int imported = 0;
                for (Product product : products) {
                    dataSource.upsertProduct(product);
                    imported++;
                }
                dataSource.close();

                mImportExportResult.postValue(new ImportExportResult(true, "Import completed", imported, 0));
            } catch (Exception e) {
                mImportExportResult.postValue(new ImportExportResult(false, "Import failed: " + e.getMessage()));
            }
        }).start();
    }

    public void importProductsAutoDetect(String content) {
        new Thread(() -> {
            try {
                String trimmed = content.trim();
                
                // Detect format: JSON starts with { or [
                if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
                    importProductsFromJSONContent(content);
                } else {
                    // Otherwise treat as CSV
                    importProductsFromCSVContent(content);
                }
            } catch (Exception e) {
                mImportExportResult.postValue(new ImportExportResult(false, "Import failed: " + e.getMessage()));
            }
        }).start();
    }
}