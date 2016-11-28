-- name: create-table-users!
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT, 
    slack_user_id TEXT NOT NULL DEFAULT (''),
    slack_user_name TEXT NOT NULL DEFAULT (''),
    created_dt DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, 
    active BOOLEAN NOT NULL DEFAULT (0),
    full_name TEXT NOT NULL DEFAULT (''),
    CONSTRAINT slack_user_name_unique UNIQUE (slack_user_name)
);

-- name: populate-users!
INSERT INTO users (
    slack_user_id,
    slack_user_name,
    active,
    full_name
)
VALUES (
    'U2XCP90V7',
    'rn',
    1,
    'Jerry'
);

-- name: create-table-orders!
CREATE TABLE IF NOT EXISTS orders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    created_dt DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    order_date TEXT DEFAULT (strftime('%Y-%m-%d', 'now')) NOT NULL,
    vendor_name TEXT NOT NULL DEFAULT (''),
    menu_url TEXT NOT NULL DEFAULT (''),
    CONSTRAINT slack_user_name_unique UNIQUE (order_date, vendor_name)
);

-- name: create-table-order-items!
CREATE TABLE IF NOT EXISTS order_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    order_id INTEGER NOT NULL,
    created_dt DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    description TEXT NOT NULL DEFAULT (''),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT slack_user_name_unique UNIQUE (user_id, order_id)
);

-- name: test-fetch
-- Just a test fetch (from 'users')
select * from users;

-- name: user-list
SELECT id, slack_user_id, slack_user_name, created_dt, active, full_name
FROM users
ORDER BY full_name;
