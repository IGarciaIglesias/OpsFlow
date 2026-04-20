CREATE TABLE catalog (
                         id BIGSERIAL PRIMARY KEY,
                         code VARCHAR(100) NOT NULL,
                         category VARCHAR(100) NOT NULL,
                         description VARCHAR(255) NOT NULL,
                         active BOOLEAN NOT NULL DEFAULT TRUE,
                         CONSTRAINT uk_catalog_category_code UNIQUE (category, code)
);

CREATE INDEX idx_catalog_category ON catalog(category);
CREATE INDEX idx_catalog_category_active ON catalog(category, active);