package com.solubank.ui;

import com.solubank.entity.Client;
import com.solubank.entity.Compte;
import com.solubank.entity.Transaction;
import com.solubank.entity.TypeTransaction;
import com.solubank.service.ClientService;
import com.solubank.service.CompteService;
import com.solubank.service.RapportService;
import com.solubank.service.TransactionService;
import com.solubank.util.DateUtil;
import com.solubank.util.ValidationUtil;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;


public class MenuPrincipal {
    
    private final Scanner scanner;
    private final ClientService clientService;
    private final CompteService compteService;
    private final TransactionService transactionService;
    private final RapportService rapportService;
    
    public MenuPrincipal() {
        this.scanner = new Scanner(System.in);
        this.clientService = new ClientService();
        this.compteService = new CompteService();
        this.transactionService = new TransactionService();
        this.rapportService = new RapportService();
    }
    
    public void demarrer() {
        System.out.println("==============================================");
        System.out.println("    BANQUE AL BARAKA - SYSTEME D'ANALYSE    ");
        System.out.println("         Detection d'Anomalies Bancaires     ");
        System.out.println("==============================================\n");
        
        boolean continuer = true;
        while (continuer) {
            afficherMenuPrincipal();
            int choix = lireChoix();
            
            switch (choix) {
                case 1 -> gererClients();
                case 2 -> gererComptes();
                case 3 -> gererTransactions();
                case 4 -> consulterHistorique();
                case 5 -> lancerAnalyses();
                case 6 -> recevoirAlertes();
                case 0 -> {
                    System.out.println("Merci d'avoir utilise le systeme Al Baraka. Au revoir !");
                    continuer = false;
                }
                default -> System.out.println("X Choix invalide. Veuillez reessayer.");
            }
        }
        
        scanner.close();
    }
    
    private void afficherMenuPrincipal() {
        System.out.println("\n>> MENU PRINCIPAL");
        System.out.println("1. [U] Gestion des clients");
        System.out.println("2. [$] Gestion des comptes");
        System.out.println("3. [T] Enregistrer une transaction");
        System.out.println("4. [H] Consulter l'historique des transactions");
        System.out.println("5. [A] Lancer une analyse");
        System.out.println("6. [!] Recevoir des alertes");
        System.out.println("0. [Q] Quitter");
        System.out.print("\nVotre choix: ");
    }
    
    private void gererClients() {
        System.out.println("\n[U] GESTION DES CLIENTS");
        System.out.println("1. Ajouter un client");
        System.out.println("2. Modifier un client");
        System.out.println("3. Supprimer un client");
        System.out.println("4. Rechercher un client");
        System.out.println("5. Lister tous les clients");
        System.out.println("0. Retour au menu principal");
        System.out.print("\nVotre choix: ");
        
        int choix = lireChoix();
        
        switch (choix) {
            case 1 -> ajouterClient();
            case 2 -> modifierClient();
            case 3 -> supprimerClient();
            case 4 -> rechercherClient();
            case 5 -> listerClients();
            case 0 -> { /* Retour */ }
            default -> System.out.println("X Choix invalide.");
        }
    }
    
    private void ajouterClient() {
        System.out.println("\n+ Ajouter un nouveau client");
        
        System.out.print("Nom: ");
        String nom = scanner.nextLine();
        
        if (!ValidationUtil.isValidNom(nom)) {
            System.out.println("X Nom invalide.");
            return;
        }
        
        System.out.print("Email: ");
        String email = scanner.nextLine();
        
        if (!ValidationUtil.isValidEmail(email)) {
            System.out.println("X Email invalide.");
            return;
        }
        
        Optional<Client> clientOpt = clientService.ajouterClient(nom, email);
        if (clientOpt.isPresent()) {
            Client client = clientOpt.get();
            System.out.printf("√ Client ajoute avec succes! ID: %d%n", client.id());
        } else {
            System.out.println("X Erreur lors de l'ajout du client.");
        }
    }
    
