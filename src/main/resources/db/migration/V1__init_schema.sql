-- Initial schema for todo app (aligned with current entities)
-- Create users and todos tables

CREATE TABLE IF NOT EXISTS users (
    user_token VARCHAR(100) PRIMARY KEY,
    group_id BIGINT,
    last_use_time TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- add indexes for users
CREATE INDEX IF NOT EXISTS idx_users_group_id ON users (group_id);
CREATE INDEX IF NOT EXISTS idx_users_last_use_time ON users (last_use_time);

-- Ensure enum type todo_status exists (safe for re-run)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'todo_status') THEN
        CREATE TYPE todo_status AS ENUM ('TODO', 'DONE', 'CANCELLED');
    END IF;
END$$;

CREATE TABLE IF NOT EXISTS todos (
    id BIGSERIAL PRIMARY KEY,
    user_token VARCHAR(100) NOT NULL,
    task VARCHAR(200) NOT NULL,
    location VARCHAR(200),
    status todo_status DEFAULT 'TODO',
    original_string TEXT,
    todo_time TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    CONSTRAINT fk_user_token FOREIGN KEY (user_token) REFERENCES users(user_token)
);

CREATE INDEX IF NOT EXISTS idx_todos_user_time ON todos (user_token, todo_time);
CREATE INDEX IF NOT EXISTS idx_todos_status ON todos (status);

CREATE TABLE IF NOT EXISTS line_user_messages (
    id BIGSERIAL PRIMARY KEY,
    user_token VARCHAR(100) NOT NULL,
    message TEXT,
    receive_time TIMESTAMP WITH TIME ZONE NOT NULL,
    type VARCHAR(20) NOT NULL,
    reply_token VARCHAR(50),
    message_id VARCHAR(50)
);

CREATE INDEX IF NOT EXISTS idx_line_user_messages_user_token ON line_user_messages (user_token);
CREATE INDEX IF NOT EXISTS idx_line_user_messages_receive_time ON line_user_messages (receive_time);
