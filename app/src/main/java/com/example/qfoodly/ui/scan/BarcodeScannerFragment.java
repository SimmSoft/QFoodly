package com.example.qfoodly.ui.scan;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.qfoodly.R;
import com.google.android.material.button.MaterialButton;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BarcodeScannerFragment extends Fragment {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private PreviewView previewView;
    private MaterialButton toggleCameraBtn;
    private MaterialButton closeBtn;
    private BarcodeScanner barcodeScanner;
    private int currentCameraSelector = CameraSelector.LENS_FACING_BACK;
    private Camera camera;
    private ProcessCameraProvider cameraProvider;
    private boolean isScanning = true;
    private MediaPlayer mediaPlayer;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_barcode_scanner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        previewView = view.findViewById(R.id.preview_view);
        toggleCameraBtn = view.findViewById(R.id.toggle_camera_btn);
        closeBtn = view.findViewById(R.id.close_btn);

        // Konfiguracja ML Kit Barcode Scanner
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                        Barcode.FORMAT_EAN_13,
                        Barcode.FORMAT_EAN_8,
                        Barcode.FORMAT_UPC_A,
                        Barcode.FORMAT_UPC_E,
                        Barcode.FORMAT_CODE_128,
                        Barcode.FORMAT_CODE_39)
                .build();

        barcodeScanner = BarcodeScanning.getClient(options);

        toggleCameraBtn.setOnClickListener(v -> toggleCamera());
        closeBtn.setOnClickListener(v -> closeScanner());

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    @OptIn(markerClass = androidx.camera.core.ExperimentalGetImage.class)
    private void startCamera() {
        com.google.common.util.concurrent.ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                // Preview use case
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // Image analysis use case for barcode scanning
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), imageProxy -> {
                    if (!isScanning) {
                        imageProxy.close();
                        return;
                    }

                    try {
                        InputImage image = InputImage.fromMediaImage(
                                imageProxy.getImage(),
                                imageProxy.getImageInfo().getRotationDegrees()
                        );

                        barcodeScanner.process(image)
                                .addOnSuccessListener(barcodes -> {
                                    if (!barcodes.isEmpty() && isScanning) {
                                        isScanning = false;
                                        Barcode barcode = barcodes.get(0);
                                        String scannedValue = barcode.getRawValue();
                                        
                                        // >>> TUTAJ DODAJ DŹWIĘK <<<
                                        playScanSound();
                                        
                                        onBarcodeScannerSuccess(scannedValue);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    // Loguj błąd zamiast pokazywać toast
                                    Log.d("BarcodeScannerFragment", "Barcode scanning error: " + e.getMessage());
                                });
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        imageProxy.close();
                    }
                });

                // Bind to lifecycle
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(currentCameraSelector)
                        .build();

                cameraProvider.unbindAll();
                camera = cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageAnalysis
                );

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void toggleCamera() {
        currentCameraSelector = currentCameraSelector == CameraSelector.LENS_FACING_BACK
                ? CameraSelector.LENS_FACING_FRONT
                : CameraSelector.LENS_FACING_BACK;

        isScanning = true;
        startCamera();

        String cameraType = currentCameraSelector == CameraSelector.LENS_FACING_BACK
                ? "Back Camera"
                : "Front Camera";
        Toast.makeText(requireContext(), cameraType, Toast.LENGTH_SHORT).show();
    }

    private void playScanSound() {
        try {
            // >>> MIEJSCE NA CUSTOM DŹWIĘK <<<
            // Zamień R.raw.scan_sound na swoją ścieżkę do pliku audio
            // Plik powinien być w: app/src/main/res/raw/scan_sound.mp3 (lub .wav, .ogg)
            
            mediaPlayer = MediaPlayer.create(requireContext(), R.raw.scan_sound);
            if (mediaPlayer != null) {
                mediaPlayer.setOnCompletionListener(mp -> {
                    mp.release();
                    mediaPlayer = null;
                });
                mediaPlayer.start();
            }
        } catch (Exception e) {
            Log.e("BarcodeScannerFragment", "Error playing sound: " + e.getMessage());
        }
    }

    private void onBarcodeScannerSuccess(String scannedBarcode) {
        // Przejście do AddProductFragment z kodem
        Bundle bundle = new Bundle();
        bundle.putString("scanned_barcode", scannedBarcode);

        NavHostFragment.findNavController(this)
                .navigate(R.id.action_nav_barcode_scanner_to_nav_add_product, bundle);
    }

    private void closeScanner() {
        NavHostFragment.findNavController(this).popBackStack();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(requireContext(), "Camera permission is required", Toast.LENGTH_SHORT).show();
                closeScanner();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        barcodeScanner.close();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}