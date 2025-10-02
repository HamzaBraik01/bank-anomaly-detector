package com.solubank.dao;

import com.solubank.entity.Compte;
import com.solubank.entity.CompteCourant;
import com.solubank.entity.CompteEpargne;
import com.solubank.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class CompteDAO {
    
    public Optional<Compte> save(Compte compte) {
        String sql = "INSERT INTO compte (numero, solde, id_client, type_compte, decouvert_autorise, taux_interet) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, compte.getNumero());
            stmt.setBigDecimal(2, compte.getSolde());
            stmt.setLong(3, compte.getIdClient());
            stmt.setString(4, compte.getTypeCompte());
            
            if (compte instanceof CompteCourant cc) {
                stmt.setBigDecimal(5, cc.getDecouvertAutorise());
                stmt.setNull(6, Types.DECIMAL);
            } else if (compte instanceof CompteEpargne ce) {
                stmt.setNull(5, Types.DECIMAL);
                stmt.setBigDecimal(6, ce.getTauxInteret());
            }
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        Long id = generatedKeys.getLong(1);
                        return Optional.of(createCompteFromResultSet(id, compte));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du compte: " + e.getMessage());
        }
        return Optional.empty();
    }
    
    public Optional<Compte> findById(Long id) {
        String sql = "SELECT * FROM compte WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(createCompteFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche du compte: " + e.getMessage());
        }
        return Optional.empty();
    }
    
    public Optional<Compte> findByNumero(String numero) {
        String sql = "SELECT * FROM compte WHERE numero = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, numero);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(createCompteFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche du compte: " + e.getMessage());
        }
        return Optional.empty();
    }
    
    public List<Compte> findByClientId(Long clientId) {
        List<Compte> comptes = new ArrayList<>();
        String sql = "SELECT * FROM compte WHERE id_client = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, clientId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                comptes.add(createCompteFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche des comptes: " + e.getMessage());
        }
        return comptes;
    }
    
    public List<Compte> findAll() {
        List<Compte> comptes = new ArrayList<>();
        String sql = "SELECT * FROM compte ORDER BY numero";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                comptes.add(createCompteFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des comptes: " + e.getMessage());
        }
        return comptes;
    }
    
    public boolean updateSolde(Long compteId, BigDecimal nouveauSolde) {
        String sql = "UPDATE compte SET solde = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBigDecimal(1, nouveauSolde);
            stmt.setLong(2, compteId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du solde: " + e.getMessage());
        }
        return false;
    }
    
    public boolean delete(Long id) {
        String sql = "DELETE FROM compte WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du compte: " + e.getMessage());
        }
        return false;
    }
    
    public Optional<Compte> findCompteWithMaxSolde() {
        String sql = "SELECT * FROM compte ORDER BY solde DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return Optional.of(createCompteFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche du compte avec le solde maximum: " + e.getMessage());
        }
        return Optional.empty();
    }
    
    public Optional<Compte> findCompteWithMinSolde() {
        String sql = "SELECT * FROM compte ORDER BY solde ASC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return Optional.of(createCompteFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche du compte avec le solde minimum: " + e.getMessage());
        }
        return Optional.empty();
    }
    
    private Compte createCompteFromResultSet(ResultSet rs) throws SQLException {
        String typeCompte = rs.getString("type_compte");
        return switch (typeCompte) {
            case "COURANT" -> new CompteCourant(
                rs.getLong("id"),
                rs.getString("numero"),
                rs.getBigDecimal("solde"),
                rs.getLong("id_client"),
                rs.getBigDecimal("decouvert_autorise")
            );
            case "EPARGNE" -> new CompteEpargne(
                rs.getLong("id"),
                rs.getString("numero"),
                rs.getBigDecimal("solde"),
                rs.getLong("id_client"),
                rs.getBigDecimal("taux_interet")
            );
            default -> throw new IllegalArgumentException("Type de compte inconnu: " + typeCompte);
        };
    }
    
    private Compte createCompteFromResultSet(Long id, Compte compte) {
        return switch (compte.getTypeCompte()) {
            case "COURANT" -> new CompteCourant(
                id,
                compte.getNumero(),
                compte.getSolde(),
                compte.getIdClient(),
                ((CompteCourant) compte).getDecouvertAutorise()
            );
            case "EPARGNE" -> new CompteEpargne(
                id,
                compte.getNumero(),
                compte.getSolde(),
                compte.getIdClient(),
                ((CompteEpargne) compte).getTauxInteret()
            );
            default -> throw new IllegalArgumentException("Type de compte inconnu");
        };
    }
}