package com.example.qfoodly.utils;

import android.content.ContentResolver;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class FileReadWriter {

    /**
     * Read file content as UTF-8 string
     */
    public static String readFile(File file) throws IOException {
        byte[] encoded = new byte[(int) file.length()];
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            fileInputStream.read(encoded);
        }
        return new String(encoded, StandardCharsets.UTF_8);
    }

    /**
     * Read file content from URI using ContentResolver
     */
    public static String readFileFromUri(ContentResolver contentResolver, Uri uri) throws IOException {
        try (InputStream inputStream = contentResolver.openInputStream(uri)) {
            if (inputStream == null) {
                throw new IOException("Cannot open file");
            }

            byte[] buffer = new byte[1024];
            int bytesRead;
            java.io.ByteArrayOutputStream result = new java.io.ByteArrayOutputStream();

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, bytesRead);
            }

            return result.toString(StandardCharsets.UTF_8.name());
        }
    }
}