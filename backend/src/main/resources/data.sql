-- Store
INSERT INTO store (id, store_code, name) VALUES (1, 'STORE1', '맛있는 식당') ON CONFLICT DO NOTHING;

-- Admin (password: admin123 → bcrypt hash)
INSERT INTO admin (id, store_id, username, password_hash) VALUES
(1, 1, 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy')
ON CONFLICT DO NOTHING;

-- Tables (password: 1234 → bcrypt hash)
INSERT INTO store_table (id, store_id, table_number, password_hash) VALUES
(1, 1, 1, '$2a$10$EqKcp1WFKs7IFOHU8.tLPeEx1FnQIOnMb./4.U0cAJZHs.mMYWMbS'),
(2, 1, 2, '$2a$10$EqKcp1WFKs7IFOHU8.tLPeEx1FnQIOnMb./4.U0cAJZHs.mMYWMbS'),
(3, 1, 3, '$2a$10$EqKcp1WFKs7IFOHU8.tLPeEx1FnQIOnMb./4.U0cAJZHs.mMYWMbS'),
(4, 1, 4, '$2a$10$EqKcp1WFKs7IFOHU8.tLPeEx1FnQIOnMb./4.U0cAJZHs.mMYWMbS'),
(5, 1, 5, '$2a$10$EqKcp1WFKs7IFOHU8.tLPeEx1FnQIOnMb./4.U0cAJZHs.mMYWMbS')
ON CONFLICT DO NOTHING;

-- Menus
INSERT INTO menu (id, store_id, name, price, description, category, image_url, spicy_level, display_order) VALUES
(1, 1, '김치찌개', 9000, '돼지고기와 묵은지로 끓인 김치찌개', '찌개', 'https://via.placeholder.com/300x200?text=Kimchi+Jjigae', '매움', 1),
(2, 1, '된장찌개', 8000, '두부와 야채가 들어간 된장찌개', '찌개', 'https://via.placeholder.com/300x200?text=Doenjang+Jjigae', '안매움', 2),
(3, 1, '제육볶음', 11000, '매콤한 돼지고기 볶음', '볶음', 'https://via.placeholder.com/300x200?text=Jeyuk+Bokkeum', '매움', 3),
(4, 1, '비빔밥', 9500, '각종 나물과 고추장 비빔밥', '밥', 'https://via.placeholder.com/300x200?text=Bibimbap', '약간매움', 4),
(5, 1, '불고기', 13000, '달콤한 양념 소불고기', '구이', 'https://via.placeholder.com/300x200?text=Bulgogi', '안매움', 5),
(6, 1, '떡볶이', 7000, '매콤달콤 떡볶이', '분식', 'https://via.placeholder.com/300x200?text=Tteokbokki', '매움', 6),
(7, 1, '순두부찌개', 8500, '부드러운 순두부찌개', '찌개', 'https://via.placeholder.com/300x200?text=Sundubu', '아주매움', 7),
(8, 1, '공기밥', 1000, '흰 쌀밥', '밥', 'https://via.placeholder.com/300x200?text=Rice', NULL, 8),
(9, 1, '콜라', 2000, '코카콜라 355ml', '음료', 'https://via.placeholder.com/300x200?text=Cola', NULL, 9),
(10, 1, '사이다', 2000, '칠성사이다 355ml', '음료', 'https://via.placeholder.com/300x200?text=Cider', NULL, 10)
ON CONFLICT DO NOTHING;

-- Spicy options for spicy menus
INSERT INTO menu_spicy_option (id, menu_id, option_name, display_order) VALUES
(1, 1, '순한맛', 1), (2, 1, '보통', 2), (3, 1, '매운맛', 3), (4, 1, '아주매운맛', 4),
(5, 3, '순한맛', 1), (6, 3, '보통', 2), (7, 3, '매운맛', 3), (8, 3, '아주매운맛', 4),
(9, 6, '순한맛', 1), (10, 6, '보통', 2), (11, 6, '매운맛', 3), (12, 6, '아주매운맛', 4),
(13, 7, '순한맛', 1), (14, 7, '보통', 2), (15, 7, '매운맛', 3), (16, 7, '아주매운맛', 4)
ON CONFLICT DO NOTHING;

-- Reset sequences
SELECT setval('store_id_seq', (SELECT COALESCE(MAX(id), 0) FROM store));
SELECT setval('admin_id_seq', (SELECT COALESCE(MAX(id), 0) FROM admin));
SELECT setval('store_table_id_seq', (SELECT COALESCE(MAX(id), 0) FROM store_table));
SELECT setval('menu_id_seq', (SELECT COALESCE(MAX(id), 0) FROM menu));
SELECT setval('menu_spicy_option_id_seq', (SELECT COALESCE(MAX(id), 0) FROM menu_spicy_option));
