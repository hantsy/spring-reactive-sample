CREATE TABLE posts (
id SERIAL PRIMARY KEY,
title VARCHAR(255),
content VARCHAR(255),
created_at TIMESTAMP,
updated_at TIMESTAMP
);