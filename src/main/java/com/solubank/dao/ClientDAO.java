package com.solubank.dao;

import com.solubank.entity.Client;
import com.solubank.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClientDAO {

    public Optional<Client> save(Client client) {
            String sql = "INSERT INTO client (nom, email) VALUES (?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                
                stmt.setString(1, client.nom());
                stmt.setString(2, client.email());
                
                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            Long id = generatedKeys.getLong(1);
                            return Optional.of(new Client(id, client.nom(), client.email()));
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println("Erreur lors de l'ajout du client: " + e.getMessage());
            }
            return Optional.empty();
    }

    public Optional<Client> findById(Long id) {
        String sql = "SELECT * FROM client WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(new Client(
                    rs.getLong("id"),
                    rs.getString("nom"),
                    rs.getString("email")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche du client: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Client> findByNom(String nom) {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT * FROM client WHERE nom LIKE ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + nom + "%");
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                clients.add(new Client(
                    rs.getLong("id"),
                    rs.getString("nom"),
                    rs.getString("email")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche des clients: " + e.getMessage());
        }
        return clients;
    }

    public List<Client> findAll() {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT * FROM client ORDER BY nom";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                clients.add(new Client(
                    rs.getLong("id"),
                    rs.getString("nom"),
                    rs.getString("email")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des clients: " + e.getMessage());
        }
        return clients;
    }

    public boolean update(Client client) {
        String sql = "UPDATE client SET nom = ?, email = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, client.nom());
            stmt.setString(2, client.email());
            stmt.setLong(3, client.id());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du client: " + e.getMessage());
        }
        return false;
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM client WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du client: " + e.getMessage());
        }
        return false;
    }
}