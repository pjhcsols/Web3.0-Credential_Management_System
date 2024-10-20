INSERT INTO users (email, password)
VALUES
    ('pjhcsols@naver.com', '$2a$10$ENYqGvZ3p6LvtsBnRWINSOJHKlMt1Ykgb3.jCnoKkrhMihviXhkDu'),
    ('exampleuser@example.com', '$2a$10$EXAMPLEHASHFORUSERPASSWORD'),
    ('3751271433', '$2a$10$skVt4kLn.95UGnrasmxQku5AGhhg6fESNtg/Ndw3iBQin.SqHrIdK');


INSERT INTO wallets (user_id, pdfUrl,privateKey, publicKey)
VALUES
    (1,'https://basilium-product-bucket.s3.ap-northeast-2.amazonaws.com/1_certifications.pdf','privateKeyForUser1', 'publicKeyForUser1'),
    (2,null,'privateKeyForUser2', 'publicKeyForUser2'),
    (3,null,'privateKeyForUser2', 'publicKeyForUser2');


