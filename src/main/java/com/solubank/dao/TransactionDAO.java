package com.solubank.dao;

import com.solubank.entity.Transaction;
import com.solubank.entity.TypeTransaction;
import com.solubank.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class TransactionDAO {
    
    public Optional<Transaction> save(Transaction transaction) {
        String sql = "INSERT INTO transaction (date, montant, type, lieu, id_compte) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(transaction.date()));
            stmt.setBigDecimal(2, transaction.montant());
            stmt.setString(3, transaction.type().name());
            stmt.setString(4, transaction.lieu());
            stmt.setLong(5, transaction.idCompte());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        Long id = generatedKeys.getLong(1);
                        return Optional.of(new Transaction(
                            id,
                            transaction.date(),
                            transaction.montant(),
                            transaction.type(),
                            transaction.lieu(),
                            transaction.idCompte()
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de la transaction: " + e.getMessage());
        }
        return Optional.empty();
    }
    
    public Optional<Transaction> findById(Long id) {
        String sql = "SELECT * FROM transaction WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(createTransactionFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche de la transaction: " + e.getMessage());
        }
        return Optional.empty();
    }
    
    public List<Transaction> findByCompteId(Long compteId) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transaction WHERE id_compte = ? ORDER BY date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, compteId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                transactions.add(createTransactionFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche des transactions: " + e.getMessage());
        }
        return transactions;
    }
    
    public List<Transaction> findByClientId(Long clientId) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = """
            SELECT t.* FROM transaction t
            JOIN compte c ON t.id_compte = c.id
            WHERE c.id_client = ?
            ORDER BY t.date DESC
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, clientId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                transactions.add(createTransactionFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche des transactions par client: " + e.getMessage());
        }
        return transactions;
    }
    
    public List<Transaction> findByType(TypeTransaction type) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transaction WHERE type = ? ORDER BY date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, type.name());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                transactions.add(createTransactionFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche des transactions par type: " + e.getMessage());
        }
        return transactions;
    }
    
    public List<Transaction> findByMontantRange(BigDecimal montantMin, BigDecimal montantMax) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transaction WHERE montant BETWEEN ? AND ? ORDER BY date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBigDecimal(1, montantMin);
            stmt.setBigDecimal(2, montantMax);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                transactions.add(createTransactionFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche des transactions par montant: " + e.getMessage());
        }
        return transactions;
    }
    
    public List<Transaction> findByDateRange(LocalDateTime dateDebut, LocalDateTime dateFin) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transaction WHERE date BETWEEN ? AND ? ORDER BY date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(dateDebut));
            stmt.setTimestamp(2, Timestamp.valueOf(dateFin));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                transactions.add(createTransactionFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche des transactions par date: " + e.getMessage());
        }
        return transactions;
    }
    
    public List<Transaction> findByLieu(String lieu) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transaction WHERE lieu LIKE ? ORDER BY date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + lieu + "%");
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                transactions.add(createTransactionFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche des transactions par lieu: " + e.getMessage());
        }
        return transactions;
    }
    
    public List<Transaction> findSuspiciousTransactions(BigDecimal seuil) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transaction WHERE montant > ? ORDER BY montant DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBigDecimal(1, seuil);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                transactions.add(createTransactionFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche des transactions suspectes: " + e.getMessage());
        }
        return transactions;
    }
    
    public List<Transaction> findTransactionsInShortTime(Long compteId, int minutes) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = """
            SELECT * FROM transaction 
            WHERE id_compte = ? 
            AND date >= DATE_SUB(NOW(), INTERVAL ? MINUTE)
            ORDER BY date DESC
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, compteId);
            stmt.setInt(2, minutes);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                transactions.add(createTransactionFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche des transactions rapprochées: " + e.getMessage());
        }
        return transactions;
    }
    
    public List<Transaction> findAll() {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transaction ORDER BY date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                transactions.add(createTransactionFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des transactions: " + e.getMessage());
        }
        return transactions;
    }
    
    public boolean delete(Long id) {
        String sql = "DELETE FROM transaction WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de la transaction: " + e.getMessage());
        }
        return false;
    }
    
    private Transaction createTransactionFromResultSet(ResultSet rs) throws SQLException {
        return new Transaction(
            rs.getLong("id"),
            rs.getTimestamp("date").toLocalDateTime(),
            rs.getBigDecimal("montant"),
            TypeTransaction.valueOf(rs.getString("type")),
            rs.getString("lieu"),
            rs.getLong("id_compte")
        );
    }
}