    private void modifierClient() {
        System.out.println("\n~ Modifier un client");
        
        System.out.print("ID du client a modifier: ");
        String idStr = scanner.nextLine();
        
        if (!ValidationUtil.isValidId(idStr)) {
            System.out.println("X ID invalide.");
            return;
        }
        
        Long id = ValidationUtil.parseId(idStr);
        Optional<Client> clientOpt = clientService.rechercherParId(id);
        
        if (clientOpt.isEmpty()) {
            System.out.println("X Client non trouve.");
            return;
        }
        
        Client client = clientOpt.get();
        System.out.printf("Client actuel: %s (%s)%n", client.nom(), client.email());
        
        System.out.print("Nouveau nom (Entree pour conserver): ");
        String nouveauNom = scanner.nextLine();
        if (nouveauNom.trim().isEmpty()) {
            nouveauNom = client.nom();
        }
        
        System.out.print("Nouvel email (Entree pour conserver): ");
        String nouvelEmail = scanner.nextLine();
        if (nouvelEmail.trim().isEmpty()) {
            nouvelEmail = client.email();
        }
        
        if (!ValidationUtil.isValidEmail(nouvelEmail)) {
            System.out.println("X Email invalide.");
            return;
        }
        
        if (clientService.modifierClient(id, nouveauNom, nouvelEmail)) {
            System.out.println("√ Client modifie avec succes!");
        } else {
            System.out.println("X Erreur lors de la modification.");
        }
    }
    
    private void supprimerClient() {
        System.out.println("\n- Supprimer un client");
        
        System.out.print("ID du client a supprimer: ");
        String idStr = scanner.nextLine();
        
        if (!ValidationUtil.isValidId(idStr)) {
            System.out.println("X ID invalide.");
            return;
        }
        
        Long id = ValidationUtil.parseId(idStr);
        
        if (clientService.supprimerClient(id)) {
            System.out.println("√ Client supprime avec succes!");
        } else {
            System.out.println("X Impossible de supprimer le client.");
        }
    }
    
    private void rechercherClient() {
        System.out.println("\n? Rechercher un client");
        System.out.println("1. Par ID");
        System.out.println("2. Par nom");
        System.out.print("Votre choix: ");
        
        int choix = lireChoix();
        
        switch (choix) {
            case 1 -> {
                System.out.print("ID du client: ");
                String idStr = scanner.nextLine();
                if (ValidationUtil.isValidId(idStr)) {
                    Long id = ValidationUtil.parseId(idStr);
                    Optional<Client> clientOpt = clientService.rechercherParId(id);
                    if (clientOpt.isPresent()) {
                        afficherClient(clientOpt.get());
                    } else {
                        System.out.println("X Client non trouve.");
                    }
                } else {
                    System.out.println("X ID invalide.");
                }
            }
            case 2 -> {
                System.out.print("Nom du client (ou partie): ");
                String nom = scanner.nextLine();
                List<Client> clients = clientService.rechercherParNom(nom);
                if (clients.isEmpty()) {
                    System.out.println("X Aucun client trouve.");
                } else {
                    System.out.printf("√ %d client(s) trouve(s):%n", clients.size());
                    clients.forEach(this::afficherClient);
                }
            }
            default -> System.out.println("X Choix invalide.");
        }
    }
    
    private void listerClients() {
        System.out.println("\n= Liste de tous les clients");
        List<Client> clients = clientService.listerTousLesClients();
        
        if (clients.isEmpty()) {
            System.out.println("Aucun client enregistre.");
        } else {
            System.out.printf("Total: %d clients%n%n", clients.size());
            clients.forEach(this::afficherClient);
        }
    }
    
    private void afficherClient(Client client) {
        BigDecimal soldeTotal = clientService.calculerSoldeTotal(client.id());
        long nombreComptes = clientService.compterComptes(client.id());
        
        System.out.printf("* ID: %d | Nom: %s | Email: %s%n", 
                client.id(), client.nom(), client.email());
        System.out.printf("   $ Solde total: %s DH | # Comptes: %d%n%n", 
                ValidationUtil.formatMontant(soldeTotal), nombreComptes);
    }
    
