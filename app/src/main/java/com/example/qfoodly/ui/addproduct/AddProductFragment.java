package com.example.qfoodly.ui.addproduct;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.qfoodly.R;
import com.example.qfoodly.data.ProductDataSource;
import com.example.qfoodly.databinding.FragmentAddProductBinding;

public class AddProductFragment extends Fragment {

    private FragmentAddProductBinding binding;
    private ProductDataSource dataSource;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddProductBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        dataSource = new ProductDataSource(getContext());

        binding.saveButton.setOnClickListener(v -> saveProduct());

        return root;
    }

    @Override
    public void onResume() {
        dataSource.open();
        super.onResume();
    }

    @Override
    public void onPause() {
        dataSource.close();
        super.onPause();
    }

    private void saveProduct() {
        String name = binding.productNameEditText.getText().toString().trim();
        String priceStr = binding.priceEditText.getText().toString().trim();
        String expirationDate = binding.expirationDateEditText.getText().toString().trim();
        String category = binding.categoryEditText.getText().toString().trim();
        String description = binding.descriptionEditText.getText().toString().trim();
        String store = binding.storeEditText.getText().toString().trim();
        String purchaseDate = binding.purchaseDateEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceStr) || TextUtils.isEmpty(expirationDate) || TextUtils.isEmpty(category)) {
            Toast.makeText(getContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = 0;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid price format", Toast.LENGTH_SHORT).show();
            return;
        }

        long productId = dataSource.createProduct(name, price, expirationDate, category, description, store, purchaseDate);

        if (productId != -1) {
            Toast.makeText(getContext(), "Product saved successfully!", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).navigate(R.id.action_nav_add_product_to_nav_home);
        } else {
            Toast.makeText(getContext(), "Error saving product", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
