package com.example.osmzhttpserver;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

final public class SocketServer extends Thread {

    ServerSocket serverSocket;
    public final int port = 12345;
    boolean bRunning;
    TextView messages_list;
    String message;

    DataProvider dataProvider;
    
    Context context;
    Semaphore semaphore = new Semaphore(2, true);

    public SocketServer(Context context, TextView messages_list, DataProvider dataProvider) {
        this.context = context;
        this.messages_list = messages_list;
        this.dataProvider = dataProvider;
    }

    public void close() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            Log.d("SERVER", "Error, probably interrupted in accept(), see log");
            StackTraceElement[] trace = e.getStackTrace();
            for (int i = 0; i < trace.length; i++) {
                Log.d("SERVER", "SocketServer(" + e.getStackTrace()[1].getLineNumber() + "): " + Arrays.toString(trace));
            }
        }
        bRunning = false;
    }

    public void run() {
        try {
            Log.d("SERVER", "Creating Socket");
            serverSocket = new ServerSocket(port);
            bRunning = true;

            while (bRunning) {
                Log.d("SERVER", "Socket Waiting for connection");
                Socket s = serverSocket.accept();
                OutputStream out = s.getOutputStream();
                Log.d("SERVER", "Socket Accepted");

                if (semaphore.tryAcquire()) {
                    ServerThread serverThread = new ServerThread(
                            s, semaphore, this, dataProvider);
                    serverThread.start();
                } else {
                    out.write("HTTP 503 Server too busy\n".getBytes());
                }
            }
        }
        catch (IOException e) {
            if (serverSocket != null && serverSocket.isClosed())
                Log.d("SERVER", "Normal exit");
            else {
                Log.d("SERVER", "Error: " + e.getMessage());
                StackTraceElement[] trace = e.getStackTrace();
                for (int i = 0; i < trace.length; i++) {
                    Log.d("SERVER", "SocketServer(" + e.getStackTrace()[1].getLineNumber() + "): " + Arrays.toString(trace));
                }
            }
        }
        finally {
            serverSocket = null;
            bRunning = false;
        }
    }
    
    public void writeMessage(String m) {
        message = message + m + "\n\n";
        messages_list.setText(message);
    }
}
