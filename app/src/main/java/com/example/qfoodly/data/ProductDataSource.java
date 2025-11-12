package com.example.qfoodly.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class ProductDataSource {

    public enum SortOrder {
        DEFAULT,
        PRICE_ASC,
        PRICE_DESC,
        NAME_ASC,
        NAME_DESC
    }

    public enum StatusFilter {
        ALL,
        ACTIVE,
        USED
    }

    private SQLiteDatabase database;
    private ProductDbHelper dbHelper;
    private String[] columns = {
            ProductDbHelper.COLUMN_ID,
            ProductDbHelper.COLUMN_NAME,
            ProductDbHelper.COLUMN_PRICE,
            ProductDbHelper.COLUMN_EXPIRATION_DATE,
            ProductDbHelper.COLUMN_CATEGORY,
            ProductDbHelper.COLUMN_DESCRIPTION,
            ProductDbHelper.COLUMN_STORE,
            ProductDbHelper.COLUMN_PURCHASE_DATE,
            ProductDbHelper.COLUMN_IS_USED
    };

    public ProductDataSource(Context context) {
        dbHelper = new ProductDbHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long createProduct(String name, double price, String expirationDate, String category, String description, String store, String purchaseDate) {
        ContentValues values = new ContentValues();
        values.put(ProductDbHelper.COLUMN_NAME, name);
        values.put(ProductDbHelper.COLUMN_PRICE, price);
        values.put(ProductDbHelper.COLUMN_EXPIRATION_DATE, expirationDate);
        values.put(ProductDbHelper.COLUMN_CATEGORY, category);
        values.put(ProductDbHelper.COLUMN_DESCRIPTION, description);
        values.put(ProductDbHelper.COLUMN_STORE, store);
        values.put(ProductDbHelper.COLUMN_PURCHASE_DATE, purchaseDate);

        return database.insert(ProductDbHelper.TABLE_PRODUCTS, null, values);
    }

    public List<Product> getAllProducts(SortOrder sortOrder, String query) {
        return getProductsByStatus(sortOrder, query, StatusFilter.ALL);
    }

    public List<Product> getProductsByStatus(SortOrder sortOrder, String query, StatusFilter statusFilter) {
        List<Product> products = new ArrayList<>();
        String orderBy;

        switch (sortOrder) {
            case PRICE_ASC:
                orderBy = ProductDbHelper.COLUMN_PRICE + " ASC";
                break;
            case PRICE_DESC:
                orderBy = ProductDbHelper.COLUMN_PRICE + " DESC";
                break;
            case NAME_ASC:
                orderBy = ProductDbHelper.COLUMN_NAME + " ASC";
                break;
            case NAME_DESC:
                orderBy = ProductDbHelper.COLUMN_NAME + " DESC";
                break;
            case DEFAULT:
            default:
                orderBy = ProductDbHelper.COLUMN_ID + " DESC";
                break;
        }

        String selection = null;
        String[] selectionArgs = null;

        if (statusFilter == StatusFilter.ACTIVE) {
            selection = ProductDbHelper.COLUMN_IS_USED + " = 0";
        } else if (statusFilter == StatusFilter.USED) {
            selection = ProductDbHelper.COLUMN_IS_USED + " = 1";
        }

        if (query != null && !query.isEmpty()) {
            String querySelection = ProductDbHelper.COLUMN_NAME + " LIKE ?";
            if (selection != null) {
                selection = selection + " AND " + querySelection;
                selectionArgs = new String[]{"%" + query + "%"};
            } else {
                selection = querySelection;
                selectionArgs = new String[]{"%" + query + "%"};
            }
        }

        Cursor cursor = database.query(ProductDbHelper.TABLE_PRODUCTS, columns, selection, selectionArgs, null, null, orderBy);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Product product = cursorToProduct(cursor);
            products.add(product);
            cursor.moveToNext();
        }

        cursor.close();
        return products;
    }

    public void updateProduct(long id, String name, double price, String expirationDate, String category, String description, String store, String purchaseDate) {
        ContentValues values = new ContentValues();
        values.put(ProductDbHelper.COLUMN_NAME, name);
        values.put(ProductDbHelper.COLUMN_PRICE, price);
        values.put(ProductDbHelper.COLUMN_EXPIRATION_DATE, expirationDate);
        values.put(ProductDbHelper.COLUMN_CATEGORY, category);
        values.put(ProductDbHelper.COLUMN_DESCRIPTION, description);
        values.put(ProductDbHelper.COLUMN_STORE, store);
        values.put(ProductDbHelper.COLUMN_PURCHASE_DATE, purchaseDate);

        database.update(ProductDbHelper.TABLE_PRODUCTS, values,
                ProductDbHelper.COLUMN_ID + " = " + id, null);
    }

    public void deleteProduct(long id) {
        database.delete(ProductDbHelper.TABLE_PRODUCTS,
                ProductDbHelper.COLUMN_ID + " = " + id, null);
    }

    public void markAsUsed(long id, boolean isUsed) {
        ContentValues values = new ContentValues();
        values.put(ProductDbHelper.COLUMN_IS_USED, isUsed ? 1 : 0);
        database.update(ProductDbHelper.TABLE_PRODUCTS, values,
                ProductDbHelper.COLUMN_ID + " = " + id, null);
    }

    private Product cursorToProduct(Cursor cursor) {
        long id = cursor.getLong(0);
        String name = cursor.getString(1);
        double price = cursor.getDouble(2);
        String expirationDate = cursor.getString(3);
        String category = cursor.getString(4);
        String description = cursor.getString(5);
        String store = cursor.getString(6);
        String purchaseDate = cursor.getString(7);
        boolean isUsed = cursor.getInt(8) != 0;

        return new Product(id, name, price, expirationDate, category, description, store, purchaseDate, isUsed);
    }
}
