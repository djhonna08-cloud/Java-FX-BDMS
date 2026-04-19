package com.example;

import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {
        try {
            Application.launch(App.class, args);
        } catch (Exception e) {
            System.err.println("Failed to start application: " + e.getMessage());
            e.printStackTrace();
        }
    }
}