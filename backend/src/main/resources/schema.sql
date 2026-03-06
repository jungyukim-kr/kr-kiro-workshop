CREATE TABLE IF NOT EXISTS store (
    id BIGSERIAL PRIMARY KEY,
    store_code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS admin (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES store(id),
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(store_id, username)
);

CREATE TABLE IF NOT EXISTS store_table (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES store(id),
    table_number INT NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(store_id, table_number)
);

CREATE TABLE IF NOT EXISTS table_session (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES store(id),
    table_id BIGINT NOT NULL REFERENCES store_table(id),
    session_code VARCHAR(50) NOT NULL UNIQUE,
    started_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ended_at TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS menu (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES store(id),
    name VARCHAR(100) NOT NULL,
    price INT NOT NULL CHECK(price >= 0),
    description TEXT,
    category VARCHAR(50) NOT NULL,
    image_url VARCHAR(500),
    spicy_level VARCHAR(20),
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS menu_spicy_option (
    id BIGSERIAL PRIMARY KEY,
    menu_id BIGINT NOT NULL REFERENCES menu(id),
    option_name VARCHAR(50) NOT NULL,
    display_order INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES store(id),
    table_id BIGINT NOT NULL REFERENCES store_table(id),
    session_id BIGINT NOT NULL REFERENCES table_session(id),
    order_number VARCHAR(20) NOT NULL UNIQUE,
    total_amount INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'WAITING',
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS order_item (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    menu_id BIGINT NOT NULL REFERENCES menu(id),
    menu_name VARCHAR(100) NOT NULL,
    quantity INT NOT NULL CHECK(quantity > 0),
    unit_price INT NOT NULL,
    spicy_option VARCHAR(50),
    special_request TEXT
);

CREATE TABLE IF NOT EXISTS order_history (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES store(id),
    table_id BIGINT NOT NULL REFERENCES store_table(id),
    session_code VARCHAR(50) NOT NULL,
    order_number VARCHAR(20) NOT NULL,
    total_amount INT NOT NULL,
    ordered_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS order_history_item (
    id BIGSERIAL PRIMARY KEY,
    order_history_id BIGINT NOT NULL REFERENCES order_history(id),
    menu_name VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    unit_price INT NOT NULL,
    spicy_option VARCHAR(50),
    special_request TEXT
);
