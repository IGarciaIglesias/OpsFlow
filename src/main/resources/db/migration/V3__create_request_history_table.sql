CREATE TABLE request_history (
                                 id BIGSERIAL PRIMARY KEY,
                                 request_id BIGINT NOT NULL,
                                 from_status VARCHAR(50) NOT NULL,
                                 to_status VARCHAR(50) NOT NULL,
                                 changed_at TIMESTAMP NOT NULL,

                                 CONSTRAINT fk_request_history_request
                                     FOREIGN KEY (request_id)
                                         REFERENCES request(id)
);