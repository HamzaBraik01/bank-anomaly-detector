package com.solubank.entity;


public record Client(Long id ,String nom ,String email){
    public Client {
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du client ne peut pas être vide");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("L'email doit être valide");
        }
    }


}