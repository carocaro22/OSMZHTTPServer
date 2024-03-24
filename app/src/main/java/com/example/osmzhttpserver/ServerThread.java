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
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.Semaphore;


final public class ServerThread extends Thread {
    Socket s;
    Semaphore semaphore;
    SocketServer socketServer;
    DataProvider dataProvider;

    OutputStream websiteOut;
    BufferedReader websiteIn;
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

        websiteOut = getOutputStream();
        websiteIn = getBufferedReader();
        try {
            String tmp;
            String reqFile = "/";

            while ((tmp = websiteIn.readLine()) != null && !tmp.isEmpty()) {
                handleRequest(tmp);
            }

            String sdPath = Environment.getExternalStorageDirectory().getPath();
            File file = new File(sdPath + "/website" + reqFile);
            Path path = file.toPath();

            if (Files.isRegularFile(path)) {
                handleFile(file);
            } else {
                File[] files = file.listFiles();
                writeLineToWebsite("HTTP/1.1 200 OK");
                writeLineToWebsite("Content-Type: text/html");
                writeLineToWebsite("");
                assert files != null;
                for (File someFile : files) {
                    writeLineToWebsite(someFile.getName());
                }
            }

            websiteOut.flush();
            s.close();
            Log.d("SERVER", "Socket Closed");
        } catch (IOException e) {
            handleError();
           handleExeption(e);
        } finally {
            Log.d("SERVER", "Releasing semaphore");
            semaphore.release();
        }
    }

    void handleFile(File file) throws IOException {
        String extension = MimeTypeMap.getFileExtensionFromUrl(file.getAbsolutePath());
        if (extension.equals("json")) {
            Log.d("SERVER", "Sending json...");
            writeLineToWebsite("HTTP/1.1 200 OK");
            writeLineToWebsite("Content-Type: application/json");
            String str2 = "Content-Length: " + file.length();
            writeLineToWebsite(str2);
            writeLineToWebsite("");
            byte[] bytes = Files.readAllBytes(file.toPath());
            for (byte aByte : bytes) {
                websiteOut.write(aByte);
            }
        } else {
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            writeLineToWebsite("HTTP/1.1 200 OK");
            writeLineToWebsite("Content-Type: " + mimeType);
            String str2 = "Content-Length: " + file.length();
            writeLineToWebsite(str2);
            writeLineToWebsite("");

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
    }

    void handleRequest(String tmp) throws IOException {
        if (tmp.startsWith("GET")) {
            String[] log = {"", " -- [", "", "", " -- ", ""};
            Log.d("HTTPREQUEST", "Request: " + tmp);
            log[2] = Calendar.getInstance().getTime() + "] ";
            log[3] = tmp;
            String reqFile = tmp.split(" ")[1];
            if (reqFile.equals("/")) {
                reqFile = "/index.html";
            }
            if (reqFile.equals("/streams/telemetry")) {
                reqFile = "/streams/telemetry.json";
            }
            // probably a command then
            if (reqFile.startsWith("/cmd")) {
                handleCommand(reqFile);
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

    void handleCommand(String reqFile) {
        String formatted = reqFile.substring(5);
        String[] cmd;
        if (formatted.contains("%20")) {
            cmd = formatted.split("%20");
        } else {
            cmd = new String[]{formatted};
        }
        handleProccess(cmd);
    }

    void handleProccess(String[] cmd) {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        Process p = null;
        try {
            p = pb.start();
        } catch (IOException e) {
            Log.d("PROCESS", "Could not start Processs");
        }
        assert p != null;
        BufferedReader p_in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader p_error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        Log.d("PROCESS", "Process was created!");

        String p_str;
        String p_error_str;

        try {
            while ((p_str = p_in.readLine()) != null && !p_str.isEmpty()) {
                Log.d("PROCESS", p_str);
                writeLineToWebsite("HTTP/0.1 200 OK");
                writeLineToWebsite("Content-Type: text/html");
                writeLineToWebsite("");
                writeLineToWebsite(p_str);
            }
        } catch (IOException e) {
           Log.d("PROCESS", "Could not read proccess");
        }

        try {
            while ((p_error_str = p_error.readLine()) != null && !p_error_str.isEmpty()) {
                Log.d("PROCESS", "error: " + p_error_str);
                handleError();
            }
        } catch (IOException e) {
            Log.d("PROCESS", "could not read proccess error");
        }
    }

    void handleError() {
        writeLineToWebsite("HTTP/1.1 404 OK");
        writeLineToWebsite("Content-Type: text/html");
        writeLineToWebsite("");
        writeLineToWebsite("<html><h1>Your page was not found</h1></html>");
    }

    void writeLineToWebsite(String line) {
        byte[] bytes = line.getBytes();
        try {
            websiteOut.write(bytes);
            websiteOut.write("\n".getBytes());
        } catch (IOException e) {
            Log.d("SERVER", "Could not write line");
        }
    }

    OutputStream getOutputStream() {
        OutputStream out = null;
        try {
            out = s.getOutputStream();
        } catch (IOException e) {
            Log.d("SERVER", "Could not get OutputStream");
        }
        return out;
    }

    BufferedReader getBufferedReader() {
        BufferedReader in = s.getBufferedReader();
        return in;
    }
}