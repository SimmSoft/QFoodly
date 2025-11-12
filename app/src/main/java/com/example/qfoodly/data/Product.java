package com.example.qfoodly.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Product implements Parcelable {
    private long id;
    private String name;
    private double price;
    private String expirationDate;
    private String category;
    private String description;
    private String store;
    private String purchaseDate;
    private boolean isUsed;

    public Product(long id, String name, double price, String expirationDate, String category, String description, String store, String purchaseDate) {
        this(id, name, price, expirationDate, category, description, store, purchaseDate, false);
    }

    public Product(long id, String name, double price, String expirationDate, String category, String description, String store, String purchaseDate, boolean isUsed) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.expirationDate = expirationDate;
        this.category = category;
        this.description = description;
        this.store = store;
        this.purchaseDate = purchaseDate;
        this.isUsed = isUsed;
    }

    protected Product(Parcel in) {
        id = in.readLong();
        name = in.readString();
        price = in.readDouble();
        expirationDate = in.readString();
        category = in.readString();
        description = in.readString();
        store = in.readString();
        purchaseDate = in.readString();
        isUsed = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeDouble(price);
        dest.writeString(expirationDate);
        dest.writeString(category);
        dest.writeString(description);
        dest.writeString(store);
        dest.writeString(purchaseDate);
        dest.writeByte((byte) (isUsed ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public String getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(String purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public boolean isUsed() {
        return isUsed;
    }

    public void setUsed(boolean used) {
        isUsed = used;
    }
}
