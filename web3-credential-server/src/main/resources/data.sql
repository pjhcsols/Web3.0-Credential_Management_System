INSERT INTO users (email, password)
VALUES
    ('pjhcsols@naver.com', '$2a$10$ENYqGvZ3p6LvtsBnRWINSOJHKlMt1Ykgb3.jCnoKkrhMihviXhkDu'),
    ('exampleuser@example.com', '$2a$10$EXAMPLEHASHFORUSERPASSWORD');

INSERT INTO wallets (user_id, privateKey, publicKey, address)
VALUES
    (1, 'privateKeyForUser1', 'publicKeyForUser1', 'addressForUser1'),
    (2, 'privateKeyForUser2', 'publicKeyForUser2', 'addressForUser2');