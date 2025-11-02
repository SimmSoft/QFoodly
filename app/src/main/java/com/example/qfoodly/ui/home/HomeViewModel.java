package com.example.qfoodly.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<List<String>> mTexts;

    public HomeViewModel() {
        mTexts = new MutableLiveData<>();

    }

    public LiveData<List<String>> getTexts() {
        return mTexts;
    }
}