
INSERT INTO category (name, color, image_url, description)
VALUES
    ('교환권', '#FF5733', 'https://example.com/category1.jpg', 'Sample description for Category 1'),
    ('상품권', '#3366FF', 'https://example.com/category2.jpg', 'Sample description for Category 2');

INSERT INTO product (name, price, description, image_url, category_id)
VALUES
    ('Sample Product 1', 10000, 'Sample Description 1', 'c3JjL21haW4vcmVzb3VyY2VzL2ltYWdlU3RvcmFnZS90ZXN0LmpwZw', 1),
    ('Sample Product 2', 20000, 'Sample Description 2', 'c3JjL21haW4vcmVzb3VyY2VzL2ltYWdlU3RvcmFnZS90ZXN0LmpwZw', 1);

INSERT INTO users (email, password)
VALUES ('pjhcsols@naver.com', '$2a$10$ENYqGvZ3p6LvtsBnRWINSOJHKlMt1Ykgb3.jCnoKkrhMihviXhkDu');

INSERT INTO wishes (user_id, product_id, amount, is_deleted)
VALUES (1, 1, 5, FALSE);

INSERT INTO product_option (name, quantity, product_id)
VALUES
    ('Option 1', 1000, 1),
    ('Option 3', 4000, 1),
    ('Option 2', 2000, 2);

-- 포인트 테이블에 데이터 추가
INSERT INTO points (user_id, amount)
VALUES (1, 0);
/*
INSERT INTO product (name, price, description, image_url) VALUES
    ('Sample Product', 10000, 'Sample Description', 'c3JjL21haW4vcmVzb3VyY2VzL2ltYWdlU3RvcmFnZS90ZXN0LmpwZw');


INSERT INTO users (email, password) VALUES ('pjhcsols@naver.com', '$2a$10$ENYqGvZ3p6LvtsBnRWINSOJHKlMt1Ykgb3.jCnoKkrhMihviXhkDu'); -- password is '1q2w3e4r!'

INSERT INTO wishes (product_id, user_id, amount, is_deleted) VALUES (1, 1, 5, FALSE);
INSERT INTO wishes (product_id, user_id, amount, is_deleted) VALUES (2, 1, 3, FALSE);




 */