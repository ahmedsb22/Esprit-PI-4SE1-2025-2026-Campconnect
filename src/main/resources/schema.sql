SET FOREIGN_KEY_CHECKS = 0;

-- Discard stale tablespaces first (fixes .ibd orphan files), then drop
DROP TABLE IF EXISTS invoices;
DROP TABLE IF EXISTS contracts;
DROP TABLE IF EXISTS reservation_equipment;
DROP TABLE IF EXISTS reservations;
DROP TABLE IF EXISTS camping_sites;
DROP TABLE IF EXISTS equipment;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================
-- USERS
-- =============================================
CREATE TABLE users (
    id         BIGINT NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(100),
    last_name  VARCHAR(100),
    email      VARCHAR(255),
    password   VARCHAR(255),
    phone      VARCHAR(20),
    address    VARCHAR(500),
    profile_image VARCHAR(500),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- ROLES
-- =============================================
CREATE TABLE roles (
    id          BIGINT NOT NULL AUTO_INCREMENT,
    name        ENUM('ADMIN','CAMPER','OWNER') NOT NULL,
    permissions VARCHAR(500),
    PRIMARY KEY (id),
    UNIQUE KEY uk_roles_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- USER_ROLES (join table)
-- =============================================
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- CAMPING_SITES
-- =============================================
CREATE TABLE camping_sites (
    id              BIGINT NOT NULL AUTO_INCREMENT,
    name            VARCHAR(100),
    description     TEXT,
    location        VARCHAR(255),
    address         VARCHAR(255),
    price_per_night DECIMAL(10,2),
    capacity        INT,
    category        VARCHAR(50),
    image_url       VARCHAR(500),
    has_wifi        BIT NOT NULL DEFAULT 0,
    has_parking     BIT NOT NULL DEFAULT 0,
    has_restrooms   BIT NOT NULL DEFAULT 0,
    has_showers     BIT NOT NULL DEFAULT 0,
    has_electricity BIT NOT NULL DEFAULT 0,
    has_pet_friendly BIT NOT NULL DEFAULT 0,
    is_active       BIT NOT NULL DEFAULT 1,
    is_verified     BIT NOT NULL DEFAULT 0,
    rating          DECIMAL(3,2) DEFAULT 0.00,
    review_count    INT NOT NULL DEFAULT 0,
    owner_id        BIGINT,
    created_at      DATETIME(6) NOT NULL,
    updated_at      DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_cs_owner FOREIGN KEY (owner_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- EQUIPMENT
-- =============================================
CREATE TABLE equipment (
    id                 BIGINT NOT NULL AUTO_INCREMENT,
    name               VARCHAR(100),
    description        TEXT,
    category           VARCHAR(50),
    price_per_day      DECIMAL(10,2),
    stock_quantity     INT,
    available_quantity INT,
    image_url          VARCHAR(500),
    specifications     VARCHAR(100),
    is_active          BIT NOT NULL DEFAULT 1,
    rating             DECIMAL(3,2) DEFAULT 0.00,
    review_count       INT NOT NULL DEFAULT 0,
    provider_id        BIGINT,
    created_at         DATETIME(6) NOT NULL,
    updated_at         DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_eq_provider FOREIGN KEY (provider_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- RESERVATIONS
-- =============================================
CREATE TABLE reservations (
    id                BIGINT NOT NULL AUTO_INCREMENT,
    reservation_number VARCHAR(50),
    check_in_date     DATE,
    check_out_date    DATE,
    number_of_guests  INT,
    total_price       DECIMAL(10,2),
    status            ENUM('PENDING','CONFIRMED','CANCELLED','CHECKED_IN','CHECKED_OUT','COMPLETED'),
    special_requests  TEXT,
    camper_id         BIGINT,
    camping_site_id   BIGINT,
    created_at        DATETIME(6) NOT NULL,
    updated_at        DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_res_number (reservation_number),
    CONSTRAINT fk_res_camper FOREIGN KEY (camper_id) REFERENCES users(id),
    CONSTRAINT fk_res_site   FOREIGN KEY (camping_site_id) REFERENCES camping_sites(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- RESERVATION_EQUIPMENT
-- =============================================
CREATE TABLE reservation_equipment (
    id             BIGINT NOT NULL AUTO_INCREMENT,
    reservation_id BIGINT,
    equipment_id   BIGINT,
    quantity       INT,
    price_per_day  DECIMAL(10,2),
    subtotal       DECIMAL(10,2),
    PRIMARY KEY (id),
    CONSTRAINT fk_re_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id),
    CONSTRAINT fk_re_equipment   FOREIGN KEY (equipment_id)   REFERENCES equipment(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- CONTRACTS
-- =============================================
CREATE TABLE contracts (
    id              BIGINT NOT NULL AUTO_INCREMENT,
    contract_number VARCHAR(50),
    terms           TEXT,
    status          ENUM('DRAFT','PENDING','SIGNED','ACTIVE','COMPLETED','CANCELLED'),
    is_signed       BIT NOT NULL DEFAULT 0,
    signature_url   VARCHAR(500),
    signed_at       DATETIME(6),
    reservation_id  BIGINT,
    created_at      DATETIME(6) NOT NULL,
    updated_at      DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_contract_number (contract_number),
    UNIQUE KEY uk_contract_reservation (reservation_id),
    CONSTRAINT fk_ct_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- INVOICES
-- =============================================
CREATE TABLE invoices (
    id                  BIGINT NOT NULL AUTO_INCREMENT,
    invoice_number      VARCHAR(50),
    total_amount        DECIMAL(10,2),
    status              ENUM('DRAFT','SENT','PAID','CANCELLED'),
    notes               TEXT,
    issued_at           DATETIME(6),
    reservation_id      BIGINT,
    equipment_order_id  BIGINT,
    PRIMARY KEY (id),
    UNIQUE KEY uk_invoice_number (invoice_number),
    CONSTRAINT fk_inv_reservation FOREIGN KEY (reservation_id)     REFERENCES reservations(id),
    CONSTRAINT fk_inv_eq_order    FOREIGN KEY (equipment_order_id) REFERENCES reservation_equipment(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
