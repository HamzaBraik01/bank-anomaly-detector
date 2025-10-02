package com.solubank.service;

import com.solubank.dao.CompteDAO;
import com.solubank.dao.TransactionDAO;
import com.solubank.entity.Compte;
import com.solubank.entity.CompteCourant;
import com.solubank.entity.CompteEpargne;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class CompteService {
    private final CompteDAO compteDAO;
    private final TransactionDAO transactionDAO;
    
    public CompteService() {
        this.compteDAO = new CompteDAO();
        this.transactionDAO = new TransactionDAO();
    }
    
    public Optional<Compte> creerCompteCourant(Long clientId, BigDecimal soldeInitial, BigDecimal decouvertAutorise) {
        String numeroCompte = genererNumeroCompte();
        CompteCourant compte = new CompteCourant(null, numeroCompte, soldeInitial, clientId, decouvertAutorise);
        return compteDAO.save(compte);
    }
    
    public Optional<Compte> creerCompteEpargne(Long clientId, BigDecimal soldeInitial, BigDecimal tauxInteret) {
        String numeroCompte = genererNumeroCompte();
        CompteEpargne compte = new CompteEpargne(null, numeroCompte, soldeInitial, clientId, tauxInteret);
        return compteDAO.save(compte);
    }
    
    public boolean mettreAJourSolde(Long compteId, BigDecimal nouveauSolde) {
        return compteDAO.updateSolde(compteId, nouveauSolde);
    }
    
    public boolean mettreAJourDecouvertAutorise(Long compteId, BigDecimal nouveauDecouvert) {
        Optional<Compte> compteOpt = compteDAO.findById(compteId);
        if (compteOpt.isPresent() && compteOpt.get() instanceof CompteCourant compteCourant) {
            compteCourant.setDecouvertAutorise(nouveauDecouvert);
            return true;
        }
        return false;
    }
    
    public boolean mettreAJourTauxInteret(Long compteId, BigDecimal nouveauTaux) {
        Optional<Compte> compteOpt = compteDAO.findById(compteId);
        if (compteOpt.isPresent() && compteOpt.get() instanceof CompteEpargne compteEpargne) {
            compteEpargne.setTauxInteret(nouveauTaux);
            return true;
        }
        return false;
    }
    
    public Optional<Compte> rechercherParId(Long id) {
        return compteDAO.findById(id);
    }
    
    public Optional<Compte> rechercherParNumero(String numero) {
        return compteDAO.findByNumero(numero);
    }
    
    public List<Compte> rechercherParClient(Long clientId) {
        return compteDAO.findByClientId(clientId);
    }
    
    public List<Compte> listerTousLesComptes() {
        return compteDAO.findAll();
    }
    
    public Optional<Compte> trouverCompteAvecSoldeMaximum() {
        return compteDAO.findCompteWithMaxSolde();
    }
    
    public Optional<Compte> trouverCompteAvecSoldeMinimum() {
        return compteDAO.findCompteWithMinSolde();
    }
    
    
    public List<Compte> trouverTopComptes(int limite) {
        return compteDAO.findAll()
                .stream()
                .sorted(Comparator.comparing(Compte::getSolde).reversed())
                .limit(limite)
                .toList();
    }
    
    
    public BigDecimal calculerSoldeMoyen() {
        List<Compte> comptes = compteDAO.findAll();
        if (comptes.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal somme = comptes.stream()
                .map(Compte::getSolde)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return somme.divide(BigDecimal.valueOf(comptes.size()), 2, BigDecimal.ROUND_HALF_UP);
    }
    
    
    public boolean peutEffectuerRetrait(Long compteId, BigDecimal montant) {
        Optional<Compte> compteOpt = compteDAO.findById(compteId);
        if (compteOpt.isEmpty()) {
            return false;
        }
        
        Compte compte = compteOpt.get();
        BigDecimal soldeApresRetrait = compte.getSolde().subtract(montant);
        
        return switch (compte) {
            case CompteCourant cc -> 
                soldeApresRetrait.compareTo(cc.getDecouvertAutorise().negate()) >= 0;
            case CompteEpargne ce -> 
                soldeApresRetrait.compareTo(BigDecimal.ZERO) >= 0;
        };
    }
    
    public boolean supprimerCompte(Long compteId) {
        var transactions = transactionDAO.findByCompteId(compteId);
        if (!transactions.isEmpty()) {
            System.err.println("Impossible de supprimer le compte. Il y a encore des transactions associées.");
            return false;
        }
        return compteDAO.delete(compteId);
    }
    
    private String genererNumeroCompte() {
        return "ACC" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }
}