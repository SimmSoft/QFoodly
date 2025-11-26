package com.example.qfoodly.ui.addproduct;

import android.app.DatePickerDialog;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.qfoodly.R;
import com.example.qfoodly.data.Product;
import com.example.qfoodly.data.ProductDataSource;
import com.example.qfoodly.data.OpenFoodFactsClient;
import com.example.qfoodly.data.OpenFoodFactsResponse;
import com.example.qfoodly.databinding.FragmentAddProductBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddProductFragment extends Fragment {

    private FragmentAddProductBinding binding;
    private ProductDataSource dataSource;
    private Product editingProduct = null;
    private boolean isEditMode = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddProductBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        dataSource = new ProductDataSource(getContext());

        if (getArguments() != null) {
            editingProduct = getArguments().getParcelable("product");
            isEditMode = getArguments().getBoolean("isEdit", false);
            
            if (editingProduct != null && isEditMode) {
                populateFormWithProduct(editingProduct);
                binding.saveButton.setText("Update");
            }
            
            // Obsługa skanowanego kodu kreskowego
            String scannedBarcode = getArguments().getString("scanned_barcode");
            if (scannedBarcode != null && !scannedBarcode.isEmpty()) {
                // Pobierz dane z Open Food Facts API
                fetchProductFromAPI(scannedBarcode);
            }
        }

        setupDatePickers();
        binding.saveButton.setOnClickListener(v -> saveProduct());

        return root;
    }

    private void fetchProductFromAPI(String barcode) {
        // Pokaż loading (dezaktywuj save button)
        binding.saveButton.setEnabled(false);
        binding.saveButton.setAlpha(0.5f);
        
        // Wykonaj API call
        OpenFoodFactsClient.getService().getProduct(barcode).enqueue(new Callback<OpenFoodFactsResponse>() {
            @Override
            public void onResponse(Call<OpenFoodFactsResponse> call, Response<OpenFoodFactsResponse> response) {
                binding.saveButton.setEnabled(true);
                binding.saveButton.setAlpha(1.0f);
                
                if (response.isSuccessful() && response.body() != null) {
                    OpenFoodFactsResponse apiResponse = response.body();
                    
                    // Sprawdź czy produkt został znaleziony
                    if (apiResponse.getStatus() == 1 && apiResponse.getProduct() != null) {
                        populateFormFromAPI(apiResponse.getProduct());
                        Toast.makeText(getContext(), "Produkt znaleziony! ✓", Toast.LENGTH_SHORT).show();
                    } else {
                        // Produkt nie znaleziony - wstaw tylko barcode
                        binding.productNameEditText.setText(barcode);
                        Toast.makeText(getContext(), "Produkt nie znaleziony w bazie. Uzupełnij ręcznie.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    binding.productNameEditText.setText(barcode);
                    Toast.makeText(getContext(), "Błąd połączenia z API", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OpenFoodFactsResponse> call, Throwable t) {
                binding.saveButton.setEnabled(true);
                binding.saveButton.setAlpha(1.0f);
                
                Log.e("OpenFoodFacts", "API Error: " + t.getMessage());
                binding.productNameEditText.setText(barcode);
                Toast.makeText(getContext(), "Błąd pobierania danych", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateFormFromAPI(com.example.qfoodly.data.OpenFoodFactsProduct apiProduct) {
        // Wstaw nazwę produktu
        if (apiProduct.getProductName() != null && !apiProduct.getProductName().isEmpty()) {
            binding.productNameEditText.setText(apiProduct.getProductName());
        }
        
        // Wstaw kategorię
        if (apiProduct.getCategories() != null && !apiProduct.getCategories().isEmpty()) {
            // Weź pierwszą kategorię (może być rozdzielona przecinkami)
            String[] categories = apiProduct.getCategories().split(",");
            String mainCategory = categories[0].trim();
            binding.categoryEditText.setText(mainCategory);
        }
        
        // Wstaw kalorie do opisu (jeśli dostępne)
        if (apiProduct.getEnergyKcal100g() != null) {
            String description = "Kalorii na 100g: " + apiProduct.getEnergyKcal100g() + " kcal";
            binding.descriptionEditText.setText(description);
        }
    }

    private void populateFormWithProduct(Product product) {
        binding.productNameEditText.setText(product.getName());
        binding.priceEditText.setText(String.valueOf(product.getPrice()));
        binding.expirationDateEditText.setText(product.getExpirationDate());
        binding.categoryEditText.setText(product.getCategory());
        binding.descriptionEditText.setText(product.getDescription());
        binding.storeEditText.setText(product.getStore());
        binding.purchaseDateEditText.setText(product.getPurchaseDate());
    }

    private void setupDatePickers() {
        binding.expirationDateLayout.setEndIconOnClickListener(v -> 
            showDatePicker(binding.expirationDateEditText));

        binding.purchaseDateLayout.setEndIconOnClickListener(v -> 
            showDatePicker(binding.purchaseDateEditText));

        binding.expirationDateEditText.setOnClickListener(v -> 
            showDatePicker(binding.expirationDateEditText));

        binding.purchaseDateEditText.setOnClickListener(v -> 
            showDatePicker(binding.purchaseDateEditText));
    }

    private void showDatePicker(View dateEditText) {
        Calendar calendar = Calendar.getInstance();
        String currentText = binding.expirationDateEditText.getText().toString();
        
        if (dateEditText == binding.purchaseDateEditText) {
            currentText = binding.purchaseDateEditText.getText().toString();
        }

        if (!currentText.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = sdf.parse(currentText);
                calendar.setTime(date);
            } catch (Exception e) {
                // Use current date if parsing fails
            }
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", 
                            year, month + 1, dayOfMonth);
                    if (dateEditText == binding.expirationDateEditText) {
                        binding.expirationDateEditText.setText(selectedDate);
                        binding.expirationDateLayout.setError(null);
                    } else {
                        binding.purchaseDateEditText.setText(selectedDate);
                        binding.purchaseDateLayout.setError(null);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
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
        clearErrors();

        String name = binding.productNameEditText.getText().toString().trim();
        String priceStr = binding.priceEditText.getText().toString().trim();
        String expirationDate = binding.expirationDateEditText.getText().toString().trim();
        String category = binding.categoryEditText.getText().toString().trim();
        String description = binding.descriptionEditText.getText().toString().trim();
        String store = binding.storeEditText.getText().toString().trim();
        String purchaseDate = binding.purchaseDateEditText.getText().toString().trim();

        boolean isValid = true;

        if (TextUtils.isEmpty(name)) {
            binding.productNameLayout.setError("Product name is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(priceStr)) {
            binding.priceLayout.setError("Price is required");
            isValid = false;
        } else {
            try {
                double price = Double.parseDouble(priceStr);
                if (price <= 0) {
                    binding.priceLayout.setError("Price must be greater than 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                binding.priceLayout.setError("Invalid price format");
                isValid = false;
            }
        }

        if (TextUtils.isEmpty(expirationDate)) {
            binding.expirationDateLayout.setError("Expiration date is required");
            isValid = false;
        } else {
            if (!isValidExpirationDate(expirationDate)) {
                binding.expirationDateLayout.setError("Expiration date cannot be earlier than today");
                isValid = false;
            }
        }

        if (TextUtils.isEmpty(category)) {
            binding.categoryLayout.setError("Category is required");
            isValid = false;
        }

        if (!isValid) {
            Toast.makeText(getContext(), "Please correct the errors", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceStr);

        if (isEditMode && editingProduct != null) {
            dataSource.updateProduct(editingProduct.getId(), name, price, expirationDate, category, description, store, purchaseDate);
            Toast.makeText(getContext(), "Product updated successfully!", Toast.LENGTH_SHORT).show();
        } else {
            long productId = dataSource.createProduct(name, price, expirationDate, category, description, store, purchaseDate);
            if (productId != -1) {
                Toast.makeText(getContext(), "Product saved successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Error saving product", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        playConfirmationSound();
        NavHostFragment.findNavController(this).navigate(R.id.action_nav_add_product_to_nav_home);
    }

    private boolean isValidExpirationDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date expirationDate = sdf.parse(dateStr);
            Date today = new Date();
            
            Calendar calToday = Calendar.getInstance();
            calToday.setTime(today);
            calToday.set(Calendar.HOUR_OF_DAY, 0);
            calToday.set(Calendar.MINUTE, 0);
            calToday.set(Calendar.SECOND, 0);
            calToday.set(Calendar.MILLISECOND, 0);

            Calendar calExpiration = Calendar.getInstance();
            calExpiration.setTime(expirationDate);
            calExpiration.set(Calendar.HOUR_OF_DAY, 0);
            calExpiration.set(Calendar.MINUTE, 0);
            calExpiration.set(Calendar.SECOND, 0);
            calExpiration.set(Calendar.MILLISECOND, 0);

            return calExpiration.getTimeInMillis() >= calToday.getTimeInMillis();
        } catch (Exception e) {
            return false;
        }
    }

    private void playConfirmationSound() {
        try {
            MediaPlayer mediaPlayer = MediaPlayer.create(getContext(), R.raw.confirm_sound);
            if (mediaPlayer != null) {
                mediaPlayer.setOnCompletionListener(MediaPlayer::release);
                mediaPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearErrors() {
        binding.productNameLayout.setError(null);
        binding.priceLayout.setError(null);
        binding.expirationDateLayout.setError(null);
        binding.categoryLayout.setError(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
