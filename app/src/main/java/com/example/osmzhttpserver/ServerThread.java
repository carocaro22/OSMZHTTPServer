package com.example.osmzhttpserver;

import android.util.Log;

import com.example.osmzhttpserver.handlers.BufferHandler;
import com.example.osmzhttpserver.handlers.CmdHandler;
import com.example.osmzhttpserver.handlers.ErrorHandler;
import com.example.osmzhttpserver.handlers.FileHandler;
import com.example.osmzhttpserver.handlers.RequestHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.Semaphore;


final public class ServerThread extends Thread {
    Socket socket;
    Semaphore semaphore;
    DataProvider dataProvider;

    OutputStream websiteOut;
    BufferedReader websiteIn;

    // Services
    private final RequestHandler requestHandler = RequestHandler.getInstance();
    private final FileHandler fileHandler = FileHandler.getInstance();
    private final ErrorHandler error = ErrorHandler.getInstance();


    public ServerThread(Socket socket,
                        Semaphore semaphore,
                        DataProvider dataProvider
    ) {
        this.semaphore = semaphore;
        this.socket = socket;
        this.dataProvider = dataProvider;
    }

    @Override
    public void run() {
        websiteOut = getOutputStream();
        websiteIn = getBufferedReader();
        String tmp;
        String reqFile = "/";

        try {
            while ((tmp = websiteIn.readLine()) != null && !tmp.isEmpty()) {
                requestHandler.handleRequest(tmp);
            }
            //Log.d("DEBUG", "end of while loop");
        } catch (IOException e) {
            Log.d("ServerThread", "Could not read socket line");
        }
        if (requestHandler.getReqFile().startsWith("/cmd")) {
            CmdHandler.getInstance().handleCommand(requestHandler.getReqFile());
        } else {
            byte[] response = fileHandler.getResponse(requestHandler.getReqFile());
            try {
                websiteOut.write(response);
            } catch (IOException e) {
                error.writingSocket();
            }
        }
        flushSocket();
        try {
            socket.close();
        } catch (IOException e) {
            Log.e("ServerThread", "Could not close socket", e);
        }
        BufferHandler.getInstance().resetBuffer();
        semaphore.release();
    }

    void flushSocket() {
        try {
            websiteOut.flush();
        } catch (IOException e) {
            Log.d("ServerThread", "Could not flush");
        }
    }

    void writeLineToWebsite(String line) {
        byte[] bytes = line.getBytes();
        try {
            websiteOut.write(bytes);
            websiteOut.write("\n".getBytes());
        } catch (IOException e) {
            Log.d("ServerThread", "Could not write line");
        }
    }

    OutputStream getOutputStream() {
        OutputStream out = null;
        try {
            out = socket.getOutputStream();
        } catch (IOException e) {
            Log.d("ServerThread", "Could not get OutputStream");
        }
        return out;
    }

    BufferedReader getBufferedReader() {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            Log.d("ServerThread", "Could not get Input Reader");
        }
        return in;
    }
}