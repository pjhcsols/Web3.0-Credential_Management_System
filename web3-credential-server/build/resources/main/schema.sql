DROP TABLE IF EXISTS wallet_pdf_urls;
DROP TABLE IF EXISTS wallet_pdf_hash;
DROP TABLE IF EXISTS wallets;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL
);

CREATE TABLE wallets (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         user_id BIGINT NOT NULL,
                         private_key TEXT NOT NULL,  -- TEXT로 변경
                         public_key TEXT NOT NULL,  -- TEXT로 변경
                         FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE wallet_pdf_urls (
                                 wallet_id BIGINT,
                                 certificate_type VARCHAR(255),
                                 pdf_url VARCHAR(255),
                                 PRIMARY KEY (wallet_id, certificate_type),
                                 FOREIGN KEY (wallet_id) REFERENCES wallets(id)
);

CREATE TABLE wallet_pdf_hash (
                                 wallet_id BIGINT,
                                 certificate_type VARCHAR(255),
                                 pdf_hash VARCHAR(255),
                                 PRIMARY KEY (wallet_id, certificate_type),
                                 FOREIGN KEY (wallet_id) REFERENCES wallets(id)
);
