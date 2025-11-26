package com.example.qfoodly.ui.scan;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Rect;
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
import androidx.camera.core.ImageProxy;
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
    private ScanningOverlayView scanningOverlay;
    private long scanStartTime = 0;
    private int frameCount = 0;

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
        scanningOverlay = view.findViewById(R.id.scanning_overlay);

        // Konfiguracja ML Kit Barcode Scanner - ZOPTYMALIZOWANA dla szybkości
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                        Barcode.FORMAT_EAN_13,
                        Barcode.FORMAT_EAN_8)
                .build();

        barcodeScanner = BarcodeScanning.getClient(options);

        toggleCameraBtn.setOnClickListener(v -> toggleCamera());
        closeBtn.setOnClickListener(v -> closeScanner());
        
        scanStartTime = System.currentTimeMillis();
        Log.d("BarcodeScanner", "=== SKANOWANIE ROZPOCZĘTE ===");

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

                // Image analysis use case for barcode scanning - ZOPTYMALIZOWANA dla SZYBKOŚCI
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetResolution(new android.util.Size(480, 640))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                        .build();

                imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), imageProxy -> {
                    frameCount++;
                    if (frameCount % 30 == 0) {
                        Log.d("BarcodeScanner", "Frame: " + frameCount + " | Czas: " + (System.currentTimeMillis() - scanStartTime) + "ms");
                    }
                    
                    if (!isScanning) {
                        imageProxy.close();
                        return;
                    }

                    try {
                        InputImage image = InputImage.fromMediaImage(
                                imageProxy.getImage(),
                                imageProxy.getImageInfo().getRotationDegrees()
                        );

                        // Przetwarzaj tylko obraz - ML Kit automatycznie fokusuje na środku
                        barcodeScanner.process(image)
                                .addOnSuccessListener(barcodes -> {
                                    if (!barcodes.isEmpty() && isScanning) {
                                        isScanning = false;
                                        Barcode barcode = barcodes.get(0);
                                        String scannedValue = barcode.getRawValue();
                                        long timeElapsed = System.currentTimeMillis() - scanStartTime;
                                        
                                        Log.d("BarcodeScanner", "✓ BARCODE ZNALEZIONY! Czas: " + timeElapsed + "ms | Frames: " + frameCount);
                                        Log.d("BarcodeScanner", "Wartość: " + scannedValue);
                                        
                                        Toast.makeText(requireContext(), "Barcode: " + scannedValue + " (" + timeElapsed + "ms)", Toast.LENGTH_LONG).show();
                                        playScanSound();
                                        onBarcodeScannerSuccess(scannedValue);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.d("BarcodeScannerFragment", "Barcode scanning error: " + e.getMessage());
                                })
                                .addOnCompleteListener(task -> {
                                    imageProxy.close();
                                });
                    } catch (Exception e) {
                        e.printStackTrace();
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

    private Rect getScanRectFromImage(ImageProxy imageProxy) {
        if (scanningOverlay == null) return null;

        // Rozdzielczość Preview
        int previewWidth = imageProxy.getWidth();
        int previewHeight = imageProxy.getHeight();

        // Wymiary overlay (ekran)
        int screenWidth = scanningOverlay.getWidth();
        int screenHeight = scanningOverlay.getHeight();

        if (screenWidth <= 0 || screenHeight <= 0) return null;

        // Obszar skanowania (ze overlay)
        Rect overlayRect = scanningOverlay.getScanRect();

        // Przelicz współrzędne z ekranu na obraz
        float scaleX = (float) previewWidth / screenWidth;
        float scaleY = (float) previewHeight / screenHeight;

        int imageLeft = (int) (overlayRect.left * scaleX);
        int imageTop = (int) (overlayRect.top * scaleY);
        int imageRight = (int) (overlayRect.right * scaleX);
        int imageBottom = (int) (overlayRect.bottom * scaleY);

        return new Rect(imageLeft, imageTop, imageRight, imageBottom);
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