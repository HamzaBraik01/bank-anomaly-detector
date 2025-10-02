package com.solubank.service;

import com.solubank.dao.ClientDAO;
import com.solubank.dao.CompteDAO;
import com.solubank.entity.Client;
import com.solubank.entity.Compte;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


public class ClientService {
    private final ClientDAO clientDAO;
    private final CompteDAO compteDAO;
    
    public ClientService() {
        this.clientDAO = new ClientDAO();
        this.compteDAO = new CompteDAO();
    }
    
    public Optional<Client> ajouterClient(String nom, String email) {
        try {
            Client client = new Client(null, nom, email);
            return clientDAO.save(client);
        } catch (IllegalArgumentException e) {
            System.err.println("Erreur de validation: " + e.getMessage());
            return Optional.empty();
        }
    }
    
    public boolean modifierClient(Long id, String nom, String email) {
        try {
            Client client = new Client(id, nom, email);
            return clientDAO.update(client);
        } catch (IllegalArgumentException e) {
            System.err.println("Erreur de validation: " + e.getMessage());
            return false;
        }
    }
    
    public boolean supprimerClient(Long id) {
        List<Compte> comptes = compteDAO.findByClientId(id);
        if (!comptes.isEmpty()) {
            System.err.println("Impossible de supprimer le client. Il a encore des comptes associés.");
            return false;
        }
        return clientDAO.delete(id);
    }
    
    public Optional<Client> rechercherParId(Long id) {
        return clientDAO.findById(id);
    }
    
    public List<Client> rechercherParNom(String nom) {
        return clientDAO.findByNom(nom);
    }
    
    public List<Client> listerTousLesClients() {
        return clientDAO.findAll();
    }
    
    
    public BigDecimal calculerSoldeTotal(Long clientId) {
        return compteDAO.findByClientId(clientId)
                .stream()
                .map(Compte::getSolde)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    
    public long compterComptes(Long clientId) {
        return compteDAO.findByClientId(clientId).size();
    }
    
    
    public boolean clientExiste(Long clientId) {
        return clientDAO.findById(clientId).isPresent();
    }
}