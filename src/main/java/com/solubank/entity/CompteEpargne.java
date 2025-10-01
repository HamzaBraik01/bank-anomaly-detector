package com.solubank.entity;

import java.math.BigDecimal;


public final class CompteEpargne extends Compte {
    private BigDecimal tauxInteret;

    public CompteEpargne(Long id, String numero, BigDecimal solde, Long idClient, BigDecimal tauxInteret) {
        super(id, numero, solde, idClient);
        this.tauxInteret = tauxInteret;
    }

    public BigDecimal getTauxInteret() {
        return tauxInteret;
    }

    public void setTauxInteret(BigDecimal tauxInteret) {
        this.tauxInteret = tauxInteret;
    }

    @Override
    public String getTypeCompte() {
        return "EPARGNE";
    }
}