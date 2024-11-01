INSERT INTO users (email, password)
VALUES
    ('pjhcsols@naver.com', '$2a$10$ENYqGvZ3p6LvtsBnRWINSOJHKlMt1Ykgb3.jCnoKkrhMihviXhkDu'),
    ('exampleuser@example.com', '$2a$10$EXAMPLEHASHFORUSERPASSWORD'),
    ('3751271433', '$2a$10$ENYqGvZ3p6LvtsBnRWINSOJHKlMt1Ykgb3.jCnoKkrhMihviXhkDu');

INSERT INTO wallets (user_id, private_key, public_key)
VALUES
    (1, 'privateKeyForUser1', 'publicKeyForUser1'),
    (2, 'privateKeyForUser2', 'publicKeyForUser2'),
    (3, 'privateKeyForUser3', 'publicKeyForUser3');

INSERT INTO wallet_pdf_urls (wallet_id, certificate_type, pdf_url)
VALUES
    (1, '재학증_1', 'https://basilium-product-bucket.s3.ap-northeast-2.amazonaws.com/1_student_certifications.pdf'),
    (1, '여권_1', 'https://s3.ap-northeast-2.amazonaws.com/basilium-product-bucket/1_passport_certification.pdf'),
    (2, '재학증_2', null);

