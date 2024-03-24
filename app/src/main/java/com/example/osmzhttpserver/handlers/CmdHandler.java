package com.example.osmzhttpserver.handlers;

public class CmdHandler {
    private static CmdHandler instance;

    private CmdHandler() { }

    public static synchronized CmdHandler getInstance() {
        if (instance == null) {
            instance = new CmdHandler();
        }
        return instance;
    }

    public void handleCommand(String reqFile) {
        String formatted = reqFile.substring(5);
        String[] cmd;
        if (formatted.contains("%20")) {
            cmd = formatted.split("%20");
        } else {
            cmd = new String[]{formatted};
        }
        ProcessHandler.getInstance().handleProcess(cmd);
    }
}