    private void gererComptes() {
        System.out.println("\n[$] GESTION DES COMPTES");
        System.out.println("1. Creer un compte courant");
        System.out.println("2. Creer un compte epargne");
        System.out.println("3. Consulter un compte");
        System.out.println("4. Lister les comptes d'un client");
        System.out.println("5. Mettre a jour un parametre de compte");
        System.out.println("0. Retour au menu principal");
        System.out.print("\nVotre choix: ");
        
        int choix = lireChoix();
        
        switch (choix) {
            case 1 -> creerCompteCourant();
            case 2 -> creerCompteEpargne();
            case 3 -> consulterCompte();
            case 4 -> listerComptesClient();
            case 5 -> mettreAJourCompte();
            case 0 -> { /* Retour */ }
            default -> System.out.println("X Choix invalide.");
        }
    }
    
    private void creerCompteCourant() {
        System.out.println("\n+ Creer un compte courant");
        
        System.out.print("ID du client: ");
        String clientIdStr = scanner.nextLine();
        
        if (!ValidationUtil.isValidId(clientIdStr)) {
            System.out.println("X ID client invalide.");
            return;
        }
        
        Long clientId = ValidationUtil.parseId(clientIdStr);
        
        if (!clientService.clientExiste(clientId)) {
            System.out.println("X Client inexistant.");
            return;
        }
        
        System.out.print("Solde initial: ");
        String soldeStr = scanner.nextLine();
        
        if (!ValidationUtil.isValidMontant(soldeStr)) {
            System.out.println("X Montant invalide.");
            return;
        }
        
        System.out.print("Decouvert autorise: ");
        String decouvertStr = scanner.nextLine();
        
        if (!ValidationUtil.isValidMontant(decouvertStr)) {
            System.out.println("X Montant invalide.");
            return;
        }
        
        BigDecimal solde = ValidationUtil.parseMontant(soldeStr);
        BigDecimal decouvert = ValidationUtil.parseMontant(decouvertStr);
        
        Optional<Compte> compteOpt = compteService.creerCompteCourant(clientId, solde, decouvert);
        if (compteOpt.isPresent()) {
            System.out.printf("√ Compte courant cree! Numero: %s%n", compteOpt.get().getNumero());
        } else {
            System.out.println("X Erreur lors de la creation du compte.");
        }
    }
    
    private void creerCompteEpargne() {
        System.out.println("\n+ Creer un compte epargne");
        
        System.out.print("ID du client: ");
        String clientIdStr = scanner.nextLine();
        
        if (!ValidationUtil.isValidId(clientIdStr)) {
            System.out.println("X ID client invalide.");
            return;
        }
        
        Long clientId = ValidationUtil.parseId(clientIdStr);
        
        if (!clientService.clientExiste(clientId)) {
            System.out.println("X Client inexistant.");
            return;
        }
        
        System.out.print("Solde initial: ");
        String soldeStr = scanner.nextLine();
        
        if (!ValidationUtil.isValidMontant(soldeStr)) {
            System.out.println("X Montant invalide.");
            return;
        }
        
        System.out.print("Taux d'interet (%): ");
        String tauxStr = scanner.nextLine();
        
        if (!ValidationUtil.isValidMontant(tauxStr)) {
            System.out.println("X Taux invalide.");
            return;
        }
        
        BigDecimal solde = ValidationUtil.parseMontant(soldeStr);
        BigDecimal taux = ValidationUtil.parseMontant(tauxStr);
        
        Optional<Compte> compteOpt = compteService.creerCompteEpargne(clientId, solde, taux);
        if (compteOpt.isPresent()) {
            System.out.printf("√ Compte epargne cree! Numero: %s%n", compteOpt.get().getNumero());
        } else {
            System.out.println("X Erreur lors de la creation du compte.");
        }
    }
    
    private void consulterCompte() {
        System.out.println("\n? Consulter un compte");
        
        System.out.print("Numero du compte: ");
        String numero = scanner.nextLine();
        
        Optional<Compte> compteOpt = compteService.rechercherParNumero(numero);
        if (compteOpt.isPresent()) {
            afficherCompte(compteOpt.get());
        } else {
            System.out.println("X Compte non trouve.");
        }
    }
    
