-- V2: Convert todos.status from custom enum type (todo_status) to varchar
-- Safe: only runs ALTER when column exists and is currently using the todo_status UDT
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'todos'
      AND column_name = 'status'
      AND udt_name = 'todo_status'
  ) THEN
    ALTER TABLE todos ALTER COLUMN status TYPE varchar USING status::text;
  END IF;
END
$$;
