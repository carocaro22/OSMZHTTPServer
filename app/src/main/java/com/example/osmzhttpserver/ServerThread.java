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
import java.util.concurrent.Semaphore;

public class ServerThread extends Thread {

    Socket s;
    Semaphore semaphore; 

    public ServerThread(Socket s, Semaphore semaphore) {
        this.semaphore = semaphore;
        this.s = s;
    }
    
    @Override
    public void run() {
        try {
        OutputStream out = s.getOutputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

        String tmp;
        String reqFile = "/";

// in does not exist 
            while (!(tmp = in.readLine()).isEmpty()) {
                Log.d("HTTPREQUEST", tmp);
                if (tmp.startsWith("GET")) {
                    reqFile = tmp.split(" ")[1];
                    if (reqFile.equals("/")) {
                        reqFile = "/index.html";
                    }
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
                } else if (extension.equals("png")) {
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
        } catch (IOException e) {
            Log.d("SERVER", "Error");
            StackTraceElement[] trace = e.getStackTrace();
            for (int i = 0; i < trace.length; i++) {
                Log.d("SERVER", Arrays.toString(trace));
            }
        }
        finally {
            Log.d("SERVER", "Releasing semaphore");
            semaphore.release();
        }
    }
}