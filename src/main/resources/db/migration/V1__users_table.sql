CREATE TABLE user_profiles (
                               id SERIAL PRIMARY KEY,
                               uuid UUID NOT NULL UNIQUE,
                               username VARCHAR(255),
                               email VARCHAR(255)
);