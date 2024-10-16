INSERT INTO users (email, password)
VALUES
    ('pjhcsols@naver.com', '$2a$10$ENYqGvZ3p6LvtsBnRWINSOJHKlMt1Ykgb3.jCnoKkrhMihviXhkDu'),
    ('exampleuser@example.com', '$2a$10$EXAMPLEHASHFORUSERPASSWORD');

INSERT INTO wallets (user_id, pdfUrl,privateKey, publicKey, address)
VALUES
    (1, 'https://web3credentialbucket.s3.ap-northeast-2.amazonaws.com/ssa_1727764703278_ssa','privateKeyForUser1', 'publicKeyForUser1', 'addressForUser1'),
    (2,null,'privateKeyForUser2', 'publicKeyForUser2', 'addressForUser2');