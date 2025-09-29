
-- Flyway migration: create weight_records table (aligned with WeightEntity)
-- Version: V6

CREATE TABLE IF NOT EXISTS public.weight_records (
    id BIGSERIAL PRIMARY KEY,
    user_token VARCHAR(100) NOT NULL,
    weight DECIMAL(5,2) NOT NULL,
    record_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Index for faster lookup by user and record time
CREATE INDEX IF NOT EXISTS idx_weight_records_user_token_record_at ON public.weight_records(user_token, record_at);

-- Trigger function to update updated_at timestamp
CREATE OR REPLACE FUNCTION public.trigger_set_updated_at()
RETURNS TRIGGER AS $body$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$body$ LANGUAGE plpgsql;

-- Trigger to call the function on UPDATE
DROP TRIGGER IF EXISTS trg_set_updated_at ON public.weight_records;
CREATE TRIGGER trg_set_updated_at
BEFORE UPDATE ON public.weight_records
FOR EACH ROW
EXECUTE FUNCTION public.trigger_set_updated_at();
