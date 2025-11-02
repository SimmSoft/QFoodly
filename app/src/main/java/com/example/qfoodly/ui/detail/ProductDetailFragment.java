package com.example.qfoodly.ui.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.qfoodly.data.Product;
import com.example.qfoodly.databinding.FragmentProductDetailBinding;

import java.util.Locale;

public class ProductDetailFragment extends Fragment {

    private FragmentProductDetailBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProductDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            Product product = getArguments().getParcelable("product");
            if (product != null) {
                populateUI(product);
            }
        }
    }

    private void populateUI(Product product) {
        binding.detailProductName.setText(product.getName());
        binding.detailProductPrice.setText(String.format(Locale.getDefault(), "%.2f zł", product.getPrice()));
        binding.detailProductCategory.setText(product.getCategory());

        if (product.getDescription() != null && !product.getDescription().isEmpty()) {
            binding.detailDescriptionText.setText(product.getDescription());
            binding.detailDescriptionLabel.setVisibility(View.VISIBLE);
            binding.detailDescriptionText.setVisibility(View.VISIBLE);
        } else {
            binding.detailDescriptionLabel.setVisibility(View.GONE);
            binding.detailDescriptionText.setVisibility(View.GONE);
        }

        binding.detailExpirationDate.setText(product.getExpirationDate());
        binding.detailPurchaseDate.setText(product.getPurchaseDate());
        binding.detailStoreText.setText(product.getStore());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}