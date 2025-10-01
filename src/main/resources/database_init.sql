-- Table des clients
CREATE TABLE client (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Table des comptes
CREATE TABLE compte (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero VARCHAR(20) NOT NULL UNIQUE,
    solde DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    id_client BIGINT NOT NULL,
    type_compte ENUM('COURANT', 'EPARGNE') NOT NULL,
    decouvert_autorise DECIMAL(15,2) NULL,
    taux_interet DECIMAL(5,2) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (id_client) REFERENCES client(id) ON DELETE RESTRICT,
    INDEX idx_client (id_client),
    INDEX idx_numero (numero),
    INDEX idx_type (type_compte)
);

-- Table des transactions
CREATE TABLE transaction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    montant DECIMAL(15,2) NOT NULL,
    type ENUM('VERSEMENT', 'RETRAIT', 'VIREMENT') NOT NULL,
    lieu VARCHAR(200),
    id_compte BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (id_compte) REFERENCES compte(id) ON DELETE RESTRICT,
    INDEX idx_compte (id_compte),
    INDEX idx_date (date),
    INDEX idx_type (type),
    INDEX idx_montant (montant),
    INDEX idx_lieu (lieu)
);
