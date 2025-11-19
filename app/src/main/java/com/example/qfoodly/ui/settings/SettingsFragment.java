package com.example.qfoodly.ui.settings;

import android.content.ContentResolver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.qfoodly.databinding.FragmentSettingsBinding;
import com.example.qfoodly.utils.FileReadWriter;

import java.io.File;
import java.io.InputStream;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SettingsViewModel settingsViewModel;
    private ActivityResultLauncher<String> importLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register file picker launcher - auto-detects format
        importLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                try {
                    ContentResolver contentResolver = getContext().getContentResolver();
                    String content = FileReadWriter.readFileFromUri(contentResolver, uri);
                    settingsViewModel.importProductsAutoDetect(content);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Set up button click listeners
        binding.btnExport.setOnClickListener(v -> showExportMenu());
        binding.btnImport.setOnClickListener(v -> showImportMenu());

        // Observe import/export results (only once per view creation)
        if (settingsViewModel.getImportExportResult().getValue() != null) {
            settingsViewModel.clearImportExportResult();
        }
        
        settingsViewModel.getImportExportResult().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                if (result.success) {
                    if (result.processedCount > 0) {
                        String message = String.format("Success! Processed: %d, Skipped: %d", 
                            result.processedCount, result.skippedCount);
                        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), result.message, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Error: " + result.message, Toast.LENGTH_LONG).show();
                }
                // Clear the result after displaying to prevent showing it again
                settingsViewModel.clearImportExportResult();
            }
        });

        return root;
    }

    private void showExportMenu() {
        String[] formats = {"CSV", "JSON"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Export Format")
                .setItems(formats, (dialog, which) -> {
                    if (which == 0) {
                        settingsViewModel.exportProductsToCSV();
                        Toast.makeText(getContext(), "Exporting to CSV...", Toast.LENGTH_SHORT).show();
                    } else {
                        settingsViewModel.exportProductsToJSON();
                        Toast.makeText(getContext(), "Exporting to JSON...", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showImportMenu() {
        importLauncher.launch("*/*");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}