CREATE TABLE app_user (
                          id BIGSERIAL PRIMARY KEY,
                          username VARCHAR(50) NOT NULL UNIQUE,
                          password VARCHAR(255) NOT NULL,
                          role VARCHAR(30) NOT NULL,
                          active BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO app_user (username, password, role, active)
VALUES (
           'admin',
           '$2a$10$7sQxZ6T7B0dZCBoZVxKkOeQ6zRkC6lZrE8Y0bYfF0Pj1E0p8zKxqK',
           'ADMIN',
           true
       );