ALTER TABLE public.outbox_event
    ADD COLUMN last_error character varying(2000),
    ADD COLUMN processing_token character varying(64);