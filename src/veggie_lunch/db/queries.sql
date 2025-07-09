-- name: create-table-users!
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT, 
    slack_user_id TEXT NOT NULL DEFAULT (''),
    slack_user_name TEXT NOT NULL DEFAULT (''),
    created_dt DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, 
    active BOOLEAN NOT NULL DEFAULT (0),
    full_name TEXT NOT NULL DEFAULT (''),
    level INTEGER NOT NULL DEFAULT (1),
    CONSTRAINT slack_user_name_unique UNIQUE (slack_user_name)
);

-- name: populate-users!
-- NOTE: These are Slack user IDs, which are not usable anyway. Still, this is not good practice - don't do this with real users. Populate the user table manually or via CI automation.
INSERT INTO users (
    slack_user_id,
    slack_user_name,
    active,
    full_name
)
VALUES (
    '<Slack_user_id>',
    '@<slack_handle>',
    1,
    'User name'
);

-- name: create-table-orders!
CREATE TABLE IF NOT EXISTS orders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    created_dt DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    order_date TEXT DEFAULT (strftime('%Y-%m-%d', 'now')) NOT NULL,
    vendor_name TEXT NOT NULL DEFAULT (''),
    menu_url TEXT NOT NULL DEFAULT (''),
    locked INTEGER NOT NULL DEFAULT (0),
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

-- name: create-index-order-date!
CREATE UNIQUE INDEX 'ix-order-date' ON 'orders' ('order_date' ASC);

-- name: create-table-user-level!
CREATE TABLE user_level (
    id INTEGER PRIMARY KEY,
    level TEXT NOT NULL
);

-- name: populate-user-level!
INSERT INTO user_level (id, level)
VALUES (1, 'User'), (2, 'Admin');

-- name: fetch-user-existence
-- Just a SELECT on the user to see whether they exist (a non-nil row)
SELECT id
FROM users
WHERE slack_user_name = :slack_user_name;

-- name: fetch-user-as-admin
-- Try to fetch a user as an admin, to see if they actually are one
SELECT u.full_name
FROM users AS u 
    INNER JOIN user_level AS ul
    ON u.level = ul.id
WHERE u.slack_user_name = :slack_user_name
    AND ul.level = 'Admin';

-- name: test-fetch
-- Just a test fetch (from 'users')
select * from users;

-- name: user-list
SELECT u.slack_user_name, u.created_dt, u.full_name, ul.level, 
    CASE WHEN u.active = 1 THEN 'Active' ELSE 'Inactive' END
FROM users AS u
    INNER JOIN user_level AS ul
    ON u.level = ul.id
ORDER BY 
    ul.level, u.full_name;

-- name: user-add!
-- Insert a user in the users table
INSERT INTO users (
    slack_user_name,
    full_name
)
VALUES (
    :slack_user_name,
    :full_name
);

-- name: user-remove!
-- Delete a user from the users table, based on Slack user name
DELETE FROM users
WHERE slack_user_name = :slack_user_name;

-- name: user-perm!
-- Updates a user's (permission) level
UPDATE users
SET level = :level
WHERE slack_user_name = :slack_user_name;

-- name: fetch-order-existence
-- Just a SELECT on the given date string to see whether an order exists (a non-nil row)
SELECT id
FROM orders
WHERE order_date = :order_date;

-- name: create-order!
INSERT INTO orders (vendor_name)
VALUES (:vendor_name);

-- name: set-menu-url!
UPDATE orders 
SET menu_url = :url
WHERE order_date = :order_date;

-- name: upsert-order-item!
-- Upsert's (sort of) a user's order_item for the current order
INSERT OR REPLACE INTO order_items (user_id, order_id, description) 
VALUES (:user_id, :order_id, :order_text);

-- name: delete-order-item!
DELETE FROM order_items
WHERE user_id = (SELECT id FROM users WHERE slack_user_name = :slack_user_name)
AND order_id = (SELECT id FROM orders WHERE order_date = :order_date);

-- name: fetch-menu-url
-- Gets the menu URL for the current order (if there is one)
SELECT CASE menu_url 
    WHEN '' 
    THEN 'Oops, no menu has been assigned to today`s order. Thanks Obama :unamused:' 
    ELSE menu_url END 
    AS menu_url
FROM orders
WHERE order_date = :order_date;

-- name: lock-order!
-- Sets the locked field to true (1) for a given order
UPDATE orders
SET locked = 1
WHERE order_date = :order_date;

-- name: unlock-order!
-- Sets the locked field to false (0) for a given order
UPDATE orders
SET locked = 0
WHERE order_date = :order_date;

-- name: fetch-order-items
-- Fetches the list of order items for a given date
SELECT 
    o.vendor_name, o.menu_url, 
    CASE o.locked WHEN 1 THEN 'LOCKED' ELSE 'OPEN' END AS status,
    u.slack_user_name, u.full_name, 
    oi.description
FROM
    orders AS o 
    INNER JOIN order_items AS oi 
        ON o.id = oi.order_id
    INNER JOIN users AS u 
        ON u.id = oi.user_id
WHERE 
    o.order_date = :order_date
ORDER BY
    u.full_name ASC;

-- name: update-user-status!
-- By 'status', we mean Active/Inactive, which we store as a boolean integer for 'active'
UPDATE users
SET active = :active_status
WHERE slack_user_name = :slack_user_name;
