package com.example.osmzhttpserver;

import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.Semaphore;


final public class ServerThread extends Thread {
    Socket s;
    Semaphore semaphore;
    String message;
    SocketServer socketServer;
    DataProvider dataProvider;

    public ServerThread(Socket s,
                        Semaphore semaphore,
                        SocketServer socketServer,
                        DataProvider dataProvider
    ) {
        this.semaphore = semaphore;
        this.s = s;
        this.socketServer = socketServer;
        this.dataProvider = dataProvider;
    }

    @Override
    public void run() {
        try {
            OutputStream out = s.getOutputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

            String tmp;
            String reqFile = "/";
            String[] log = {"", " -- [", "", "", " -- ", ""};

            while ((tmp = in.readLine()) != null && !tmp.isEmpty()) {
                // Log.d("HTTPREQUEST", "Request: " + tmp);
                if (tmp.startsWith("GET")) {
                    Log.d("HTTPREQUEST", "Request: " + tmp);
                    log[2] = Calendar.getInstance().getTime() + "] ";
                    log[3] = tmp;
                    reqFile = tmp.split(" ")[1];
                    if (reqFile.equals("/")) {
                        reqFile = "/index.html";
                    }
                    if (reqFile.equals("/streams/telemetry")) {
                        reqFile = "/streams/telemetry.json";
                    }
                }
                if (tmp.startsWith("Host:")) {
                    log[0] = tmp.substring(6, tmp.length());
                }
                if (tmp.startsWith("User-Agent:")) {
                    log[5] = tmp.substring(12, tmp.length());
                    socketServer.writeMessage(String.join("", log));
                }
            }

            String sdPath = Environment.getExternalStorageDirectory().getPath();
            File file = new File(sdPath + "/website" + reqFile);
            String extension = MimeTypeMap.getFileExtensionFromUrl(file.getAbsolutePath());

            if (file.exists()) {
                switch (extension) {
                    case "html" -> {
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
                    case "png" -> {
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
                    case "json" -> {
                        Log.d("SERVER", "Sending json...");
                        out.write("HTTP/1.1 200 OK\n".getBytes());
                        out.write("Content-Type: application/json\n".getBytes());
                        String str2 = "Content-Length: " + file.length() + "\n";
                        out.write(str2.getBytes());
                        out.write("\n".getBytes());
                        byte[] bytes = Files.readAllBytes(file.toPath());
                        for (byte aByte : bytes) {
                            out.write(aByte);
                        }
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
        } catch (IOException e) {
            Log.d("SERVER", "ServerThread(" + e.getStackTrace()[1].getLineNumber() + "): " + e.getMessage());
            StackTraceElement[] trace = e.getStackTrace();
            for (int i = 0; i < trace.length; i++) {
                Log.d("SERVER", "Catch: " + Arrays.toString(trace));
            }
        } finally {
            Log.d("SERVER", "Releasing semaphore");
            semaphore.release();
        }
    }
}