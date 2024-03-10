package com.example.osmzhttpserver;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Arrays;

public class SocketServer extends Thread {

    ServerSocket serverSocket;
    public final int port = 12345;
    boolean bRunning;

    Context context;

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
                Log.d("SERVER", "Socket Accepted");

                OutputStream out = s.getOutputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

                String tmp;
                String reqFile = "/";
                
                while (!(tmp = in.readLine()).isEmpty()) {
                    Log.d("HTTP REQUEST", tmp);
                    if (tmp.startsWith("GET")) {
                        reqFile = tmp.split(" ")[1];
                        if (reqFile.equals("/")){
                            reqFile = "/index.html";
                        }
                        break;
                    }
                    tmp = in.readLine();
                }

                String sdPath = Environment.getExternalStorageDirectory().getPath();
                File file = new File(sdPath + "/website" + reqFile);
                String extension = MimeTypeMap.getFileExtensionFromUrl(file.getAbsolutePath());
                
                if (file.exists()) {
                    if (extension.equals("html")) {
                        out.write("HTTP/1.1 200 OK\n".getBytes());
                        out.write("Content-Type: text/html\n".getBytes());
                        String str1 = "Content-Length: " + file.length() + "\n";
                        out.write(str1.getBytes());
                        out.write("\n".getBytes());
                        byte[] bytes = Files.readAllBytes(file.toPath());
                        for (byte aByte : bytes) {
                            out.write(aByte);
                        }
                    }
                    else if (extension.equals("png")) {
                        out.write("HTTP/1.1 200 OK\n".getBytes());
                        out.write("Content-Type: image/png\n".getBytes());
                        String str2 = "Content-Length: " + file.length() + "\n";
                        out.write(str2.getBytes());
                        out.write("\n".getBytes());

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
                } else {
                    out.write("HTTP/1.1 404 OK\n".getBytes());
                    out.write("Content-Type: text/html\n".getBytes());
                    out.write("\n".getBytes());
                    out.write("<html><h1>Your page was not found</h1></html>\n".getBytes());
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

