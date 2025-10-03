package com.solubank.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/bank_anomaly_db";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    private static Connection connection;
    private static boolean firstConnection = true;

    private DatabaseConnection() {}

    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            if (firstConnection) {
                System.out.println("Connexion établie avec succès !");
                firstConnection = false;
            }
        }
        return connection;
    }

    public static synchronized void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null; 
                System.out.println("Connexion fermée avec succès !");
                firstConnection = true; // Reset for next session
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture de la connexion: " + e.getMessage());
            }
        }
    }
}
