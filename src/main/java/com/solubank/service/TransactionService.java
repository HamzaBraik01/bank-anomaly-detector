package com.solubank.service;

import com.solubank.dao.TransactionDAO;
import com.solubank.entity.Transaction;
import com.solubank.entity.TypeTransaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


public class TransactionService {
    private final TransactionDAO transactionDAO;
    private final CompteService compteService;
    
    private static final BigDecimal SEUIL_MONTANT_SUSPECT = new BigDecimal("10000");
    private static final String PAYS_HABITUEL = "Maroc";
    private static final int MINUTES_SUSPICION = 1;
    
    public TransactionService() {
        this.transactionDAO = new TransactionDAO();
        this.compteService = new CompteService();
    }
    
    public Optional<Transaction> enregistrerTransaction(BigDecimal montant, TypeTransaction type, 
                                                      String lieu, Long compteId) {
        try {
            if (compteService.rechercherParId(compteId).isEmpty()) {
                System.err.println("Compte inexistant");
                return Optional.empty();
            }
            
            if (type == TypeTransaction.RETRAIT) {
                if (!compteService.peutEffectuerRetrait(compteId, montant)) {
                    System.err.println("Fonds insuffisants pour effectuer le retrait");
                    return Optional.empty();
                }
            }
            
            Transaction transaction = new Transaction(
                null,
                LocalDateTime.now(),
                montant,
                type,
                lieu,
                compteId
            );
            
            Optional<Transaction> savedTransaction = transactionDAO.save(transaction);
            
            if (savedTransaction.isPresent()) {
                mettreAJourSoldeCompte(compteId, montant, type);
            }
            
            return savedTransaction;
            
        } catch (IllegalArgumentException e) {
            System.err.println("Erreur de validation: " + e.getMessage());
            return Optional.empty();
        }
    }
    
    public List<Transaction> listerTransactionsParCompte(Long compteId) {
        return transactionDAO.findByCompteId(compteId);
    }
    
    public List<Transaction> listerTransactionsParClient(Long clientId) {
        return transactionDAO.findByClientId(clientId);
    }
    
    public List<Transaction> filtrerParType(TypeTransaction type) {
        return transactionDAO.findByType(type);
    }
    
    public List<Transaction> filtrerParMontant(BigDecimal montantMin, BigDecimal montantMax) {
        return transactionDAO.findByMontantRange(montantMin, montantMax);
    }
    
    public List<Transaction> filtrerParDate(LocalDateTime dateDebut, LocalDateTime dateFin) {
        return transactionDAO.findByDateRange(dateDebut, dateFin);
    }
    
    public List<Transaction> filtrerParLieu(String lieu) {
        return transactionDAO.findByLieu(lieu);
    }
    
    
    public Map<TypeTransaction, List<Transaction>> regrouperParType() {
        return transactionDAO.findAll()
                .stream()
                .collect(Collectors.groupingBy(Transaction::type));
    }
    
    
    public Map<YearMonth, List<Transaction>> regrouperParMois() {
        return transactionDAO.findAll()
                .stream()
                .collect(Collectors.groupingBy(
                    t -> YearMonth.from(t.date())
                ));
    }
    
    
    public BigDecimal calculerMoyenneTransactionsClient(Long clientId) {
        List<Transaction> transactions = transactionDAO.findByClientId(clientId);
        if (transactions.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal somme = transactions.stream()
                .map(Transaction::montant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return somme.divide(BigDecimal.valueOf(transactions.size()), 2, RoundingMode.HALF_UP);
    }
    
    
    public BigDecimal calculerTotalTransactionsCompte(Long compteId) {
        return transactionDAO.findByCompteId(compteId)
                .stream()
                .map(Transaction::montant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    
    public List<Transaction> detecterTransactionsMontantEleve() {
        return transactionDAO.findSuspiciousTransactions(SEUIL_MONTANT_SUSPECT);
    }
    
   
    public List<Transaction> detecterTransactionsLieuInhabituel() {
        return transactionDAO.findAll()
                .stream()
                .filter(t -> !t.lieu().toLowerCase().contains(PAYS_HABITUEL.toLowerCase()))
                .toList();
    }
    
    
    public List<Transaction> detecterTransactionsFrequenceExcessive(Long compteId) {
        return transactionDAO.findTransactionsInShortTime(compteId, MINUTES_SUSPICION);
    }
    
   
    public List<Transaction> detecterToutesTransactionsSuspectes() {
        var montantEleve = detecterTransactionsMontantEleve();
        var lieuInhabituel = detecterTransactionsLieuInhabituel();
        
        return transactionDAO.findAll()
                .stream()
                .filter(t -> montantEleve.contains(t) || lieuInhabituel.contains(t))
                .distinct()
                .toList();
    }
    
    
    public Map<TypeTransaction, Long> compterTransactionsParType(LocalDateTime dateDebut, LocalDateTime dateFin) {
        return transactionDAO.findByDateRange(dateDebut, dateFin)
                .stream()
                .collect(Collectors.groupingBy(
                    Transaction::type,
                    Collectors.counting()
                ));
    }
    
    
    public Map<TypeTransaction, BigDecimal> calculerVolumeParType(LocalDateTime dateDebut, LocalDateTime dateFin) {
        return transactionDAO.findByDateRange(dateDebut, dateFin)
                .stream()
                .collect(Collectors.groupingBy(
                    Transaction::type,
                    Collectors.reducing(BigDecimal.ZERO, Transaction::montant, BigDecimal::add)
                ));
    }
    
    private void mettreAJourSoldeCompte(Long compteId, BigDecimal montant, TypeTransaction type) {
        var compteOpt = compteService.rechercherParId(compteId);
        if (compteOpt.isPresent()) {
            var compte = compteOpt.get();
            BigDecimal nouveauSolde = switch (type) {
                case VERSEMENT -> compte.getSolde().add(montant);
                case RETRAIT -> compte.getSolde().subtract(montant);
                case VIREMENT -> compte.getSolde().subtract(montant); 
            };
            compteService.mettreAJourSolde(compteId, nouveauSolde);
        }
    }
}