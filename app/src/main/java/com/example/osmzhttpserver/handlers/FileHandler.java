package com.example.osmzhttpserver.handlers;

import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

public class FileHandler {
    void handleFile(File file) throws IOException {
        String extension = MimeTypeMap.getFileExtensionFromUrl(file.getAbsolutePath());
        if (extension.equals("json")) {
            Log.d("SERVER", "Sending json...");
            return "json";
            writeLineToWebsite("HTTP/1.1 200 OK");
            writeLineToWebsite("Content-Type: application/json");
            String str2 = "Content-Length: " + file.length() + "\n";
            writeLineToWebsite(str2);
            writeLineToWebsite("");
            byte[] bytes = Files.readAllBytes(file.toPath());
            for (byte aByte : bytes) {
                websiteOut.write(aByte);
            }
        } else {
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            writeLineToWebsite("HTTP/1.1 200 OK");
            writeLineToWebsite("Content-Type: " + mimeType);
            String str2 = "Content-Length: " + file.length() + "\n";
            writeLineToWebsite(str2);
            writeLineToWebsite("");

            try (FileInputStream fileInputStream = new FileInputStream(file);
                 OutputStream os = s.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }
        }
    }

}