    private void listerComptesClient() {
        System.out.println("\n= Lister les comptes d'un client");
        
        System.out.print("ID du client: ");
        String clientIdStr = scanner.nextLine();
        
        if (!ValidationUtil.isValidId(clientIdStr)) {
            System.out.println("X ID invalide.");
            return;
        }
        
        Long clientId = ValidationUtil.parseId(clientIdStr);
        List<Compte> comptes = compteService.rechercherParClient(clientId);
        
        if (comptes.isEmpty()) {
            System.out.println("X Aucun compte trouve pour ce client.");
        } else {
            System.out.printf("√ %d compte(s) trouve(s):%n", comptes.size());
            comptes.forEach(this::afficherCompte);
        }
    }
    
    private void mettreAJourCompte() {
        System.out.println("\n~ Mettre a jour les parametres d'un compte");
        
        System.out.print("ID du compte: ");
        String compteIdStr = scanner.nextLine();
        
        if (!ValidationUtil.isValidId(compteIdStr)) {
            System.out.println("X ID invalide.");
            return;
        }
        
        Long compteId = ValidationUtil.parseId(compteIdStr);
        Optional<Compte> compteOpt = compteService.rechercherParId(compteId);
        
        if (compteOpt.isEmpty()) {
            System.out.println("X Compte non trouve.");
            return;
        }
        
        Compte compte = compteOpt.get();
        afficherCompte(compte);
        
        switch (compte.getTypeCompte()) {
            case "COURANT" -> {
                System.out.print("Nouveau decouvert autorise: ");
                String decouvertStr = scanner.nextLine();
                if (ValidationUtil.isValidMontant(decouvertStr)) {
                    BigDecimal decouvert = ValidationUtil.parseMontant(decouvertStr);
                    if (compteService.mettreAJourDecouvertAutorise(compteId, decouvert)) {
                        System.out.println("√ Decouvert mis a jour!");
                    } else {
                        System.out.println("X Erreur lors de la mise a jour.");
                    }
                } else {
                    System.out.println("X Montant invalide.");
                }
            }
            case "EPARGNE" -> {
                System.out.print("Nouveau taux d'interet (%): ");
                String tauxStr = scanner.nextLine();
                if (ValidationUtil.isValidMontant(tauxStr)) {
                    BigDecimal taux = ValidationUtil.parseMontant(tauxStr);
                    if (compteService.mettreAJourTauxInteret(compteId, taux)) {
                        System.out.println("√ Taux d'interet mis a jour!");
                    } else {
                        System.out.println("X Erreur lors de la mise a jour.");
                    }
                } else {
                    System.out.println("X Taux invalide.");
                }
            }
        }
    }
    
    private void afficherCompte(Compte compte) {
        Optional<Client> clientOpt = clientService.rechercherParId(compte.getIdClient());
        
        System.out.printf("$ Compte %s (%s)%n", compte.getNumero(), compte.getTypeCompte());
        if (clientOpt.isPresent()) {
            System.out.printf("   U Client: %s%n", clientOpt.get().nom());
        }
        System.out.printf("   $ Solde: %s DH%n", ValidationUtil.formatMontant(compte.getSolde()));
        
        switch (compte) {
            case com.solubank.entity.CompteCourant cc ->
                System.out.printf("   - Decouvert autorise: %s DH%n", 
                        ValidationUtil.formatMontant(cc.getDecouvertAutorise()));
            case com.solubank.entity.CompteEpargne ce ->
                System.out.printf("   + Taux d'interet: %s %%%n", 
                        ValidationUtil.formatMontant(ce.getTauxInteret()));
        }
        System.out.println();
    }
    
    private void gererTransactions() {
        System.out.println("\n[T] ENREGISTRER UNE TRANSACTION");
        System.out.println("1. Versement");
        System.out.println("2. Retrait");
        System.out.println("3. Virement");
        System.out.println("0. Retour au menu principal");
        System.out.print("\nVotre choix: ");
        
        int choix = lireChoix();
        
        switch (choix) {
            case 1 -> enregistrerTransaction(TypeTransaction.VERSEMENT);
            case 2 -> enregistrerTransaction(TypeTransaction.RETRAIT);
            case 3 -> enregistrerTransaction(TypeTransaction.VIREMENT);
            case 0 -> { /* Retour */ }
            default -> System.out.println("X Choix invalide.");
        }
    }
    
