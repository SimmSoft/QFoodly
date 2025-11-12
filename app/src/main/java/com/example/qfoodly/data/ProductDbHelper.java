package com.example.qfoodly.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ProductDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "QFOODLY.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_PRODUCTS = "PRODUCT_LIST";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_EXPIRATION_DATE = "expiration_date";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_STORE = "store";
    public static final String COLUMN_PURCHASE_DATE = "purchase_date";
    public static final String COLUMN_IS_USED = "is_used";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_PRODUCTS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT, " +
                    COLUMN_PRICE + " REAL, " +
                    COLUMN_EXPIRATION_DATE + " TEXT, " +
                    COLUMN_CATEGORY + " TEXT, " +
                    COLUMN_DESCRIPTION + " TEXT, " +
                    COLUMN_STORE + " TEXT, " +
                    COLUMN_PURCHASE_DATE + " TEXT, " +
                    COLUMN_IS_USED + " INTEGER DEFAULT 0" +
                    ");";

    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_PRODUCTS + " ADD COLUMN " + COLUMN_IS_USED + " INTEGER DEFAULT 0");
        }
    }
}
