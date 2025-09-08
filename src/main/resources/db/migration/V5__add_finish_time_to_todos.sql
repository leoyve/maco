-- V5__add_finish_time_to_todos.sql
-- Add finish_time column to todos
ALTER TABLE todos
ADD COLUMN IF NOT EXISTS finish_time timestamptz;
