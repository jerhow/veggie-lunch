-- name: create-table-users!
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT, 
    slack_user_id TEXT NOT NULL DEFAULT (''),
    slack_user_name TEXT NOT NULL DEFAULT (''),
    created_dt datetime DEFAULT CURRENT_TIMESTAMP NOT NULL, 
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

-- name: test-fetch
-- Just a test fetch (from 'users')
select * from users;
