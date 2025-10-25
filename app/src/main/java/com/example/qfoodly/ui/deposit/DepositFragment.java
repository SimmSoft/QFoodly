package com.example.qfoodly.ui.deposit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.qfoodly.databinding.FragmentDepositBinding;

public class DepositFragment extends Fragment {

    private FragmentDepositBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DepositViewModel depositViewModel =
                new ViewModelProvider(this).get(DepositViewModel.class);

        binding = FragmentDepositBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textDeposit;
        depositViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}