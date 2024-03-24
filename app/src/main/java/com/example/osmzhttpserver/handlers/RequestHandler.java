package com.example.osmzhttpserver.handlers;

import android.util.Log;

import java.util.Calendar;

public class RequestHandler {
    private final String[] log = {"", " -- [", "", "", " -- ", ""};
    private static RequestHandler instance;
    private String reqFile;

    private RequestHandler() { }

    public static synchronized RequestHandler getInstance() {
        if (instance == null) {
            instance = new RequestHandler();
        }
        return instance;
    }

    public void handleRequest(String tmp) {
        if (tmp.startsWith("GET")) {
            reqFile = "/";
            Log.d("RequestHandler", "Request: " + tmp);
            log[2] = Calendar.getInstance().getTime() + "] ";
            log[3] = tmp;
            reqFile = tmp.split(" ")[1];

            if (tmp.startsWith("Host:")) {
                log[0] = tmp.substring(6);
            }
            if (tmp.startsWith("User-Agent:")) {
                log[5] = tmp.substring(12);
                // TODO: socketServer.writeMessage();
            }

            FileHandler fileHandler = FileHandler.getInstance();

            if (reqFile.equals("/")) {
                reqFile = "/index.html";
            }
            else if (reqFile.equals("/streams/telemetry")) {
                reqFile = "/streams/telemetry.json";
            }
        }
    }

    public String getReqFile() {
        return reqFile;
    }

    public String getLog() {
        return String.join("", log);
    }
}
