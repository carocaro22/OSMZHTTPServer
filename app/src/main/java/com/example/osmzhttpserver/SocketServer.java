package com.example.osmzhttpserver;
import android.util.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class SocketServer extends Thread {

    ServerSocket serverSocket;
    public final int port = 12345;
    boolean bRunning;

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
                Log.d("SERVER", "Socket Accepted");

                OutputStream o = s.getOutputStream();
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(o));
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

                String tmp;
                while (!(tmp = in.readLine()).isEmpty()) {
                    Log.d("HTTP REQUEST", tmp);
                    if (tmp.startsWith("GET")) {
                        out.write("HTTP/1.1 200 OK\n");
                        out.write("Content-Type: text/html\n");
                        out.write("\n");
                        out.write("<html><h1>Good Morning!</h1></html>\n");
                        break;
                    }
                    tmp = in.readLine();
                }
                
                out.flush();
                s.close();
                Log.d("SERVER", "Socket Closed");
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

