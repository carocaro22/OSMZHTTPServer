package com.example.osmzhttpserver.handlers;

import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

public class FileHandler {
    private static FileHandler instance;
    ErrorHandler error = ErrorHandler.getInstance();
    BufferHandler bufferHandler = BufferHandler.getInstance();

    private FileHandler() {
    }

    public static synchronized FileHandler getInstance() {
        if (instance == null) {
            instance = new FileHandler();
        }
        return instance;
    }

    public byte[] getResponse(String reqFile) {
        String sdPath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(sdPath + "/website" + reqFile);
        assert !reqFile.isEmpty();
        if (file.isFile()) {
            return handleFile(file);
        } else {
            return handleDirectory(file);
        }
    }

    byte[] handleDirectory(File file) {
        File[] files = file.listFiles();
        writeLine("HTTP/1.1 200 OK");
        writeLine("Content-Type: text/html");
        writeLine("");
        if (files == null) {
            writeLine(file.getAbsolutePath());
            writeLine("There is no file in this directory");
        } else {
            for (File someFile : files) {
                writeLine(someFile.getName());
                writeLine("<br>");
            }
        }
        return getBuffer();
    }

    private byte[] handleFile(File file) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(file.getAbsolutePath());
        if (extension.equals("json")) {
            return handleJson(file);
        } else {
            return readFile(file, extension);
        }
    }

    private byte[] readFile(File file, String extension) {
        byte[] err = null;
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        writeLine("HTTP/1.1 200 OK");
        writeLine("Content-Type: " + mimeType);
        String str2 = "Content-Length: " + file.length() + "\n";
        writeLine(str2);
        writeLine("");

        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            err = error.fileInputStreamNotCreated();
            System.exit(1);
        }

        byte[] buffer = new byte[(int) file.length()];
        try {
            while ((fileInputStream.read(buffer)) != -1) {
                writeByte(buffer);
            }
        } catch (IOException e) {
            err = error.fileCouldNotBeRead();
            System.exit(1);
        }

        try {
            fileInputStream.close();
        } catch (IOException e) {
            err = error.fileInputStreamNotClosed();
            System.exit(1);
        }
        if (err != null) {
            return err;
        } else {
            return getBuffer();
        }
    }

    byte[] handleJson(File file) {
        byte[] err = null;
        Log.d("FILE", "Sending json...");

        writeLine("HTTP/1.1 200 OK");
        writeLine("Content-Type: application/json");
        String str2 = "Content-Length: " + file.length();
        writeLine(str2);

        try {
            writeByte(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            err = error.jsonFileNotRead();
            System.exit(1);
        }

        if (err != null) {
            return err;
        } else {
            return getBuffer();
        }
    }

    private void writeByte(byte[] bytes) {
        bufferHandler.writeByte(bytes);
    }

    private void writeLine(String line) {
        bufferHandler.writeLine(line);
    }

    private byte[] getBuffer() {
        return bufferHandler.getBuffer();
    }
}
