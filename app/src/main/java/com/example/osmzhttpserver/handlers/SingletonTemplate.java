package com.example.osmzhttpserver.handlers;

public class SingletonTemplate {
    private static SingletonTemplate instance;

    private SingletonTemplate() {
        // private constructor to prevent instantiation
    }

    public static synchronized SingletonTemplate getInstance() {
        if (instance == null) {
            instance = new SingletonTemplate();
        }
        return instance;
    }
}