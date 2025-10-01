package com.solubank.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Représente une transaction bancaire
 * Utilise record pour l'immutabilité
 */
public record Transaction(
    Long id,
    LocalDateTime date,
    BigDecimal montant,
    TypeTransaction type,
    String lieu,
    Long idCompte
) {
    public Transaction {
        if (montant == null || montant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant doit être positif");
        }
        if (type == null) {
            throw new IllegalArgumentException("Le type de transaction est obligatoire");
        }
        if (idCompte == null) {
            throw new IllegalArgumentException("L'ID du compte est obligatoire");
        }
    }
}