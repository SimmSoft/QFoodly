package com.example.qfoodly.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.qfoodly.data.Product;
import com.example.qfoodly.data.ProductDataSource;

import java.util.List;

public class HomeViewModel extends AndroidViewModel {

    private final ProductDataSource dataSource;
    private final MutableLiveData<List<Product>> _products = new MutableLiveData<>();
    private final MutableLiveData<ProductDataSource.SortOrder> _sortOrder = new MutableLiveData<>(ProductDataSource.SortOrder.DEFAULT);
    private final MutableLiveData<String> _searchQuery = new MutableLiveData<>();
    private final MutableLiveData<ProductDataSource.StatusFilter> _statusFilter = new MutableLiveData<>(ProductDataSource.StatusFilter.ALL);
    public final LiveData<List<Product>> products = _products;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        dataSource = new ProductDataSource(application.getApplicationContext());
        dataSource.open();
        loadProducts();
    }

    public void setSortOrder(ProductDataSource.SortOrder sortOrder) {
        _sortOrder.setValue(sortOrder);
        loadProducts();
    }

    public void setSearchQuery(String query) {
        _searchQuery.setValue(query);
        loadProducts();
    }

    public void setStatusFilter(ProductDataSource.StatusFilter statusFilter) {
        _statusFilter.setValue(statusFilter);
        loadProducts();
    }

    private void loadProducts() {
        List<Product> productList = dataSource.getProductsByStatus(
                _sortOrder.getValue(),
                _searchQuery.getValue(),
                _statusFilter.getValue()
        );
        _products.postValue(productList);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        dataSource.close();
    }
}