    private void enregistrerTransaction(TypeTransaction type) {
        System.out.printf("\n$ Enregistrer un %s%n", type.name().toLowerCase());
        
        System.out.print("ID du compte: ");
        String compteIdStr = scanner.nextLine();
        
        if (!ValidationUtil.isValidId(compteIdStr)) {
            System.out.println("X ID compte invalide.");
            return;
        }
        
        Long compteId = ValidationUtil.parseId(compteIdStr);
        
        Optional<Compte> compteOpt = compteService.rechercherParId(compteId);
        if (compteOpt.isEmpty()) {
            System.out.println("X Compte inexistant.");
            return;
        }
        
        System.out.print("Montant: ");
        String montantStr = scanner.nextLine();
        
        if (!ValidationUtil.isValidMontant(montantStr)) {
            System.out.println("X Montant invalide.");
            return;
        }
        
        System.out.print("Lieu: ");
        String lieu = scanner.nextLine();
        
        if (lieu.trim().isEmpty()) {
            lieu = "Non specifie";
        }
        
        BigDecimal montant = ValidationUtil.parseMontant(montantStr);
        
        Optional<Transaction> transactionOpt = transactionService.enregistrerTransaction(montant, type, lieu, compteId);
        if (transactionOpt.isPresent()) {
            System.out.printf("√ %s enregistre avec succes! ID: %d%n", 
                    type.name(), transactionOpt.get().id());
            
            Optional<Compte> compteUpdated = compteService.rechercherParId(compteId);
            if (compteUpdated.isPresent()) {
                System.out.printf("$ Nouveau solde: %s DH%n", 
                        ValidationUtil.formatMontant(compteUpdated.get().getSolde()));
            }
        } else {
            System.out.println("X Erreur lors de l'enregistrement de la transaction.");
        }
    }
    
    private void consulterHistorique() {
        System.out.println("\n[H] CONSULTER L'HISTORIQUE DES TRANSACTIONS");
        System.out.println("1. Historique d'un compte");
        System.out.println("2. Historique d'un client");
        System.out.println("3. Filtrer par type de transaction");
        System.out.println("4. Filtrer par montant");
        System.out.println("5. Filtrer par lieu");
        System.out.println("0. Retour au menu principal");
        System.out.print("\nVotre choix: ");
        
        int choix = lireChoix();
        
        switch (choix) {
            case 1 -> consulterHistoriqueCompte();
            case 2 -> consulterHistoriqueClient();
            case 3 -> filtrerParType();
            case 4 -> filtrerParMontant();
            case 5 -> filtrerParLieu();
            case 0 -> { /* Retour */ }
            default -> System.out.println("X Choix invalide.");
        }
    }
    
    private void consulterHistoriqueCompte() {
        System.out.println("\n= Historique d'un compte");
        
        System.out.print("ID du compte: ");
        String compteIdStr = scanner.nextLine();
        
        if (!ValidationUtil.isValidId(compteIdStr)) {
            System.out.println("X ID invalide.");
            return;
        }
        
        Long compteId = ValidationUtil.parseId(compteIdStr);
        List<Transaction> transactions = transactionService.listerTransactionsParCompte(compteId);
        
        if (transactions.isEmpty()) {
            System.out.println("X Aucune transaction trouvee pour ce compte.");
        } else {
            System.out.printf("√ %d transaction(s) trouvee(s):%n%n", transactions.size());
            transactions.forEach(this::afficherTransaction);
        }
    }
    
