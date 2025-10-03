package com.solubank.service;

import com.solubank.entity.Client;
import com.solubank.entity.Compte;
import com.solubank.entity.Transaction;
import com.solubank.entity.TypeTransaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class RapportService {
    private final ClientService clientService;
    private final CompteService compteService;
    private final TransactionService transactionService;
    
    public RapportService() {
        this.clientService = new ClientService();
        this.compteService = new CompteService();
        this.transactionService = new TransactionService();
    }
    
    
    public String genererTop5ClientsParSolde() {
        List<Client> clients = clientService.listerTousLesClients();
        
        var topClients = clients.stream()
                .sorted((c1, c2) -> {
                    BigDecimal solde1 = clientService.calculerSoldeTotal(c1.id());
                    BigDecimal solde2 = clientService.calculerSoldeTotal(c2.id());
                    return solde2.compareTo(solde1);
                })
                .limit(5)
                .toList();
        
        StringBuilder rapport = new StringBuilder();
        rapport.append("=== TOP 5 DES CLIENTS PAR SOLDE TOTAL ===\n\n");
        
        int rang = 1;
        for (Client client : topClients) {
            BigDecimal soldeTotal = clientService.calculerSoldeTotal(client.id());
            long nombreComptes = clientService.compterComptes(client.id());
            
            rapport.append(String.format("%d. %s (%s)\n", rang++, client.nom(), client.email()));
            rapport.append(String.format("   Solde total: %s DH\n", soldeTotal));
            rapport.append(String.format("   Nombre de comptes: %d\n\n", nombreComptes));
        }
        
        return rapport.toString();
    }
    
    
    public String genererRapportMensuel(YearMonth mois) {
        LocalDateTime debutMois = mois.atDay(1).atStartOfDay();
        LocalDateTime finMois = mois.atEndOfMonth().atTime(23, 59, 59);
        
        Map<TypeTransaction, Long> comptesParType = transactionService.compterTransactionsParType(debutMois, finMois);
        Map<TypeTransaction, BigDecimal> volumesParType = transactionService.calculerVolumeParType(debutMois, finMois);
        
        StringBuilder rapport = new StringBuilder();
        rapport.append("=== RAPPORT MENSUEL - ").append(mois.format(DateTimeFormatter.ofPattern("MMMM yyyy")))
               .append(" ===\n\n");
        
        BigDecimal volumeTotal = BigDecimal.ZERO;
        long nombreTotal = 0;
        
        for (TypeTransaction type : TypeTransaction.values()) {
            long nombre = comptesParType.getOrDefault(type, 0L);
            BigDecimal volume = volumesParType.getOrDefault(type, BigDecimal.ZERO);
            
            rapport.append(String.format("%-12s: %d transactions pour un volume de %s DH\n", 
                    type.name(), nombre, volume));
            
            nombreTotal += nombre;
            volumeTotal = volumeTotal.add(volume);
        }
        
        rapport.append(String.format("\nTOTAL: %d transactions pour un volume de %s DH\n", 
                nombreTotal, volumeTotal));
        
        return rapport.toString();
    }
    
    
    public String detecterComptesInactifs(int joursInactivite) {
        LocalDateTime seuil = LocalDateTime.now().minusDays(joursInactivite);
        List<Compte> tousLesComptes = compteService.listerTousLesComptes();
        
        var comptesInactifs = tousLesComptes.stream()
                .filter(compte -> {
                    List<Transaction> transactions = transactionService.listerTransactionsParCompte(compte.getId());
                    return transactions.isEmpty() || 
                           transactions.stream().allMatch(t -> t.date().isBefore(seuil));
                })
                .toList();
        
        StringBuilder rapport = new StringBuilder();
        rapport.append("=== COMPTES INACTIFS (").append(joursInactivite).append(" jours) ===\n\n");
        
        if (comptesInactifs.isEmpty()) {
            rapport.append("Aucun compte inactif détecté.\n");
        } else {
            for (Compte compte : comptesInactifs) {
                var client = clientService.rechercherParId(compte.getIdClient());
                List<Transaction> transactions = transactionService.listerTransactionsParCompte(compte.getId());
                
                rapport.append(String.format("Compte: %s (%s)\n", compte.getNumero(), compte.getTypeCompte()));
                if (client.isPresent()) {
                    rapport.append(String.format("Client: %s\n", client.get().nom()));
                }
                rapport.append(String.format("Solde: %s DH\n", compte.getSolde()));
                
                if (transactions.isEmpty()) {
                    rapport.append("Aucune transaction enregistrée\n");
                } else {
                    var derniereTransaction = transactions.stream()
                            .max(Comparator.comparing(Transaction::date));
                    if (derniereTransaction.isPresent()) {
                        rapport.append(String.format("Dernière transaction: %s\n", 
                                derniereTransaction.get().date().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
                    }
                }
                rapport.append("\n");
            }
        }
        
        return rapport.toString();
    }
    
    
    public String genererRapportTransactionsSuspectes() {
        List<Transaction> transactionsSuspectes = transactionService.detecterToutesTransactionsSuspectes();
        
        StringBuilder rapport = new StringBuilder();
        rapport.append("=== TRANSACTIONS SUSPECTES ===\n\n");
        
        if (transactionsSuspectes.isEmpty()) {
            rapport.append("Aucune transaction suspecte détectée.\n");
        } else {
            var montantEleve = transactionService.detecterTransactionsMontantEleve();
            var lieuInhabituel = transactionService.detecterTransactionsLieuInhabituel();
            
            rapport.append("1. TRANSACTIONS AVEC MONTANT ÉLEVÉ (> 10 000 DH):\n");
            for (Transaction t : montantEleve) {
                rapport.append(String.format("   - %s: %s DH le %s à %s\n",
                        t.type().name(),
                        t.montant(),
                        t.date().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                        t.lieu()));
            }
            
            rapport.append("\n2. TRANSACTIONS DANS DES LIEUX INHABITUELS:\n");
            for (Transaction t : lieuInhabituel) {
                rapport.append(String.format("   - %s: %s DH le %s à %s\n",
                        t.type().name(),
                        t.montant(),
                        t.date().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                        t.lieu()));
            }
        }
        
        return rapport.toString();
    }
    
    
    public String genererAlertesComptesBasSolde(BigDecimal seuilSolde) {
        List<Compte> tousLesComptes = compteService.listerTousLesComptes();
        
        var comptesBasSolde = tousLesComptes.stream()
                .filter(compte -> compte.getSolde().compareTo(seuilSolde) < 0)
                .sorted(Comparator.comparing(Compte::getSolde))
                .toList();
        
        StringBuilder rapport = new StringBuilder();
        rapport.append("=== ALERTES SOLDES BAS (< ").append(seuilSolde).append(" DH) ===\n\n");
        
        if (comptesBasSolde.isEmpty()) {
            rapport.append("Aucun compte avec solde bas détecté.\n");
        } else {
            for (Compte compte : comptesBasSolde) {
                var client = clientService.rechercherParId(compte.getIdClient());
                
                rapport.append(String.format("⚠️  Compte: %s (%s)\n", compte.getNumero(), compte.getTypeCompte()));
                if (client.isPresent()) {
                    rapport.append(String.format("    Client: %s (%s)\n", client.get().nom(), client.get().email()));
                }
                rapport.append(String.format("    Solde actuel: %s DH\n\n", compte.getSolde()));
            }
        }
        
        return rapport.toString();
    }
    
    
    public String genererRapportStatistique() {
        List<Client> clients = clientService.listerTousLesClients();
        List<Compte> comptes = compteService.listerTousLesComptes();
        Map<TypeTransaction, List<Transaction>> transactionsParType = transactionService.regrouperParType();
        
        StringBuilder rapport = new StringBuilder();
        rapport.append("=== RAPPORT STATISTIQUE GLOBAL ===\n\n");
        
        rapport.append(String.format("Nombre total de clients: %d\n", clients.size()));
        rapport.append(String.format("Nombre total de comptes: %d\n", comptes.size()));
        
        long totalTransactions = transactionsParType.values().stream()
                .mapToLong(List::size)
                .sum();
        rapport.append(String.format("Nombre total de transactions: %d\n\n", totalTransactions));
        
        BigDecimal soldeMoyen = compteService.calculerSoldeMoyen();
        rapport.append(String.format("Solde moyen des comptes: %s DH\n", soldeMoyen));
        
        var compteMaxSolde = compteService.trouverCompteAvecSoldeMaximum();
        var compteMinSolde = compteService.trouverCompteAvecSoldeMinimum();
        
        if (compteMaxSolde.isPresent()) {
            rapport.append(String.format("Compte avec le solde maximum: %s (%s DH)\n", 
                    compteMaxSolde.get().getNumero(), compteMaxSolde.get().getSolde()));
        }
        
        if (compteMinSolde.isPresent()) {
            rapport.append(String.format("Compte avec le solde minimum: %s (%s €)\n", 
                    compteMinSolde.get().getNumero(), compteMinSolde.get().getSolde()));
        }
        
        rapport.append("\nRépartition des transactions par type:\n");
        for (var entry : transactionsParType.entrySet()) {
            rapport.append(String.format("- %s: %d transactions\n", 
                    entry.getKey().name(), entry.getValue().size()));
        }
        
        return rapport.toString();
    }
}