package com.example.osmzhttpserver;

import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.BufferedReader;
import java.io.File;
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

            while ((tmp = in.readLine()) != null && !tmp.isEmpty()) {
                handleRequest(tmp, reqFile, out);
            }

            String sdPath = Environment.getExternalStorageDirectory().getPath();
            File file = new File(sdPath + "/website" + reqFile);

            if (file.exists()) {
                handleFile(out, file);
            } else {
                File[] files = file.listFiles();
                out.write("HTTP/1.1 200 OK\n".getBytes());
                out.write("Content-Type: text/html\n".getBytes());
                out.write("\n".getBytes());
                assert files != null;
                for (File someFile : files) {
                    out.write(someFile.getName().getBytes());
                }
                handleError(out);
            }

            out.flush();
            s.close();
            Log.d("SERVER", "Socket Closed");
        } catch (IOException e) {
           handleExeption(e);
        } finally {
            Log.d("SERVER", "Releasing semaphore");
            semaphore.release();
        }
    }

    void handleFile(OutputStream out, File file) throws IOException {
        String extension = MimeTypeMap.getFileExtensionFromUrl(file.getAbsolutePath());
        if (extension.equals("json")) {
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
        } else {
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            out.write("HTTP/1.1 200 OK\n".getBytes());
            String str2 = "Content-Type: " + mimeType + "\n";
            out.write(str2.getBytes());
            String str1 = "Content-Length: " + file.length() + "\n";
            out.write(str1.getBytes());
            out.write("\n".getBytes());
            byte[] bytes = Files.readAllBytes(file.toPath());
            for (byte aByte : bytes) {
                out.write(aByte);
            }
        }
    }

    void handleRequest(String tmp, String reqFile, OutputStream out) throws IOException {
        if (tmp.startsWith("GET")) {
            String[] log = {"", " -- [", "", "", " -- ", ""};
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
            // probably a command then
            if (reqFile.startsWith("/cmd")) {
                handleCommand(reqFile, out);
            }
            if (tmp.startsWith("Host:")) {
                log[0] = tmp.substring(6);
            }
            if (tmp.startsWith("User-Agent:")) {
                log[5] = tmp.substring(12);
                socketServer.writeMessage(String.join("", log));
            }
        }
    }
    void handleExeption(IOException e) {
        Log.d("SERVER", "ServerThread(" + e.getStackTrace()[1].getLineNumber() + "): " + e.getMessage());
        StackTraceElement[] trace = e.getStackTrace();
        for (int i = 0; i < trace.length; i++) {
            Log.d("SERVER", "Catch: " + Arrays.toString(trace));
        }
    }

    void handleCommand(String reqFile, OutputStream out) throws IOException {
        String formatted = reqFile.substring(5);
        String[] cmd;
        if (formatted.contains("%20")) {
            cmd = formatted.split("%20");
        } else {
            cmd = new String[]{formatted};
        }
        ProcessBuilder pb = new ProcessBuilder(cmd);
        Process p = pb.start();
        BufferedReader p_in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader p_error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        Log.d("PROCESS", "Process was a success!");
        String p_str;
        String p_error_str;

        while ((p_str = p_in.readLine()) != null && !p_str.isEmpty()) {
            Log.d("PROCESS", p_str);
            out.write("HTTP/1.1 200 OK\n".getBytes());
            out.write("Content-Type: text/html\n".getBytes());
            out.write("\n".getBytes());
            out.write(p_str.getBytes());
        }
        while ((p_error_str = p_error.readLine()) != null && !p_error_str.isEmpty()) {
            Log.d("PROCESS", "error: " + p_error_str);
        }
    }
    void handleError(OutputStream out) throws IOException {
        out.write("HTTP/1.1 404 OK\n".getBytes());
        out.write("Content-Type: text/html\n".getBytes());
        out.write("\n".getBytes());
        out.write("<html><h1>Your page was not found</h1></html>\n".getBytes());
    }
}