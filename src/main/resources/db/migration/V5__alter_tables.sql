ALTER TABLE request ADD COLUMN code VARCHAR(50);

UPDATE request
SET code = CONCAT('REQ-', id)
WHERE code IS NULL;

ALTER TABLE request
    ALTER COLUMN code SET NOT NULL;

ALTER TABLE request
    ADD CONSTRAINT uk_request_code UNIQUE (code);

UPDATE request
SET code = 'REQ-' || id
WHERE code IS NULL;