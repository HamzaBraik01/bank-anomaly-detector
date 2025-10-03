package com.solubank;

import com.solubank.ui.MenuPrincipal;
import com.solubank.util.DatabaseConnection;

public class Main {
    public static void main(String[] args) {
        try {
            DatabaseConnection.getConnection();
            
            MenuPrincipal menu = new MenuPrincipal();
            menu.demarrer();
            
        } catch (Exception e) {
            System.err.println("Erreur lors du demarrage de l'application: " + e.getMessage());
            System.err.println("Verifiez que MySQL est demarre et que la base de donnees 'bank_anomaly_db' existe.");
        } finally {
            DatabaseConnection.closeConnection();
        }
    }
}