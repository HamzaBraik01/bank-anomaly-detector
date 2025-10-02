package com.solubank.entity;

import java.math.BigDecimal;


public sealed abstract class Compte permits CompteCourant, CompteEpargne {
    private final Long id;
    private final String numero;
    private BigDecimal solde;
    private final Long idClient;

    public Compte(Long id, String numero, BigDecimal solde, Long idClient) {
        this.id = id;
        this.numero = numero;
        this.solde = solde;
        this.idClient = idClient;
    }

    
    public Long getId() { return id; }
    public String getNumero() { return numero; }
    public BigDecimal getSolde() { return solde; }
    public Long getIdClient() { return idClient; }

    public void setSolde(BigDecimal solde) {
        this.solde = solde;
    }

    public abstract String getTypeCompte();
}