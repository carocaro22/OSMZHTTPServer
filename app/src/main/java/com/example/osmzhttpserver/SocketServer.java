package com.example.osmzhttpserver;
import android.content.Context;
import android.os.Message;
import android.util.Log;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

public class SocketServer extends Thread {

    ServerSocket serverSocket;
    public final int port = 12345;
    boolean bRunning;

    Context context;
    Semaphore semaphore = new Semaphore(2, true);

    public SocketServer(Context context) {
        this.context = context;
    }

    public void close() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            Log.d("SERVER", "Error, probably interrupted in accept(), see log");
            StackTraceElement[] trace = e.getStackTrace();
            for (int i = 0; i < trace.length; i++) {
                Log.d("SERVER", Arrays.toString(trace));
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
                    ServerThread serverThread = new ServerThread(s, semaphore);
                    serverThread.start();
                } else {
                    out.write("HTTP 503 Server too busy\n".getBytes());
                }
                Message msg = Message.obtain();
                String str = msg.toString() + "\n\n" ; 
                Log.d("SERVER", str);
            }
        }
        catch (IOException e) {
            if (serverSocket != null && serverSocket.isClosed())
                Log.d("SERVER", "Normal exit");
            else {
                Log.d("SERVER", "Error");
                StackTraceElement[] trace = e.getStackTrace();
                for (int i = 0; i < trace.length; i++) {
                    Log.d("SERVER", Arrays.toString(trace));
                }
            }
        }
        finally {
            serverSocket = null;
            bRunning = false;
        }
    }
}
