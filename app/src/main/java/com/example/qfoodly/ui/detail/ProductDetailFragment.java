package com.example.qfoodly.ui.detail;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.qfoodly.R;
import com.example.qfoodly.data.Product;
import com.example.qfoodly.data.ProductDataSource;
import com.example.qfoodly.databinding.FragmentProductDetailBinding;

import java.util.Locale;

public class ProductDetailFragment extends Fragment {

    private FragmentProductDetailBinding binding;
    private Product currentProduct;
    private ProductDataSource dataSource;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProductDetailBinding.inflate(inflater, container, false);
        dataSource = new ProductDataSource(requireContext());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            Product product = getArguments().getParcelable("product");
            if (product != null) {
                currentProduct = product;
                populateUI(product);
                setupButtonListeners();
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

    private void setupButtonListeners() {
        binding.btnEdit.setOnClickListener(v -> editProduct());
        binding.btnDelete.setOnClickListener(v -> deleteProduct());
    }

    private void editProduct() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("product", currentProduct);
        bundle.putBoolean("isEdit", true);
        NavHostFragment.findNavController(ProductDetailFragment.this)
                .navigate(R.id.action_productDetailFragment_to_nav_add_product, bundle);
    }

    private void deleteProduct() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this product?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dataSource.open();
                    dataSource.deleteProduct(currentProduct.getId());
                    dataSource.close();
                    Toast.makeText(requireContext(), "Product deleted", Toast.LENGTH_SHORT).show();
                    playConfirmationSound();
                    NavHostFragment.findNavController(ProductDetailFragment.this).navigateUp();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void playConfirmationSound() {
        try {
            MediaPlayer mediaPlayer = MediaPlayer.create(requireContext(), R.raw.confirm_sound);
            if (mediaPlayer != null) {
                mediaPlayer.setOnCompletionListener(MediaPlayer::release);
                mediaPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}