package com.solubank.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;


public class ValidationUtil {
    
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    
    private static final Pattern NOMBRE_PATTERN = 
        Pattern.compile("^\\d+(\\.\\d{1,2})?$");
    
    
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
    
    
    public static boolean isValidMontant(String montantStr) {
        if (montantStr == null || montantStr.trim().isEmpty()) {
            return false;
        }
        
        try {
            BigDecimal montant = new BigDecimal(montantStr);
            return montant.compareTo(BigDecimal.ZERO) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    
    public static BigDecimal parseMontant(String montantStr) {
        try {
            return new BigDecimal(montantStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Montant invalide: " + montantStr);
        }
    }
    
    
    public static boolean isValidNom(String nom) {
        return nom != null && !nom.trim().isEmpty();
    }
    
    
    public static boolean isValidId(String idStr) {
        try {
            long id = Long.parseLong(idStr);
            return id > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    
    public static Long parseId(String idStr) {
        try {
            return Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID invalide: " + idStr);
        }
    }
    
    public static String formatMontant(BigDecimal montant) {
        return String.format("%.2f", montant);
    }
}