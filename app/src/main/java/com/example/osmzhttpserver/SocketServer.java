package com.example.osmzhttpserver;

import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Semaphore;

final public class SocketServer extends Thread {
    private final TextView messages_list;
    private final DataProvider dataProvider;

    private ServerSocket serverSocket;
    private OutputStream out = null;
    private String message;
    boolean bRunning = true;
    private final Semaphore semaphore = new Semaphore(2, true);

    public SocketServer(TextView messages_list, DataProvider dataProvider) {
        this.messages_list = messages_list;
        this.dataProvider = dataProvider;
    }

    public void run() {
        int port = 12345;
        Log.d("SocketServer", "Creating Socket at port: " + port);
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            Log.d("SocketServer", "Could not create server socket instance");
            Log.d("SocketServer", Objects.requireNonNull(e.getMessage()));
        }
        while (bRunning) {
            Log.d("SocketServer", "Socket Waiting for connection");
            try {
                Socket socket = serverSocket.accept();
                out = getSocketOutputStream(socket);
                assert out != null;
                Log.d("SocketServer", "Socket Accepted");
                createThread(socket);
            } catch (IOException e) {
                if (serverSocket != null && serverSocket.isClosed())
                    Log.d("SocketServer", "Normal exit");
                else {
                    Log.d("SocketServer", "Error: " + e.getMessage());
                    StackTraceElement[] trace = e.getStackTrace();
                    for (int i = 0; i < trace.length; i++) {
                        Log.d("SocketServer", "SocketServer(" + e.getStackTrace()[1].getLineNumber() + "): " + Arrays.toString(trace));
                    }
                }
            }
        }
    }

    private OutputStream getSocketOutputStream(Socket socket) {
        OutputStream socketOut = null;
        try {
            socketOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.d("SocketServer", "Could not get socket OutputStream");
        }
        return socketOut;
    }

    void writeMessage(String m) {
        message = message + m + "\n\n";
        messages_list.setText(message);
    }

    void createThread(Socket socket) {
        if (semaphore.tryAcquire()) {
            ServerThread serverThread = new ServerThread(socket, semaphore, dataProvider);
            serverThread.start();
        } else {
            try {
                out.write("HTTP 503 Server too busy\n".getBytes());
            } catch (IOException e) {
                Log.d("SocketServer", "Could not write to socket output");
            }
        }
    }
}
