package com.solubank.entity;

import java.math.BigDecimal;


public final class CompteCourant extends Compte {
    private BigDecimal decouvertAutorise;

    public CompteCourant(Long id, String numero, BigDecimal solde, Long idClient, BigDecimal decouvertAutorise) {
        super(id, numero, solde, idClient);
        this.decouvertAutorise = decouvertAutorise;
    }

    public BigDecimal getDecouvertAutorise() {
        return decouvertAutorise;
    }

    public void setDecouvertAutorise(BigDecimal decouvertAutorise) {
        this.decouvertAutorise = decouvertAutorise;
    }

    @Override
    public String getTypeCompte() {
        return "COURANT";
    }
}