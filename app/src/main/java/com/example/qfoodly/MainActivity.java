package com.example.qfoodly;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.example.qfoodly.databinding.ActivityMainBinding;
import com.example.qfoodly.ui.addproduct.AddProductViewModel;

import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private boolean isNavigating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();

        NavigationView navigationView = binding.navView;
        if (navigationView != null) {
            mAppBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.nav_home, R.id.nav_deposit, R.id.nav_stats, R.id.nav_settings)
                    .setOpenableLayout(binding.drawerLayout)
                    .build();
            NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
            NavigationUI.setupWithNavController(navigationView, navController);
        }

        BottomNavigationView bottomNavigationView = binding.appBarMain.contentMain.bottomNavView;
        if (bottomNavigationView != null) {
            mAppBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.nav_home, R.id.nav_deposit, R.id.nav_stats, R.id.nav_settings)
                    .build();
            NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
            NavigationUI.setupWithNavController(bottomNavigationView, navController);
            

        }

        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        ImageButton btnAddProduct = binding.appBarMain.btnAddProductToolbar;
        
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (toolbarTitle != null) {
                toolbarTitle.setText(destination.getLabel());
            }
            
            // Show Add Product button only on Home fragment
            if (btnAddProduct != null) {
                boolean showButton = destination.getId() == R.id.nav_home;
                btnAddProduct.setVisibility(showButton ? android.view.View.VISIBLE : android.view.View.GONE);
            }
            
            // Reset navigation flag when leaving barcode scanner
            if (destination.getId() != R.id.nav_barcode_scanner) {
                isNavigating = false;
            }
        });

        // Set up add product button click listener
        if (btnAddProduct != null) {
            btnAddProduct.setOnClickListener(view -> {
                navController.navigate(R.id.action_nav_home_to_nav_add_product);
            });
        }

        // Set up barcode scan button click listener
        binding.appBarMain.contentMain.fabScanBarcodeContainer.setOnClickListener(view -> {
            onScanBarcodeClicked();
        });
    }

    private void onScanBarcodeClicked() {
        // Jeśli nawigacja jest już w trakcie, zignoruj kliknięcie
        if (isNavigating) {
            return;
        }
        
        isNavigating = true;
        
        // Sprawdź czy AddProductFragment ma niezapisane zmiany
        AddProductViewModel viewModel = new ViewModelProvider(this).get(AddProductViewModel.class);
        
        if (viewModel.getHasUnsavedChanges().getValue() != null && 
            viewModel.getHasUnsavedChanges().getValue()) {
            // Ma niezapisane zmiany - pokaż dialog
            showScanWithUnsavedChangesDialog(viewModel);
        } else {
            // Brak zmian - normalnie idź do skanera
            navigateToScanner();
        }
    }

    private void showScanWithUnsavedChangesDialog(AddProductViewModel viewModel) {
        new AlertDialog.Builder(this)
            .setTitle("Niezapisane zmiany")
            .setMessage("Masz niezapisane dane w formularzu. Co chcesz zrobić?")
            .setPositiveButton("Porzuć i skanuj nowy", (dialog, which) -> {
                viewModel.clearUnsavedChanges();
                navigateToScanner();
            })
            .setNegativeButton("Wróć do edycji", (dialog, which) -> {
                isNavigating = false; // Reset flag since navigation didn't happen
                dialog.dismiss();
            })
            .setCancelable(false)
            .show();
    }

    private void navigateToScanner() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            // Pop the current fragment (AddProductFragment) from the back stack
            navController.popBackStack();
            // Navigate to scanner
            navController.navigate(R.id.nav_barcode_scanner);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = NavHostFragment.findNavController(getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main));
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
