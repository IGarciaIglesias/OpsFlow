CREATE TABLE request (
                         id BIGSERIAL PRIMARY KEY,
                         title VARCHAR(255) NOT NULL,
                         description VARCHAR(1000),
                         status VARCHAR(50) NOT NULL,
                         created_at TIMESTAMP NOT NULL
);
