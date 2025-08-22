-- V3: Ensure index on line_user_messages.user_token
-- Create index if not exists to be safe for re-runs

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'line_user_messages') THEN
        EXECUTE 'CREATE INDEX IF NOT EXISTS idx_line_user_messages_user_token ON line_user_messages (user_token)';
    END IF;
END$$;
