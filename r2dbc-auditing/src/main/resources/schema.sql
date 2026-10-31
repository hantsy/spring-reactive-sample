CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS posts (
     id UUID DEFAULT uuid_generate_v4(),
     title VARCHAR(255),
     content VARCHAR(255),
    status VARCHAR(255) default 'DRAFT',
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    created_at TIMESTAMP , --NOT NULL DEFAULT LOCALTIMESTAMP,
    updated_at TIMESTAMP,
    version INTEGER
 );

ALTER TABLE posts ADD CONSTRAINT posts_pk PRIMARY KEY (id);