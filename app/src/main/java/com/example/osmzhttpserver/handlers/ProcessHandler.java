package com.example.osmzhttpserver.handlers;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ProcessHandler {
    private static ProcessHandler instance;
    private ProcessHandler() { }
    public static synchronized ProcessHandler getInstance() {
        if (instance == null) {
            instance = new ProcessHandler();
        }
        return instance;
    }

    ErrorHandler error = ErrorHandler.getInstance();
    BufferHandler bufferHandler = BufferHandler.getInstance();

    byte[] handleProcess(String[] cmd) {
        byte[] err = null;
        ProcessBuilder pb = new ProcessBuilder(cmd);
        Process p = null;

        try {
            p = pb.start();
        } catch (IOException e) {
            err = error.processStart();
        }
        assert p != null;

        BufferedReader p_in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader p_error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        Log.d("PROCESS", "Process was created!");

        String p_str;
        String p_error_str;

        writeLine("HTTP/0.1 200 OK");
        writeLine("Content-Type: text/html");
        writeLine("");

        try {
            while ((p_str = p_in.readLine()) != null && !p_str.isEmpty()) {
                Log.d("PROCESS", p_str);
                writeLine(p_str);
            }
        } catch (IOException e) {
            err = error.couldNotReadProccessAnswer();
        }

        try {
            while ((p_error_str = p_error.readLine()) != null && !p_error_str.isEmpty()) {
                err = error.process(p_error_str);
            }
        } catch (IOException e) {
            err = error.processRead(e);
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
