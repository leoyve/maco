-- Add index on todos.user_token to speed up queries filtered by user_token
CREATE INDEX IF NOT EXISTS idx_todos_user_token ON todos (user_token);
