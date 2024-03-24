package com.example.osmzhttpserver.handlers;

import android.util.Log;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class ErrorHandler {
    private static ErrorHandler instance;
    private ErrorHandler() { }
    public static synchronized ErrorHandler getInstance() {
        if (instance == null) {
            instance = new ErrorHandler();
        }
        return instance;
    }

    BufferHandler bufferHandler = BufferHandler.getInstance();

    // proccess errors
    byte[] processRead(IOException e) {
        Log.d("ProcessHandler", "Could not start Process");
        writeLine("HTTP/1.1 404 OK");
        writeLine("Content-Type: text/html");
        writeLine("");
        writeLine("<html>");

        Log.d("ProcessHandler", "ServerThread(" + e.getStackTrace()[1].getLineNumber() + "): " + e.getMessage());
        StackTraceElement[] trace = e.getStackTrace();
        for (int i = 0; i < trace.length; i++) {
            writeLine(Arrays.toString(trace));
            Log.d("ProccessHandler", "Catch: " + Arrays.toString(trace));
        }
        writeLine("</html>");
        return getBuffer();
    }

    byte[] processStart(IOException e) {
        Log.d("ProcessHandler", "Could not start Process", e);
        Log.d("ProccessHandler", Objects.requireNonNull(e.getMessage()));
        writeLine("HTTP/1.1 404 OK");
        writeLine("Content-Type: text/html");
        writeLine("");
        writeLine("<html><h1>Could not Start Process</h1></html>");
        return getBuffer();
    }

    byte[] process(String p_error_str) {
        Log.d("ProcessHandler", "error: " + p_error_str);
        writeLine("HTTP/1.1 404 OK");
        writeLine("Content-Type: text/html");
        writeLine("");
        writeLine("<html><h1>Process Error!</h1>");
        writeLine("error: " + p_error_str + "</html>");
        return getBuffer();
    }

    byte[] couldNotReadProccessAnswer() {
        Log.d("ProcessHandler", "Could not read Process Answer");
        writeLine("HTTP/1.1 404 OK");
        writeLine("Content-Type: text/html");
        writeLine("");
        writeLine("<html><h1>Could not read Prosscess Answer</h1>");
        return getBuffer();
    }

    // file errors
    byte[] fileNotFound(String reqFile) {
        Log.d("FileHandler", "file does not exist, is a directory rather than a regular file, or for some other reason cannot be opened for reading");
        writeLine("HTTP/1.1 404 OK");
        writeLine("Content-Type: text/html");
        writeLine("");
        writeLine("<html><h1>The file " + reqFile + " was not found</h1></html>");
        return getBuffer();
    }

    byte[] fileCouldNotBeRead() {
        Log.d("FileHandler", "File could not be read!");
        writeLine("HTTP/1.1 404 OK");
        writeLine("Content-Type: text/html");
        writeLine("");
        writeLine("<html><h1>File could not be read!</h1>");
        return getBuffer();
    }

    byte[] fileInputStreamNotCreated() {
        Log.d("FileHandler", "File input stream could not be created");
        writeLine("HTTP/1.1 404 OK");
        writeLine("Content-Type: text/html");
        writeLine("");
        writeLine("<html><h1>File input stream instance could not be created!</h1>");
        return getBuffer();
    }

    byte[] fileInputStreamNotClosed() {
        Log.d("FileHandler", "File input stream could not be created");
        writeLine("HTTP/1.1 404 OK");
        writeLine("Content-Type: text/html");
        writeLine("");
        writeLine("<html><h1>File input stream instance could not be created!</h1>");
        return getBuffer();
    }

    byte[] jsonFileNotRead() {
        Log.d("FileHandler", "Json file could not be read");
        writeLine("HTTP/1.1 404 OK");
        writeLine("Content-Type: text/html");
        writeLine("");
        writeLine("<html><h1>Json file could not be read</h1>");
        return getBuffer();
    }

    // socket errors
    public byte[] writingSocket() {
        Log.d("ServerThread", "Error writing to socket");
        return getBuffer();
    }

    // Privat Methods
    private void writeLine(String line) {
        bufferHandler.writeLine(line);
    }

    private byte[] getBuffer() {
        return bufferHandler.getBuffer();
    }


}