    private void consulterHistoriqueClient() {
        System.out.println("\n= Historique d'un client");
        
        System.out.print("ID du client: ");
        String clientIdStr = scanner.nextLine();
        
        if (!ValidationUtil.isValidId(clientIdStr)) {
            System.out.println("X ID invalide.");
            return;
        }
        
        Long clientId = ValidationUtil.parseId(clientIdStr);
        List<Transaction> transactions = transactionService.listerTransactionsParClient(clientId);
        
        if (transactions.isEmpty()) {
            System.out.println("X Aucune transaction trouvee pour ce client.");
        } else {
            System.out.printf("√ %d transaction(s) trouvee(s):%n%n", transactions.size());
            transactions.forEach(this::afficherTransaction);
        }
    }
    
    private void filtrerParType() {
        System.out.println("\n? Filtrer par type de transaction");
        System.out.println("1. VERSEMENT");
        System.out.println("2. RETRAIT");
        System.out.println("3. VIREMENT");
        System.out.print("Votre choix: ");
        
        int choix = lireChoix();
        TypeTransaction type = switch (choix) {
            case 1 -> TypeTransaction.VERSEMENT;
            case 2 -> TypeTransaction.RETRAIT;
            case 3 -> TypeTransaction.VIREMENT;
            default -> null;
        };
        
        if (type == null) {
            System.out.println("X Choix invalide.");
            return;
        }
        
        List<Transaction> transactions = transactionService.filtrerParType(type);
        if (transactions.isEmpty()) {
            System.out.printf("X Aucune transaction de type %s trouvee.%n", type.name());
        } else {
            System.out.printf("√ %d transaction(s) de type %s:%n%n", transactions.size(), type.name());
            transactions.forEach(this::afficherTransaction);
        }
    }
    
    private void filtrerParMontant() {
        System.out.println("\n? Filtrer par montant");
        
        System.out.print("Montant minimum: ");
        String minStr = scanner.nextLine();
        
        System.out.print("Montant maximum: ");
        String maxStr = scanner.nextLine();
        
        if (!ValidationUtil.isValidMontant(minStr) || !ValidationUtil.isValidMontant(maxStr)) {
            System.out.println("X Montants invalides.");
            return;
        }
        
        BigDecimal min = ValidationUtil.parseMontant(minStr);
        BigDecimal max = ValidationUtil.parseMontant(maxStr);
        
        List<Transaction> transactions = transactionService.filtrerParMontant(min, max);
        if (transactions.isEmpty()) {
            System.out.printf("X Aucune transaction entre %s DH et %s DH trouvee.%n", min, max);
        } else {
            System.out.printf("√ %d transaction(s) entre %s DH et %s DH:%n%n", 
                    transactions.size(), min, max);
            transactions.forEach(this::afficherTransaction);
        }
    }
    
    private void filtrerParLieu() {
        System.out.println("\n? Filtrer par lieu");
        
        System.out.print("Lieu (ou partie du lieu): ");
        String lieu = scanner.nextLine();
        
        List<Transaction> transactions = transactionService.filtrerParLieu(lieu);
        if (transactions.isEmpty()) {
            System.out.printf("X Aucune transaction a '%s' trouvee.%n", lieu);
        } else {
            System.out.printf("√ %d transaction(s) a '%s':%n%n", transactions.size(), lieu);
            transactions.forEach(this::afficherTransaction);
        }
    }
    
    private void lancerAnalyses() {
        System.out.println("\n[A] LANCER UNE ANALYSE");
        System.out.println("1. Top 5 des clients par solde");
        System.out.println("2. Rapport mensuel des transactions");
        System.out.println("3. Detecter les comptes inactifs");
        System.out.println("4. Detecter les transactions suspectes");
        System.out.println("5. Rapport statistique global");
        System.out.println("0. Retour au menu principal");
        System.out.print("\nVotre choix: ");
        
        int choix = lireChoix();
        
        switch (choix) {
            case 1 -> afficherTop5Clients();
            case 2 -> afficherRapportMensuel();
            case 3 -> detecterComptesInactifs();
            case 4 -> detecterTransactionsSuspectes();
            case 5 -> afficherRapportStatistique();
            case 0 -> { /* Retour */ }
            default -> System.out.println("X Choix invalide.");
        }
    }
    
