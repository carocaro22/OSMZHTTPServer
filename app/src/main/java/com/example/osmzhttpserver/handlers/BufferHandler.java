package com.example.osmzhttpserver.handlers;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class BufferHandler {
    private static BufferHandler instance;
    private byte[] buffer;
    private ByteArrayOutputStream out = new ByteArrayOutputStream( );

    private BufferHandler() { }

    public static synchronized BufferHandler getInstance() {
        if (instance == null) {
            instance = new BufferHandler();
        }
        return instance;
    }

    void writeLine(String line) {
        byte[] bytes = line.getBytes();
        try {
            out.write(bytes);
            out.write("\n".getBytes());
        } catch (IOException e) {
            Log.d("ServerThread", "Could not write line");
        }
    }

    void writeByte(byte[] bytes) {
        try {
            out.write(bytes);
        } catch (IOException e) {
            Log.d("ServerThread", "Could not write bytes");
        }
    }

    byte[] getBuffer() {
        byte[] bytes = out.toByteArray();
        try {
            out.flush();
        } catch (IOException e) {
            Log.d("BufferHandler", "could not flush bytes buffer.");
        }
        return bytes;
    }

    public void resetBuffer() {
        out.reset();
    }
}
