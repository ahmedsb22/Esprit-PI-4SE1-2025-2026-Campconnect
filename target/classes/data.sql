-- ============================================================
-- ROLES
-- ============================================================
INSERT INTO roles (name, permissions) VALUES
('ADMIN',  'ALL'),
('CAMPER', 'READ,BOOK'),
('OWNER',  'READ,MANAGE_SITE');

-- ============================================================
-- USERS  (password = 'admin123' BCrypt hashed)
-- ============================================================
INSERT INTO users (email, password, first_name, last_name, phone, address, created_at, updated_at) VALUES
('admin@campconnect.tn',   '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOnu', 'Admin',   'System',   '+216 71 000 001', 'Tunis, Tunisie',   NOW(), NOW()),
('mohamed@campconnect.tn', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOnu',  'Mohamed', 'Ben Ali',  '+216 22 111 222', 'Sousse, Tunisie',  NOW(), NOW()),
('fatma@campconnect.tn',   '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOnu',  'Fatma',   'Gharbi',   '+216 25 333 444', 'Sfax, Tunisie',    NOW(), NOW()),
('youssef@campconnect.tn', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOnu',  'Youssef', 'Hamdi',    '+216 98 555 666', 'Bizerte, Tunisie', NOW(), NOW()),
('leila@campconnect.tn',   '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOnu',  'Leila',   'Mansouri', '+216 55 777 888', 'Nabeul, Tunisie',  NOW(), NOW());

-- user_roles  (admin=1, camper=2, owner=3 | users: admin=1, mohamed=2, fatma=3, youssef=4, leila=5)
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1),  -- admin  -> ADMIN
(2, 2),  -- mohamed-> CAMPER
(3, 2),  -- fatma  -> CAMPER
(4, 3),  -- youssef-> OWNER
(5, 3);  -- leila  -> OWNER

-- ============================================================
-- CAMPING SITES  — exactly the 6 shown in the frontend
-- owner_id = 4 (youssef) and 5 (leila)
-- ============================================================
INSERT INTO camping_sites (name, description, location, address, price_per_night, capacity, category, image_url,
    has_wifi, has_parking, has_restrooms, has_showers, has_electricity, has_pet_friendly,
    is_active, is_verified, rating, review_count, owner_id, created_at, updated_at) VALUES

('Mountain View Campsite',
 'Nestled in the mountains with breathtaking views. Perfect for hiking and nature lovers.',
 'Ain Draham, North Tunisia', 'Route Forestiere, Ain Draham 8300',
 75.00, 20, 'MOUNTAIN',
 'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800',
 0, 1, 1, 1, 1, 0,   1, 1, 4.80, 127, 4, NOW(), NOW()),

('Coastal Paradise',
 'Beach camping at its finest. Wake up to the sound of waves and stunning sunrises.',
 'Hammamet, Coastal', 'Zone Touristique Hammamet Sud, Nabeul 8050',
 120.00, 30, 'BEACH',
 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800',
 1, 1, 1, 1, 1, 0,   1, 1, 4.90, 203, 4, NOW(), NOW()),

('Desert Oasis',
 'Experience the magic of the Sahara. Star gazing and traditional Bedouin experiences.',
 'Tozeur, South Tunisia', 'Oasis de Tozeur, Tozeur 2200',
 150.00, 10, 'DESERT',
 'https://images.unsplash.com/photo-1509316785289-025f5b846b35?w=800',
 0, 1, 1, 0, 0, 0,   1, 1, 5.00, 156, 5, NOW(), NOW()),

('Forest Retreat',
 'Peaceful forest setting perfect for families. Close to hiking trails and natural springs.',
 'Tabarka, North Tunisia', 'Route du Corail Tabarka, Jendouba 8110',
 65.00, 15, 'FOREST',
 'https://images.unsplash.com/photo-1537225228614-56cc3556d7ed?w=800',
 1, 1, 1, 1, 1, 1,   1, 1, 4.70, 89, 5, NOW(), NOW()),

('Lakeside Camping',
 'Camping by the lake with opportunities for fishing, kayaking, and bird watching.',
 'Ichkeul, North Tunisia', 'Parc National Ichkeul, Bizerte 7016',
 85.00, 12, 'NATURE',
 'https://images.unsplash.com/photo-1533240332313-0db49b459ad6?w=800',
 0, 0, 1, 1, 0, 1,   1, 1, 4.60, 74, 5, NOW(), NOW()),

('Glamping Luxury',
 'Luxury glamping tents with all amenities. Perfect for a comfortable outdoor experience.',
 'Sidi Bou Said, Coastal', 'Rue Hedi Zarrouk, Sidi Bou Said 2026',
 250.00, 8, 'GLAMPING',
 'https://images.unsplash.com/photo-1571863533956-01c88e79957e?w=800',
 1, 1, 1, 1, 1, 0,   1, 1, 5.00, 45, 4, NOW(), NOW()),

-- 4 extra sites for richness
('Cap Serrat Beach Camp',
 'Pristine beach camping on the northern coast with crystal clear waters and white sand.',
 'Cap Serrat, North Tunisia', 'Route de Cap Serrat, Sejnane 7014',
 45.00, 8, 'BEACH',
 'https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?w=800',
 1, 1, 1, 1, 1, 0,   1, 1, 4.80, 50, 4, NOW(), NOW()),

('Djerba Island Escape',
 'Exotic island camping with Mediterranean views and rich Berber cultural heritage.',
 'Djerba, South Tunisia', 'Zone Touristique, Djerba 4180',
 55.00, 12, 'BEACH',
 'https://images.unsplash.com/photo-1496080174650-637e3f22fa03?w=800',
 1, 1, 1, 1, 1, 0,   1, 1, 4.70, 60, 5, NOW(), NOW()),

('Zaghouan Mountain Spring',
 'High-altitude camping near the ancient Roman aqueduct and crystal mountain spring.',
 'Zaghouan, North Tunisia', 'Route du Djebel Zaghouan 1100',
 35.00, 6, 'MOUNTAIN',
 'https://images.unsplash.com/photo-1516912481808-3406841bd33c?w=800',
 0, 1, 1, 1, 0, 1,   1, 1, 4.50, 25, 5, NOW(), NOW()),

('Douz Desert Gateway',
 'Desert adventure camp at the edge of the Grand Erg Oriental sand dunes. Camel rides available.',
 'Douz, South Tunisia', 'Route du Desert, Douz 4260',
 65.00, 6, 'DESERT',
 'https://images.unsplash.com/photo-1548438294-1ad5d5f4f063?w=800',
 0, 1, 1, 0, 0, 0,   1, 1, 4.80, 55, 4, NOW(), NOW());

-- ============================================================
-- EQUIPMENT  (provider = fatma id=3)
-- ============================================================
INSERT INTO equipment (name, description, category, price_per_day, stock_quantity, available_quantity,
    image_url, specifications, is_active, rating, review_count, provider_id, created_at, updated_at) VALUES
('Family Tent 6 Person',
 'Spacious waterproof tent with easy setup. Perfect for family camping.',
 'TENTS', 35.00, 10, 10,
 'https://images.unsplash.com/photo-1478131143081-80f7f84ca84d?w=800', '6 persons, Waterproof',
 1, 4.50, 20, 3, NOW(), NOW()),

('Premium Sleeping Bag',
 'Warm and comfortable sleeping bag rated for 0°C to 15°C.',
 'SLEEPING_GEAR', 12.00, 20, 20,
 'https://images.unsplash.com/photo-1555375771-14b2a63968a0?w=800', '0-15°C, Compact',
 1, 4.60, 35, 3, NOW(), NOW()),

('Portable Camping Stove',
 'Compact gas stove with wind protection. Includes full cookware set.',
 'COOKING', 18.00, 15, 15,
 'https://images.unsplash.com/photo-1525811902-f2342640856e?w=800', 'Gas powered, Cookware included',
 1, 4.40, 28, 3, NOW(), NOW()),

('LED Camping Lantern',
 'Rechargeable LED lantern with 48-hour battery life. Adjustable brightness.',
 'LIGHTING', 8.00, 25, 25,
 'https://images.unsplash.com/photo-1478131143081-80f7f84ca84d?w=800', '48h battery, USB charge',
 1, 4.50, 15, 3, NOW(), NOW()),

('Hiking Backpack 70L',
 'Professional hiking backpack with ergonomic design and integrated rain cover.',
 'BAGS', 20.00, 12, 12,
 'https://images.unsplash.com/photo-1553361371-9b22f78e8b1d?w=800', '70L, Rain cover included',
 1, 4.70, 42, 3, NOW(), NOW()),

('Inflatable Kayak 2 Person',
 'Two-person inflatable kayak with paddles and full safety equipment included.',
 'WATER_SPORTS', 45.00, 5, 5,
 'https://images.unsplash.com/photo-1472745433479-4556f22e32c2?w=800', '2 person, Pump included',
 1, 4.80, 18, 3, NOW(), NOW()),

('Mountain Bike',
 '21-speed mountain bike suitable for all terrains. Helmet included.',
 'BIKES', 35.00, 8, 8,
 'https://images.unsplash.com/photo-1485965120184-e220f721d03e?w=800', '21-speed, Helmet included',
 1, 4.50, 22, 3, NOW(), NOW()),

('Camping Table & Chairs Set',
 'Foldable aluminum table with 4 chairs. Lightweight and easy to carry.',
 'FURNITURE', 15.00, 10, 10,
 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=800', '4 chairs, 5kg total',
 1, 4.30, 12, 3, NOW(), NOW()),

('Solar Charger 20W',
 '20W portable solar charger panel for phones and small devices. Dual USB.',
 'ELECTRONICS', 12.00, 15, 15,
 'https://images.unsplash.com/photo-1509391366360-2e959784a276?w=800', '20W, Dual USB, Foldable',
 1, 4.50, 30, 3, NOW(), NOW()),

('Snorkeling Set',
 'Full snorkeling set with anti-fog mask, fins, and waterproof dry bag.',
 'WATER_SPORTS', 22.00, 10, 10,
 'https://images.unsplash.com/photo-1559827260-dc66d52bef19?w=800', 'Anti-fog mask, Adjustable fins',
 1, 4.60, 25, 3, NOW(), NOW());

-- ============================================================
-- RESERVATIONS
-- camper: mohamed=2, fatma=3  |  sites: Mountain View=1, Coastal=2, Desert Oasis=3, Forest=4
-- ============================================================
INSERT INTO reservations (reservation_number, check_in_date, check_out_date, number_of_guests,
    total_price, status, camper_id, camping_site_id, created_at, updated_at) VALUES
('RES-001', DATE_ADD(CURDATE(), INTERVAL 5 DAY),  DATE_ADD(CURDATE(), INTERVAL 8 DAY),  4, 225.00,  'CONFIRMED',  2, 1, NOW(), NOW()),
('RES-002', DATE_ADD(CURDATE(), INTERVAL 10 DAY), DATE_ADD(CURDATE(), INTERVAL 14 DAY), 2, 480.00,  'PENDING',    3, 2, NOW(), NOW()),
('RES-003', DATE_SUB(CURDATE(), INTERVAL 10 DAY), DATE_SUB(CURDATE(), INTERVAL 7 DAY),  3, 450.00,  'COMPLETED',  2, 3, NOW(), NOW()),
('RES-004', DATE_ADD(CURDATE(), INTERVAL 3 DAY),  DATE_ADD(CURDATE(), INTERVAL 6 DAY),  2, 195.00,  'CANCELLED',  3, 4, NOW(), NOW()),
('RES-005', DATE_ADD(CURDATE(), INTERVAL 7 DAY),  DATE_ADD(CURDATE(), INTERVAL 10 DAY), 2, 255.00,  'CONFIRMED',  3, 5, NOW(), NOW()),
('RES-006', DATE_SUB(CURDATE(), INTERVAL 5 DAY),  DATE_SUB(CURDATE(), INTERVAL 3 DAY),  1, 500.00,  'COMPLETED',  2, 6, NOW(), NOW()),
('RES-007', DATE_ADD(CURDATE(), INTERVAL 15 DAY), DATE_ADD(CURDATE(), INTERVAL 18 DAY), 3, 135.00,  'PENDING',    3, 7, NOW(), NOW()),
('RES-008', DATE_SUB(CURDATE(), INTERVAL 2 DAY),  DATE_ADD(CURDATE(), INTERVAL 1 DAY),  4, 220.00,  'CHECKED_IN', 2, 8, NOW(), NOW());

-- ============================================================
-- RESERVATION EQUIPMENT  (equipment: tent=1, sleeping bag=2, stove=3)
-- ============================================================
INSERT INTO reservation_equipment (quantity, price_per_day, subtotal, reservation_id, equipment_id) VALUES
(2, 35.00, 210.00, 1, 1),  -- 2 tents for RES-001 (3 days)
(2, 12.00,  96.00, 2, 2),  -- 2 sleeping bags for RES-002 (4 days)
(1, 18.00,  54.00, 3, 3);  -- 1 stove for RES-003 (3 days)

-- ============================================================
-- CONTRACTS
-- ============================================================
INSERT INTO contracts (contract_number, terms, is_signed, signed_at, status, reservation_id, created_at, updated_at) VALUES
('CTR-001', 'Standard camping agreement. Guest agrees to follow all site rules, respect nature, and take full responsibility for any damages during the stay.', 1, DATE_SUB(NOW(), INTERVAL 1 DAY), 'ACTIVE',  1, NOW(), NOW()),
('CTR-002', 'Standard camping agreement. Guest agrees to follow all site rules, respect nature, and take full responsibility for any damages during the stay.', 0, NULL, 'PENDING', 2, NOW(), NOW());

-- ============================================================
-- INVOICES
-- ============================================================
INSERT INTO invoices (invoice_number, total_amount, status, issued_at, notes, reservation_id, equipment_order_id) VALUES
('INV-001', 435.00, 'PAID', DATE_SUB(NOW(), INTERVAL 2 DAY), 'Camping + equipment rental. Payment received. Thank you!', 1, NULL),
('INV-002', 576.00, 'SENT', DATE_SUB(NOW(), INTERVAL 1 DAY), 'Camping + equipment rental. Awaiting payment.',             2, NULL),
('INV-003', 504.00, 'PAID', DATE_SUB(NOW(), INTERVAL 3 DAY), 'Completed stay invoice. Payment received.',                 3, NULL);