    private void afficherTop5Clients() {
        String rapport = rapportService.genererTop5ClientsParSolde();
        System.out.println("\n" + rapport);
    }
    
    private void afficherRapportMensuel() {
        System.out.println("\n@ Rapport mensuel");
        System.out.print("Mois (MM/YYYY): ");
        String moisStr = scanner.nextLine();
        
        Optional<YearMonth> moisOpt = DateUtil.parseMonth(moisStr);
        if (moisOpt.isEmpty()) {
            System.out.println("X Format de mois invalide. Utilisez MM/YYYY (ex: 09/2025)");
            return;
        }
        
        String rapport = rapportService.genererRapportMensuel(moisOpt.get());
        System.out.println("\n" + rapport);
    }
    
    private void detecterComptesInactifs() {
        System.out.println("\n~ Detecter les comptes inactifs");
        System.out.print("Nombre de jours d'inactivite (defaut: 30): ");
        String joursStr = scanner.nextLine();
        
        int jours = 30;
        if (!joursStr.trim().isEmpty()) {
            try {
                jours = Integer.parseInt(joursStr);
            } catch (NumberFormatException e) {
                System.out.println("X Nombre de jours invalide, utilisation de 30 par defaut.");
            }
        }
        
        String rapport = rapportService.detecterComptesInactifs(jours);
        System.out.println("\n" + rapport);
    }
    
    private void detecterTransactionsSuspectes() {
        String rapport = rapportService.genererRapportTransactionsSuspectes();
        System.out.println("\n" + rapport);
    }
    
    private void afficherRapportStatistique() {
        String rapport = rapportService.genererRapportStatistique();
        System.out.println("\n" + rapport);
    }
    
    private void recevoirAlertes() {
        System.out.println("\n[!] RECEVOIR DES ALERTES");
        System.out.println("1. Alertes soldes bas");
        System.out.println("2. Alertes comptes inactifs");
        System.out.println("3. Alertes transactions suspectes");
        System.out.println("0. Retour au menu principal");
        System.out.print("\nVotre choix: ");
        
        int choix = lireChoix();
        
        switch (choix) {
            case 1 -> alertesSoldesbas();
            case 2 -> alertesComptesInactifs();
            case 3 -> alertesTransactionsSuspectes();
            case 0 -> { /* Retour */ }
            default -> System.out.println("X Choix invalide.");
        }
    }
    
    private void alertesSoldesbas() {
        System.out.println("\n! Alertes soldes bas");
        System.out.print("Seuil de solde bas (defaut: 100 DH): ");
        String seuilStr = scanner.nextLine();
        
        BigDecimal seuil = new BigDecimal("100");
        if (!seuilStr.trim().isEmpty() && ValidationUtil.isValidMontant(seuilStr)) {
            seuil = ValidationUtil.parseMontant(seuilStr);
        }
        
        String rapport = rapportService.genererAlertesComptesBasSolde(seuil);
        System.out.println("\n" + rapport);
    }
    
    private void alertesComptesInactifs() {
        System.out.println("\n! Alertes comptes inactifs");
        System.out.print("Nombre de jours d'inactivite (defaut: 90): ");
        String joursStr = scanner.nextLine();
        
        int jours = 90;
        if (!joursStr.trim().isEmpty()) {
            try {
                jours = Integer.parseInt(joursStr);
            } catch (NumberFormatException e) {
                System.out.println("X Nombre de jours invalide, utilisation de 90 par defaut.");
            }
        }
        
        String rapport = rapportService.detecterComptesInactifs(jours);
        System.out.println("\n" + rapport);
    }
    
    private void alertesTransactionsSuspectes() {
        String rapport = rapportService.genererRapportTransactionsSuspectes();
        System.out.println("\n" + rapport);
    }
    
    private void afficherTransaction(Transaction transaction) {
        System.out.printf("$ %s | %s DH | %s | %s%n",
                transaction.type().name(),
                ValidationUtil.formatMontant(transaction.montant()),
                DateUtil.formatDateTime(transaction.date()),
                transaction.lieu());
    }
    
    private int lireChoix() {
        try {
            String input = scanner.nextLine();
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}