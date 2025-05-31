CREATE TABLE subscriptions (
                               id BIGSERIAL PRIMARY KEY,
                               user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                               service_name VARCHAR(255) NOT NULL,
                               price DECIMAL(10, 2) NOT NULL,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                               UNIQUE(user_id, service_name)
);

CREATE INDEX idx_subscriptions_user_id ON subscriptions(user_id);
CREATE INDEX idx_subscriptions_service_name ON subscriptions(service_name);
