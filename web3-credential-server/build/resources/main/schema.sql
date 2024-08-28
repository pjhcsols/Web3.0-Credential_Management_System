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
                         pdfUrl VARCHAR(255),
                         privateKey VARCHAR(255) NOT NULL,
                         publicKey VARCHAR(255) NOT NULL,
                         address VARCHAR(255) NOT NULL,
                         FOREIGN KEY (user_id) REFERENCES users(id)
);

