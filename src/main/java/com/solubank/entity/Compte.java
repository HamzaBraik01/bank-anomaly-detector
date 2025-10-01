package com.solubank.entity;

public sealed class Compte permits CompteCourant, CompteEpargne{
    private final Long id ;
    private final String numero;
    private final BigDecimal solde;
    private final Long idClient;

    public Compte (Long id, String numero, BigDecimal solde, Long idClient){
        this.id = id;
        this.numero = numero;
        this.solde = solde;
        this.idClient =idClient;
    }

    public Long getId(){
        return id;
    }
    public String getNumero(){
        return numero;
    }
    public BigDecimal getSolde(){
        return solde;
    }
    public Long getIdClient(){
        return idClient;
    }

    public void setSolde(){
        this.solde = solde;
    }

    public abstract String getTypeCompte();
    

}