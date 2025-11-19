package com.example.qfoodly.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImportExportManager {

    public static final String EXPORT_DIR = "Qfoodly";

    /**
     * Get the export directory in Downloads folder
     * Creates the directory if it doesn't exist
     */
    public static File getExportDirectory(Context context) throws Exception {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File exportDir = new File(downloadsDir, EXPORT_DIR);

        if (!exportDir.exists()) {
            if (!exportDir.mkdirs()) {
                throw new Exception("Failed to create export directory");
            }
        }

        return exportDir;
    }

    /**
     * Generate a timestamped filename for exports
     */
    public static String generateFileName(String prefix, String extension) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US);
        String timestamp = sdf.format(new Date());
        return prefix + "_" + timestamp + "." + extension;
    }

    /**
     * Create export file in the proper directory
     */
    public static File createExportFile(Context context, String prefix, String extension) throws Exception {
        File exportDir = getExportDirectory(context);
        String fileName = generateFileName(prefix, extension);
        return new File(exportDir, fileName);
    }
}