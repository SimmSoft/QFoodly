package com.example.qfoodly.ui.addproduct;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AddProductViewModel extends ViewModel {
    
    private final MutableLiveData<Boolean> hasUnsavedChanges = new MutableLiveData<>(false);
    
    public LiveData<Boolean> getHasUnsavedChanges() {
        return hasUnsavedChanges;
    }
    
    public void setHasUnsavedChanges(boolean hasChanges) {
        hasUnsavedChanges.setValue(hasChanges);
    }
    
    public void clearUnsavedChanges() {
        hasUnsavedChanges.setValue(false);
    }
}