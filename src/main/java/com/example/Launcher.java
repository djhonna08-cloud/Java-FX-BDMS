package com.example;

import javafx.application.Application;

/**
 * Main launcher class for the Barangay San Marino BDMS application.
 * This class serves as the entry point and launches the JavaFX application.
 */
public class Launcher {
    /**
     * Main method to start the application.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        try {
            Application.launch(App.class, args);
        } catch (Exception e) {
            System.err.println("Failed to start application: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}