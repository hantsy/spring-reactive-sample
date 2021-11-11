CREATE TABLE posts (
id SERIAL PRIMARY KEY,
title VARCHAR(255),
content VARCHAR(255),
created_at TIMESTAMP,
updated_at TIMESTAMP,
created_by VARCHAR(255),
updated_by VARCHAR(255),
version INTEGER
);